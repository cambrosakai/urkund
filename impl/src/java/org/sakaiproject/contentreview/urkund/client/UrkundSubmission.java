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
package org.sakaiproject.contentreview.urkund.client;

import java.io.InputStream;
import org.apache.commons.codec.binary.Base64;


/**
 *
 * @author joer0037
 */
public class UrkundSubmission {

    private String externalId;
    private InputStream document;
    private String filename;
    private String submitterEmail;
    private String subject;
    private String message;
    private boolean anon;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public InputStream getDocument() {
        return document;
    }

    public void setDocument(InputStream document) {
        this.document = document;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getFilenameEncoded() {
        return Base64.encodeBase64String(filename.getBytes());
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAnon() {
        return anon;
    }

    public void setAnon(boolean anon) {
        this.anon = anon;
    }

    @Override
    public String toString() {
        return "UrkundSubmission{" + "externalId=" + externalId + ", document=" + document + ", filename=" + filename + ", filenameEncoded=" + getFilenameEncoded() + ", submitterEmail=" + submitterEmail + ", subject=" + subject + ", message=" + message + ", anon=" + anon + '}';
    }

    /**
     * Finds the urkund supported content type from the filename extension.
     * 
     * @return null if not a supported filename extension
     */
    public String getSupportedContentType() {
        String type = null;
        String ext = "";

        int pos = filename.lastIndexOf(".");
        if (pos > 0) {
            ext = filename.substring(pos + 1);
        }

        if (ext.equalsIgnoreCase("doc")) {
            type = "application/msword";
        }

        if (ext.equalsIgnoreCase("docx")) {
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        if (ext.equalsIgnoreCase("sxw")) {
            type = "application/vnd.sun.xml.writer";
        }
        
        if (ext.equalsIgnoreCase("ppt")) {
            type = "application/vnd.ms-powerpoint";
        }
        
        if (ext.equalsIgnoreCase("pptx")) {
            type = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        if (ext.equalsIgnoreCase("pdf")) {
            type = "application/pdf";
        }
        
        if (ext.equalsIgnoreCase("txt")) {
            type = "text/plain";
        }

        if (ext.equalsIgnoreCase("rtf")) {
            type = "application/rtf";
        }

        if (ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")) {
            type = "text/html";
        }
        
        if (ext.equalsIgnoreCase("wps")) {
            type = "application/vnd.ms-works";
        }

        if (ext.equalsIgnoreCase("odt")) {
            type = "application/vnd.oasis.opendocument.text";
        }

        return type;        
    }
    
}
