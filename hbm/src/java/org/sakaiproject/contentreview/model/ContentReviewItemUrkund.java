/*
 * Copyright 2016 Sakai Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package org.sakaiproject.contentreview.model;

import java.util.Date;

/**
 *
 * @author joer0037
 */
public class ContentReviewItemUrkund extends ContentReviewItem {
    private String reportUrl;
    private String optOutUrl;
    private Integer warnings;

    public ContentReviewItemUrkund() {
    }

    public ContentReviewItemUrkund(String contentId) {
        super(contentId);
    }
    
    public ContentReviewItemUrkund(String userId, String siteId, String taskId, String contentId, String externalId, Date dateQueued, Date dateSubmitted, Date dateReportReceived, long status, Integer reviewScore, String reportUrl, String optOutUrl, Integer warnings) {
        super(userId, siteId, taskId, contentId, externalId, dateQueued, dateSubmitted, dateReportReceived, status, reviewScore);
        this.reportUrl = reportUrl;
        this.optOutUrl = optOutUrl;
        this.warnings = warnings;
    }

    public ContentReviewItemUrkund(String userId, String siteId, String taskId, String contentId, Date dateQueued, Long status, String reportUrl, String optOutUrl, Integer warnings) {
        super(userId, siteId, taskId, contentId, dateQueued, status);
        this.reportUrl = reportUrl;
        this.optOutUrl = optOutUrl;
        this.warnings = warnings;
    }

    public ContentReviewItemUrkund(String contentId, String reportUrl, String optOutUrl, Integer warnings) {
        super(contentId);
        this.reportUrl = reportUrl;
        this.optOutUrl = optOutUrl;
        this.warnings = warnings;
    }
    
    
    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }
    
    public String getOptOutUrl() {
        return optOutUrl;
    }

    public void setOptOutUrl(String optOutUrl) {
        this.optOutUrl = optOutUrl;
    }
    public Integer getWarnings() {
        return warnings;
    }

    public void setWarnings(Integer warnings) {
        this.warnings = warnings;
    }
}
