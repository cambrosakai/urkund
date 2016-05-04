/**
 *
 * Copyright (c) 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 *********************************************************************************
 */
package org.sakaiproject.contentreview.impl.urkund;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.impl.ContentReviewDao;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.model.ContentReviewItem;
import org.sakaiproject.contentreview.model.ContentReviewItemUrkund;

import org.sakaiproject.contentreview.urkund.client.SubmissionsResponse;
import org.sakaiproject.contentreview.urkund.client.UrkundClient;
import org.sakaiproject.contentreview.urkund.client.UrkundException;
import org.sakaiproject.contentreview.urkund.client.UrkundSubmission;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

public class UrkundReviewServiceImpl extends BaseReviewServiceImpl {

    private static final Log log = LogFactory.getLog(UrkundReviewServiceImpl.class);

    public static final String URKUND_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String SERVICE_NAME = "Urkund";

    // Site property to enable or disable use of Urkund for the site
    private static final String URKUND_SITE_PROPERTY = "urkund";

    // 0 is unique user ID (must include friendly email address characters only)
    // 1 is unique site ID (must include friendly email address characters only)
    // 2 is integration context string (must be 2 to 10 characters)
    private static final String URKUND_SPOOFED_EMAIL_TEMPLATE = "%s_%s.%s@submitters.urkund.com";

    final static long LOCK_PERIOD = 12000000;

    private Long maxRetry = null;

    private List<String> enabledSiteTypes;

    private UrkundClient urkundClient;

    private String spoofEmailContext;

    /**
     * Setters
     */
    private ServerConfigurationService serverConfigurationService;

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setUrkundClient(UrkundClient urkundClient) {
        this.urkundClient = urkundClient;
    }
    private EntityManager entityManager;

    public void setEntityManager(EntityManager en) {
        this.entityManager = en;
    }

    private ContentHostingService contentHostingService;

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    private SakaiPersonManager sakaiPersonManager;

    public void setSakaiPersonManager(SakaiPersonManager s) {
        this.sakaiPersonManager = s;
    }

    private ContentReviewDao dao;

    public void setDao(ContentReviewDao dao) {
        super.setDao(dao);
        this.dao = dao;
    }

