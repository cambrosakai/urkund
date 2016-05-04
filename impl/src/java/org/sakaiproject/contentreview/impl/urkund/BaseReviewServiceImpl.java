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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.impl.ContentReviewDao;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

public abstract class BaseReviewServiceImpl implements ContentReviewService {

    String defaultAssignmentName = null;

    private static final Log log = LogFactory
            .getLog(BaseReviewServiceImpl.class);

    private ContentReviewDao dao;

    public void setDao(ContentReviewDao dao) {
        this.dao = dao;
    }

    ToolManager toolManager;

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    private UserDirectoryService userDirectoryService;

    public void setUserDirectoryService(
            UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void queueContent(String userId, String siteId, String taskId, List<ContentResource> content)
            throws QueueException {

        if (content == null || content.size() < 1) {
            return;
        }

        for (ContentResource contentRes : content) {
            queueContent(userId, siteId, taskId, contentRes.getId());
        }
    }

    public abstract void queueContent(String userId, String siteId, String taskId, String contentId) throws QueueException;

    public boolean allowResubmission() {
        return true;
    }

    // All of the following methods should be extended, and these versions should never be called
    public boolean isSiteAcceptable(Site s) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void checkForReports() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getIconUrlforScore(Long score, Long warnings) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getReviewReport(String contentId) throws QueueException, ReportException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getReviewReportInstructor(String contentId) throws QueueException, ReportException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getReviewReportStudent(String contentId) throws QueueException, ReportException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getServiceName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isAcceptableContent(ContentResource resource) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void processQueue() {
        throw new UnsupportedOperationException("Not implemented");
    }

}
