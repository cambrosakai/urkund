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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * This class contains the implementation of {@link ContentReviewService.isAcceptableContent}. This includes other utility logic for checking the length and
 * correctness of Word Documents and other things.
 *
 * @author sgithens
 *
 */
public class UrkundContentValidator {

    private static final Log log = LogFactory.getLog(UrkundContentValidator.class);

    private int urkund_max_file_size;
    /**
     * Default max allowed filesize - should match urkunds own setting (surrently 20Mb)
     */
    private static int URKUND_DEFAULT_MAX_FILE_SIZE = 20971520;

    private ServerConfigurationService serverConfigurationService;

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void init() {
        urkund_max_file_size = serverConfigurationService.getInt("urkund.maxFileSize", URKUND_DEFAULT_MAX_FILE_SIZE);
    }

    public boolean isAcceptableContent(ContentResource resource) {
        String mime = resource.getContentType();
        ResourceProperties resourceProperties = resource.getProperties();
        String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
        Boolean fileTypeOk = false;

        log.debug("MIME type: " + mime + ", filename: " + fileName);

        if (!mime.isEmpty()
                && (mime.equals("application/msword")
                || mime.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || mime.equals("application/vnd.sun.xml.writer")
                || mime.equals("application/vnd.ms-powerpoint")
                || mime.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                || mime.equals("application/pdf")
                || mime.equals("text/plain")
                || mime.equals("application/rtf")
                || mime.equals("text/html")
                || mime.equals("application/vnd.oasis.opendocument.text"))) {
            fileTypeOk = true;
        }

        if (!fileTypeOk && (fileName.indexOf(".") > 0)) {
            String extension = fileName.substring(fileName.lastIndexOf("."));
            log.debug("file has an extension of " + extension);
            if (extension.equals(".doc")
                    || extension.equals(".docx")
                    || extension.equals(".sxw")
                    || extension.equals(".ppt")
                    || extension.equals(".pptx")
                    || extension.equals(".pdf")
                    || extension.equals(".txt")
                    || extension.equals(".rtf")
                    || extension.equals(".html")
                    || extension.equals(".htm")
                    || extension.equals(".odt")) {
                fileTypeOk = true;
            }
        }

        if (!fileTypeOk) {
            log.info("Unexpected filetype, MIME type: " + mime + ", filename: " + fileName);
            return false;
        }

        if (resource.getContentLength() > urkund_max_file_size) {
            log.debug("File is too big: " + resource.getContentLength());
            return false;
        }

        if (resource.getContentLength() == 0) {
            log.debug("File is empty");
            return false;
        }

        return true;
    }
}