    private UserDirectoryService userDirectoryService;

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        super.setUserDirectoryService(userDirectoryService);
        this.userDirectoryService = userDirectoryService;
    }

    private SiteService siteService;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private SqlService sqlService;

    public void setSqlService(SqlService sql) {
        sqlService = sql;
    }

    private PreferencesService preferencesService;

    public void setPreferencesService(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    private UrkundContentValidator urkundContentValidator;

    public void setUrkundContentValidator(UrkundContentValidator urkundContentValidator) {
        this.urkundContentValidator = urkundContentValidator;
    }

    private SecurityService securityService;

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    /**
     * Place any code that should run when this class is initialized by spring here
     */
    public void init() {

        maxRetry = Long.valueOf(serverConfigurationService.getInt("urkund.maxRetry", 10));

        enabledSiteTypes = Arrays.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("urkund.sitetypes")));

        if (enabledSiteTypes != null && !enabledSiteTypes.isEmpty()) {
            log.info("Urkund is enabled for site types: " + StringUtils.join(enabledSiteTypes, ","));
        }

        spoofEmailContext = serverConfigurationService.getString("urkund.spoofemailcontext", null);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    /**
     * Allow Urkund for this site?
     */
    public boolean isSiteAcceptable(Site s) {

        if (s == null) {
            return false;
        }

        log.debug("isSiteAcceptable: " + s.getId() + " / " + s.getTitle());

        // Check site property
        ResourceProperties properties = s.getProperties();

        String prop = (String) properties.get(URKUND_SITE_PROPERTY);
        if (prop != null) {
            log.debug("Using site property: " + prop);
            return Boolean.parseBoolean(prop);
        }

        // Check list of allowed site types, if defined
        if (enabledSiteTypes != null && !enabledSiteTypes.isEmpty()) {
            log.debug("Using site type: " + s.getType());
            return enabledSiteTypes.contains(s.getType());
        }

        // No property set, no restriction on site types, so allow
        return true;
    }

    
    @Override
    public String getIconUrlforScore(Long score, Long warnings) {

        String urlBase = "/sakai-contentreview-tool-urkund/images/";
        String suffix = ".gif";
        
        if (warnings > 0) {
            return urlBase + "w" + suffix;
        }        

        if (score.equals((long) 0)) {
            return urlBase + "0" + suffix;
        } else if (score.compareTo(Long.valueOf(12)) < 0) {
            return urlBase + "1" + suffix;
        } else if (score.compareTo(Long.valueOf(25)) < 0) {
            return urlBase + "2" + suffix;
        } else if (score.compareTo(Long.valueOf(38)) < 0) {
            return urlBase + "3" + suffix;
        } else if (score.compareTo(Long.valueOf(50)) < 0) {
            return urlBase + "4" + suffix;
        } else if (score.compareTo(Long.valueOf(62)) < 0) {
            return urlBase + "5" + suffix;
        } else if (score.compareTo(Long.valueOf(75)) < 0) {
            return urlBase + "6" + suffix;
        } else if (score.compareTo(Long.valueOf(88)) < 0) {
            return urlBase + "7" + suffix;
        } else {
            return urlBase + "8" + suffix;
        }

    }

    /**
     * This uses the default Instructor information or current user.
     *
     * @see org.sakaiproject.contentreview.impl.hbm.BaseReviewServiceImpl#getReviewReportInstructor(java.lang.String)
     */
    public String getReviewReportInstructor(String contentId) throws QueueException, ReportException {

        Search search = new Search();
        search.addRestriction(new Restriction("contentId", contentId));
        List<ContentReviewItemUrkund> matchingItems = dao.findBySearch(ContentReviewItemUrkund.class, search);
        if (matchingItems.isEmpty()) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.warn("More than one matching item found - using first item found");
        }

        // check that the report is available
        ContentReviewItemUrkund item = (ContentReviewItemUrkund) matchingItems.iterator().next();
        if (item.getStatus().compareTo(ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
            log.debug("Report not available: " + item.getStatus());
            throw new ReportException("Report not available: " + item.getStatus());
        }

        return item.getReportUrl();
    }

    public String getReviewReportStudent(String contentId) throws QueueException, ReportException {

        Search search = new Search();
        search.addRestriction(new Restriction("contentId", contentId));
        List<ContentReviewItemUrkund> matchingItems = dao.findBySearch(ContentReviewItemUrkund.class, search);
        if (matchingItems.size() == 0) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.debug("More than one matching item found - using first item found");
        }

        // check that the report is available
        ContentReviewItemUrkund item = (ContentReviewItemUrkund) matchingItems.iterator().next();
        if (item.getStatus().compareTo(ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
            log.debug("Report not available: " + item.getStatus());
            throw new ReportException("Report not available: " + item.getStatus());
        }

        return item.getReportUrl();
    }

    @Override
    public void queueContent(String userId, String siteId, String taskId, String contentId)
            throws QueueException {

        log.debug("Method called queueContent(" + userId + "," + siteId + "," + contentId + ")");

        if (userId == null) {
            log.debug("Using current user");
            userId = userDirectoryService.getCurrentUser().getId();
        }

        if (siteId == null) {
            log.debug("Using current site");
            siteId = toolManager.getCurrentPlacement().getContext();
        }

        if (taskId == null) {
            log.debug("Generating default taskId");
            taskId = siteId + " " + defaultAssignmentName;
        }

        log.debug("Adding content: " + contentId + " from site " + siteId
                + " and user: " + userId + " for task: " + taskId + " to submission queue");

        /*
         * first check that this content has not been submitted before this may
         * not be the best way to do this - perhaps use contentId as the primary
         * key for now id is the primary key and so the database won't complain
         * if we put in repeats necessitating the check
         */
        List<ContentReviewItemUrkund> existingItems = getItemsByContentId(contentId);
        if (existingItems.size() > 0) {
            throw new QueueException("Content " + contentId + " is already queued, not re-queued");
        }
        ContentReviewItemUrkund item = new ContentReviewItemUrkund(userId, siteId, taskId, contentId, new Date(),
                ContentReviewItem.NOT_SUBMITTED_CODE, null, null, null);
        item.setNextRetryTime(new Date());
        dao.save(item);
    }

    public int getReviewScore(String contentId)
            throws QueueException, ReportException, Exception {
        log.debug("Getting review score for content: " + contentId);

        List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);
        if (matchingItems.size() == 0) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.debug("More than one matching item - using first item found");
        }

        ContentReviewItem item = (ContentReviewItem) matchingItems.iterator().next();
        if (item.getStatus().compareTo(ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
            log.debug("Report not available: " + item.getStatus());
            throw new ReportException("Report not available: " + item.getStatus());
        }

        return item.getReviewScore().intValue();
    }

    public Long getReviewStatus(String contentId)
            throws QueueException {
        log.debug("Returning review status for content: " + contentId);

        List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);

        if (matchingItems.size() == 0) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.debug("more than one matching item found - using first item found");
        }

        return ((ContentReviewItem) matchingItems.iterator().next()).getStatus();
    }

    public Date getDateQueued(String contentId)
            throws QueueException {
        log.debug("Returning date queued for content: " + contentId);

        List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);
        if (matchingItems.size() == 0) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.debug("more than one matching item found - using first item found");
        }

        return ((ContentReviewItem) matchingItems.iterator().next()).getDateQueued();
    }

    public Date getDateSubmitted(String contentId)
            throws QueueException, SubmissionException {
        log.debug("Returning date queued for content: " + contentId);

        List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);

        if (matchingItems.size() == 0) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.debug("more than one matching item found - using first item found");
        }

        ContentReviewItem item = (ContentReviewItem) matchingItems.iterator().next();
        if (item.getDateSubmitted() == null) {
            log.debug("Content not yet submitted: " + item.getStatus());
            throw new SubmissionException("Content not yet submitted: " + item.getStatus());
        }

        return item.getDateSubmitted();
    }

    public List<ContentReviewItem> getReportList(String siteId, String taskId) {
        log.debug("Returning list of reports for site: " + siteId + ", task: " + taskId);
        Search search = new Search();
        //Urkund-99 siteId can be null
        if (siteId != null) {
            search.addRestriction(new Restriction("siteId", siteId));
        }
        search.addRestriction(new Restriction("taskId", taskId));
        search.addRestriction(new Restriction("status", ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE));

        return (List<ContentReviewItem>) (Object) dao.findBySearch(ContentReviewItemUrkund.class, search);
    }

    public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId) {
        log.debug("Returning list of reports for site: " + siteId + ", task: " + taskId);
        Search search = new Search();
        //Urkund-99 siteId can be null
        if (siteId != null) {
            search.addRestriction(new Restriction("siteId", siteId));
        }
        search.addRestriction(new Restriction("taskId", taskId));

        return (List<ContentReviewItem>) (Object) dao.findBySearch(ContentReviewItemUrkund.class, search);
    }

    public List<ContentReviewItem> getReportList(String siteId) {
        log.debug("Returning list of reports for site: " + siteId);

        Search search = new Search();
        search.addRestriction(new Restriction("siteId", siteId));
        search.addRestriction(new Restriction("status", ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE));

        return (List<ContentReviewItem>) (Object) dao.findBySearch(ContentReviewItemUrkund.class, search);
    }

    public void resetUserDetailsLockedItems(String userId) {
        Search search = new Search();
        search.addRestriction(new Restriction("userId", userId));
        search.addRestriction(new Restriction("status", ContentReviewItem.SUBMISSION_ERROR_USER_DETAILS_CODE));

        List<ContentReviewItemUrkund> lockedItems = dao.findBySearch(ContentReviewItemUrkund.class, search);
        for (int i = 0; i < lockedItems.size(); i++) {
            ContentReviewItemUrkund thisItem = (ContentReviewItemUrkund) lockedItems.get(i);
            thisItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_RETRY_CODE);
            dao.update(thisItem);
        }
    }

    public void removeFromQueue(String ContentId) {
        List<ContentReviewItemUrkund> object = getItemsByContentId(ContentId);
        dao.delete(object);
    }

    public String getReviewReport(String contentId)
            throws QueueException, ReportException {

        // first retrieve the record from the database to get the externalId of
        // the content
        log.warn("Deprecated Methog getReviewReport(String contentId) called");
        return this.getReviewReportInstructor(contentId);
    }

    public String getInlineTextId(String assignmentReference, String userId, long submissionTime) {
        return "";
    }

    public boolean acceptInlineAndMultipleAttachments() {
        return false;
    }

    public int getReviewScore(String contentId, String assignmentRef, String userId) throws QueueException,
            ReportException, Exception {
        int result = -1;
        try {
            List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);
            if (matchingItems.isEmpty()) {
                log.debug("Content " + contentId + " has not been queued previously");
            }
            if (matchingItems.size() > 1) {
                log.debug("More than one matching item - using first item found");
            }

            ContentReviewItemUrkund item = (ContentReviewItemUrkund) matchingItems.iterator().next();
            if (item.getStatus().compareTo(ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
                log.debug("Report not available: " + item.getStatus());
            }
            result = item.getReviewScore();
        } catch (Exception e) {
            log.error("(getReviewScore)" + e);
        }
        return result;
    }

    List<ContentReviewItemUrkund> getItemsByContentId(String contentId) {
        Search search = new Search();
        search.addRestriction(new Restriction("contentId", contentId));
        List<ContentReviewItemUrkund> existingItems = dao.findBySearch(ContentReviewItemUrkund.class, search);
        return existingItems;
    }

    /**
     * Check if grade sync has been run already for the specified site
     *
     * @param sess Current Session
     * @param taskId
     * @return
     */
    public boolean gradesChecked(Session sess, String taskId) {
        try {
            String sessSync = sess.getAttribute("sync").toString();
            if (sessSync.equals(taskId)) {
                return true;
            }
        } catch (Exception e) {
            //log.error("(gradesChecked)"+e);
        }
        return false;
    }

    /**
     * Check if the specified user has the student role on the specified site.
     *
     * @param siteId Site ID
     * @param userId User ID
     * @return true if user has student role on the site.
     */
    public boolean isUserStudent(String siteId, String userId) {
        boolean isStudent = false;
        try {
            Set<String> studentIds = siteService.getSite(siteId).getUsersIsAllowed("section.role.student");
            List<User> activeUsers = userDirectoryService.getUsers(studentIds);
            for (User user : activeUsers) {
                if (userId.equals(user.getId())) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.info("(isStudentUser)" + e);
        }
        return isStudent;
    }

    public void pushAdvisor() {
        securityService.pushAdvisor(new SecurityAdvisor() {

            public SecurityAdvisor.SecurityAdvice isAllowed(String userId, String function,
                    String reference) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
        });
    }

    public void popAdvisor() {
        securityService.popAdvisor();
    }

    /*
     * Obtain a lock on the item
     */
    private boolean obtainLock(String itemId) {
        Boolean lock = dao.obtainLock(itemId, serverConfigurationService.getServerId(), LOCK_PERIOD);
        return (lock != null) ? lock : false;
    }

    /*
     * Get the next item that needs to be submitted
     *
     */
    private ContentReviewItemUrkund getNextItemInSubmissionQueue() {

        Search search = new Search();
        search.addRestriction(new Restriction("status", ContentReviewItem.NOT_SUBMITTED_CODE));

        List<ContentReviewItemUrkund> notSubmittedItems = dao.findBySearch(ContentReviewItemUrkund.class, search);
        for (ContentReviewItemUrkund notSubmittedItem : notSubmittedItems) {
            // can we get a lock?
            if (obtainLock("item." + notSubmittedItem.getId().toString())) {
                return notSubmittedItem;
            }
        }

        search = new Search();
        search.addRestriction(new Restriction("status", ContentReviewItem.SUBMISSION_ERROR_RETRY_CODE));
        notSubmittedItems = dao.findBySearch(ContentReviewItemUrkund.class, search);

        //we need the next one whose retry time has not been reached
        for (ContentReviewItemUrkund notSubmittedItem : notSubmittedItems) {
            if (hasReachedRetryTime(notSubmittedItem) && obtainLock("item." + notSubmittedItem.getId().toString())) {
                return notSubmittedItem;
            }
        }

        return null;
    }

    private boolean hasReachedRetryTime(ContentReviewItemUrkund item) {

        // has the item reached its next retry time?
        if (item.getNextRetryTime() == null) {
            item.setNextRetryTime(new Date());
        }

        if (item.getNextRetryTime().after(new Date())) {
            //we haven't reached the next retry time
            log.debug("next retry time not yet reached for item: " + item.getId());
            dao.update(item);
            return false;
        }

        return true;

    }

    private void releaseLock(ContentReviewItem currentItem) {
        dao.releaseLock("item." + currentItem.getId().toString(), serverConfigurationService.getServerId());
    }

    public void processQueue() {

        log.debug("Processing submission queue");
        int errors = 0;
        int success = 0;

        for (ContentReviewItemUrkund currentItem = getNextItemInSubmissionQueue();
                currentItem != null;
                currentItem = getNextItemInSubmissionQueue()) {

            if (currentItem.getRetryCount() == null) {
                currentItem.setRetryCount(Long.valueOf(0));
                currentItem.setNextRetryTime(this.getNextRetryTime(0));
                dao.update(currentItem);
            } else if (currentItem.getRetryCount().intValue() > maxRetry) {
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_RETRY_EXCEEDED);
                currentItem.setLastError("Contact Service desk for help");
                dao.update(currentItem);
                errors++;
                continue;
            } else {
                long l = currentItem.getRetryCount();
                l++;
                currentItem.setRetryCount(l);
                currentItem.setNextRetryTime(this.getNextRetryTime(l));
                dao.update(currentItem);
            }

            User user;
            try {
                user = userDirectoryService.getUser(currentItem.getUserId());
            } catch (UserNotDefinedException e1) {
                log.error("Submission attempt unsuccessful - User not found.", e1);
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_NO_RETRY_CODE);
                currentItem.setLastError("Contact Service desk for help");
                dao.update(currentItem);
                releaseLock(currentItem);
                errors++;
                continue;
            }

            String uem = null;
            
            if (spoofEmailContext != null && spoofEmailContext.length() >= 2 && spoofEmailContext.length() <= 10) {
                uem = generateSpoofedSubmitterEmail(user, currentItem.getSiteId());
            } else {
                uem = getEmail(user);
            }
            
            if (uem == null) {
                log.error("User: " + user.getEid() + " has no valid email");
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_USER_DETAILS_CODE);
                currentItem.setLastError("Contact Service desk for help");
                dao.update(currentItem);
                releaseLock(currentItem);
                errors++;
                continue;
            }

            ContentResource resource;
            ResourceProperties resourceProperties;
            String fileName;
            try {
                try {
                    resource = contentHostingService.getResource(currentItem.getContentId());

                } catch (IdUnusedException e4) {
                    log.warn("IdUnusedException: no resource with id " + currentItem.getContentId());
                    dao.delete(currentItem);
                    errors++;
                    continue;
                }
                resourceProperties = resource.getProperties();
                fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
                // Truncate long filenames to max 200 chars
                if (fileName != null && fileName.length() >= 200) {
                    fileName = truncateFileName(fileName, 198);
                }
            } catch (PermissionException e2) {
                log.error("Submission failed due to permission error: " + e2.getMessage());
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_NO_RETRY_CODE);
                currentItem.setLastError("Contact Service desk for help");
                dao.update(currentItem);
                releaseLock(currentItem);
                errors++;
                continue;
            } catch (TypeException e) {
                log.error("Submission failed due to content Type error.", e);
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_NO_RETRY_CODE);
                currentItem.setLastError("Contact Service desk for help");
                dao.update(currentItem);
                releaseLock(currentItem);
                errors++;
                continue;
            }

            // Get last GUID in contentId for external Id
            String[] tokens = currentItem.getContentId().split("/");
            String extId = tokens[tokens.length - 2];

            // Only extension in filename
            if (fileName.lastIndexOf(".") == 0) {
                fileName = extId + fileName;
            }

            UrkundSubmission submission = new UrkundSubmission();
            submission.setSubmitterEmail(uem);

            submission.setSubject(""); // Insert site name?
            submission.setMessage(""); // Insert assignment title?

            submission.setFilename(fileName);
            submission.setExternalId(extId);
            submission.setAnon(false);

            // Check if valid content type, only try to submit if valid
            if (submission.getSupportedContentType() == null) {
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_NO_RETRY_CODE);
                currentItem.setLastError("Invalid content type");
                log.error("Submit NOT successful, invalid content type, will NOT be retried, content Id: " + currentItem.getContentId());
                dao.update(currentItem);
                releaseLock(currentItem);
                errors++;
                continue;
            }

            try {
                submission.setDocument(resource.streamContent());
            } catch (ServerOverloadException ex) {
                Logger.getLogger(UrkundReviewServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                SubmissionsResponse rsp = urkundClient.submitDocument(submission);
                currentItem.setStatus(ContentReviewItem.SUBMITTED_AWAITING_REPORT_CODE);
                currentItem.setLastError(null);
                currentItem.setErrorCode(null);
                currentItem.setExternalId(extId);
                currentItem.setDateSubmitted(new Date());
                success++;
                dao.update(currentItem);
                log.info("Submit successful, content Id:" + currentItem.getContentId());
            } catch (UrkundException e) {
                // If server responds with a 4xx it´s a bad request, otherwise retry
                if (e.getHttpCode() >= 400 && e.getHttpCode() < 500) {
                    currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_NO_RETRY_CODE);
                    currentItem.setLastError("Contact Service desk for help");
                    log.error("Submit NOT successful, HTTP code: " + e.getHttpCode() + ", will NOT be retried, content Id: " + currentItem.getContentId() + ", error: " + e.getMessage());
                } else {
                    currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_RETRY_CODE);
                    currentItem.setLastError("Submission Failure, will be retried");
                    log.error("Submit NOT successful, HTTP code: " + e.getHttpCode() + ", will be retried, content Id: " + currentItem.getContentId() + ", error: " + e.getMessage());
                }

                dao.update(currentItem);
                errors++;

            } catch (Exception e) {
                // Unknown error, retry
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_RETRY_CODE);
                currentItem.setLastError("Submission Failure, will be retried");
                dao.update(currentItem);
                errors++;
                log.error("Submit NOT successful, will be retried, content Id: " + currentItem.getContentId() + ", error: " + e.getMessage());
            }
            //release the lock so the reports job can handle it
            releaseLock(currentItem);
            getNextItemInSubmissionQueue();
        }
        if (success > 0 || errors > 0) {
            log.info("Submission queue run completed: " + success + " items submitted, " + errors + " errors.");
        }
    }

    private String truncateFileName(String fileName, int i) {
        //get the extension for later re-use
        String extension = "";
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }

        fileName = fileName.substring(0, i - extension.length());
        fileName = fileName + extension;

        return fileName;
    }

    public void checkForReports() {
        checkForReportsBulk();
    }

    /*
     * Fetch reports on a class by class basis
     */
    @SuppressWarnings({"deprecation", "unchecked"})
    public void checkForReportsBulk() {

        // get the list of all items that are waiting for reports
        List<ContentReviewItemUrkund> awaitingReport = dao.findByProperties(ContentReviewItemUrkund.class,
                new String[]{"status"},
                new Object[]{ContentReviewItem.SUBMITTED_AWAITING_REPORT_CODE});

        awaitingReport.addAll(dao.findByProperties(ContentReviewItemUrkund.class,
                new String[]{"status"},
                new Object[]{ContentReviewItem.REPORT_ERROR_RETRY_CODE}));

        Iterator<ContentReviewItemUrkund> listIterator = awaitingReport.iterator();
        log.debug("There are " + awaitingReport.size() + " submissions awaiting reports");

        while (listIterator.hasNext()) {
            ContentReviewItemUrkund currentItem = (ContentReviewItemUrkund) listIterator.next();

            // has the item reached its next retry time?
            if (currentItem.getNextRetryTime() == null) {
                currentItem.setNextRetryTime(new Date());
            }

            if (currentItem.getNextRetryTime().after(new Date())) {
                //we haven't reached the next retry time
                log.debug("next retry time not yet reached for item: " + currentItem.getId());
                dao.update(currentItem);
                continue;
            }

            if (currentItem.getRetryCount() == null) {
                currentItem.setRetryCount(Long.valueOf(0));
                currentItem.setNextRetryTime(this.getNextRetryTime(0));
            } else if (currentItem.getRetryCount().intValue() > maxRetry) {
                currentItem.setStatus(ContentReviewItem.SUBMISSION_ERROR_RETRY_EXCEEDED);
                dao.update(currentItem);
                continue;
            } else {
                long l = currentItem.getRetryCount();
                l++;
                currentItem.setRetryCount(l);
                currentItem.setNextRetryTime(this.getNextRetryTime(l));
                dao.update(currentItem);
            }

            try {
                List<SubmissionsResponse> rspList = urkundClient.getReports(currentItem.getExternalId());

                for (SubmissionsResponse rsp : rspList) {
                    if (rsp.status.state == null) {
                        log.error("status state == null");
                    } else {
                        String state = rsp.status.state.toLowerCase();
                        if (currentItem.getExternalId().equals(rsp.externalId)) {
                            log.info("Urkund submission report, state: " + state + ", " + rsp.toString());
                            if ("analyzed".equals(state)) {
                                currentItem.setReviewScore(Math.round(rsp.report.significance));
                                currentItem.setReportUrl(rsp.report.reportUrl);
                                currentItem.setStatus(ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE);
                                currentItem.setDateReportReceived(new Date());
                                currentItem.setWarnings(rsp.report.warnings.size());
                                currentItem.setOptOutUrl(rsp.document.optOutInfo.url);
                            } else {
                                currentItem.setStatus(ContentReviewItem.REPORT_ERROR_NO_RETRY_CODE);
                                currentItem.setLastError(rsp.status.message);
                            } 
                            dao.update(currentItem);
                        }
                    }
                }
            } catch (UrkundException e) {
                log.error(e.getMessage());
            }
        }
    }

    // returns null if no valid email exists
    public String getEmail(User user) {

        String uem = null;

        // Check account email address
        String account_email = null;

        if (isValidEmail(user.getEmail())) {
            account_email = user.getEmail().trim();
        }

        // Lookup system profile email address if necessary
        String profile_email = null;
        if (account_email == null) {
            SakaiPerson sp = sakaiPersonManager.getSakaiPerson(user.getId(), sakaiPersonManager.getSystemMutableType());
            if (sp != null && isValidEmail(sp.getMail())) {
                profile_email = sp.getMail().trim();
            }
        }

        if (uem == null && profile_email != null) {
            uem = profile_email;
        }

        if (uem == null && account_email != null) {
            uem = account_email;
        }

        log.debug("Using email " + uem + " for user eid " + user.getEid() + " id " + user.getId());
        return uem;
    }

    /**
     * Is this a valid email the service will recognize
     *
     * @param email
     * @return
     */
    private boolean isValidEmail(String email) {

        if (email == null || email.equals("")) {
            return false;
        }

        email = email.trim();
        //must contain @
        if (!email.contains("@")) {
            return false;
        }

        //an email can't contain spaces
        if (email.indexOf(" ") > 0) {
            return false;
        }

        //use commons-validator
        EmailValidator validator = EmailValidator.getInstance();
        return validator.isValid(email);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.contentreview.service.ContentReviewService#isAcceptableContent(org.sakaiproject.content.api.ContentResource)
     */
    public boolean isAcceptableContent(ContentResource resource) {
        return urkundContentValidator.isAcceptableContent(resource);
    }

    /**
     * find the next time this item should be tried
     *
     * @param retryCount
     * @return
     */
    private Date getNextRetryTime(long retryCount) {
        int offset = 30;

        if (retryCount == 1) {
            offset = 60;
        } else if (retryCount == 2) {
            offset = 120;
        } else if (retryCount == 3) {
            offset = 240;
        } else if (retryCount > 3) {
            offset = 480;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, offset);
        return cal.getTime();
    }

    public String getLocalizedStatusMessage(String messageCode, String userRef) {

        String userId = EntityReference.getIdFromRef(userRef);
        ResourceLoader resourceLoader = new ResourceLoader(userId, "urkund");
        return resourceLoader.getString(messageCode);
    }

    public String getReviewError(String contentId) {
        return getLocalizedReviewErrorMessage(contentId);
    }

    public String getLocalizedStatusMessage(String messageCode) {
        return getLocalizedStatusMessage(messageCode, userDirectoryService.getCurrentUser().getReference());
    }

    public String getLocalizedStatusMessage(String messageCode, Locale locale) {
        //TODO not sure how to do this with  the sakai resource loader
        return null;
    }

    public String getLocalizedReviewErrorMessage(String contentId) {
        log.debug("Returning review error for content: " + contentId);

        List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);

        if (matchingItems.isEmpty()) {
            log.debug("Content " + contentId + " has not been queued previously");
            return null;
        }

        if (matchingItems.size() > 1) {
            log.debug("more than one matching item found - using first item found");
        }

        //its possible the error code column is not populated
        Integer errorCode = ((ContentReviewItemUrkund) matchingItems.iterator().next()).getErrorCode();
        if (errorCode == null) {
            return ((ContentReviewItemUrkund) matchingItems.iterator().next()).getLastError();
        }
        return getLocalizedStatusMessage(errorCode.toString());
    }

    @Override
    public Map getAssignment(String siteId, String taskId) throws SubmissionException, TransientSubmissionException {
        log.debug("Dummy getAssignment()");
        return null;
    }

    @Override
    public void createAssignment(String siteId, String taskId, Map extraAsnnOpts) throws SubmissionException, TransientSubmissionException {
        log.debug("Dummy createAssignment()");
    }

    private String generateSpoofedSubmitterEmail(User user, String siteId) {
        String spoofedEmail = String.format(URKUND_SPOOFED_EMAIL_TEMPLATE, user.getId(), siteId, spoofEmailContext);
        return spoofedEmail;
    }

    @Override
    public int getReviewWarnings(String contentId) throws QueueException, ReportException, Exception {
        List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);
        if (matchingItems.size() == 0) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.debug("More than one matching item - using first item found");
        }

        ContentReviewItemUrkund item = (ContentReviewItemUrkund) matchingItems.iterator().next();
        if (item.getStatus().compareTo(ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
            log.debug("Report not available: " + item.getStatus());
            throw new ReportException("Report not available: " + item.getStatus());
        }

        return item.getWarnings();
    }

    @Override
    public String getOptOutUrl(String contentId) throws QueueException, ReportException {
        List<ContentReviewItemUrkund> matchingItems = getItemsByContentId(contentId);
        if (matchingItems.size() == 0) {
            log.debug("Content " + contentId + " has not been queued previously");
            throw new QueueException("Content " + contentId + " has not been queued previously");
        }

        if (matchingItems.size() > 1) {
            log.debug("More than one matching item - using first item found");
        }

        ContentReviewItemUrkund item = (ContentReviewItemUrkund) matchingItems.iterator().next();
        if (item.getStatus().compareTo(ContentReviewItem.SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
            log.debug("Report not available: " + item.getStatus());
            throw new ReportException("Report not available: " + item.getStatus());
        }

        return item.getOptOutUrl();
    }

}
