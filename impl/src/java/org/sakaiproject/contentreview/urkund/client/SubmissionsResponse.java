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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * Response from GET and POST https://secure.urkund.com/api/rest/submissions/{RECEIVER}/{EXTERNALID}
 * GET will get an array of SubmissionsResponse
 *
 * Generated at http://timboudreau.com/blog/json/read
 * @author joer0037
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public final class SubmissionsResponse {

    public final long id;
    public final String externalId;
    public final String filename;
    public final String mimeType;
    public final String timestamp;
    public final Status status;
    public final Document document;
    public final Report report;
    public final String subject;
    public final String message;
    public final boolean anonymous;

    @JsonCreator
    public SubmissionsResponse(@JsonProperty("Id") long id, @JsonProperty("ExternalId") String externalId, @JsonProperty("Filename") String filename, @JsonProperty("MimeType") String mimeType, @JsonProperty("Timestamp") String timestamp, @JsonProperty("Status") Status status, @JsonProperty("Document") Document document, @JsonProperty("Report") Report report, @JsonProperty("Subject") String subject, @JsonProperty("Message") String message, @JsonProperty("Anonymous") boolean anonymous) {
        this.id = id;
        this.externalId = externalId;
        this.filename = filename;
        this.mimeType = mimeType;
        this.timestamp = timestamp;
        this.status = status;
        this.document = document;
        this.report = report;
        this.subject = subject;
        this.message = message;
        this.anonymous = anonymous;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static final class Status {

        public final String message;
        public final String state;

        @JsonCreator
        public Status(@JsonProperty("Message") String message, @JsonProperty("State") String state) {
            this.message = message;
            this.state = state;
        }

        @Override
        public String toString() {
            return "Status{" + "message=" + message + ", state=" + state + '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static final class Document {

        public final boolean anonymous;
        public final String date;
        public final String downloadUrl;
        public final OptOutInfo optOutInfo;
        public final long id;

        @JsonCreator
        public Document(@JsonProperty("Anonymous") boolean anonymous, @JsonProperty("Date") String date, @JsonProperty("DownloadUrl") String downloadUrl, @JsonProperty("OptOutInfo") OptOutInfo optOutInfo, @JsonProperty("Id") long id) {
            this.anonymous = anonymous;
            this.date = date;
            this.downloadUrl = downloadUrl;
            this.optOutInfo = optOutInfo;
            this.id = id;
        }

        @Override
        public String toString() {
            return "Document{" + "anonymous=" + anonymous + ", date=" + date + ", downloadUrl=" + downloadUrl + ", optOutInfo=" + ObjToString(optOutInfo) + ", id=" + id + '}';
        }

        @JsonIgnoreProperties(ignoreUnknown=true)
        public static final class OptOutInfo {

            public final String message;
            public final String url;

            @JsonCreator
            public OptOutInfo(@JsonProperty("Message") String message, @JsonProperty("Url") String url) {
                this.message = message;
                this.url = url;
            }

            @Override
            public String toString() {
                return "OptOutInfo{" + "url=" + url + ", message=..." + '}';
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static final class Report {

        public final long matchCount;
        public final String reportUrl;
        public final float significance;
        public final long sourceCount;
        public final long id;
        public final List<Warnings> warnings;

        @JsonCreator
        public Report(
                @JsonProperty("MatchCount") long matchCount, 
                @JsonProperty("ReportUrl") String reportUrl, 
                @JsonProperty("Significance") float significance, 
                @JsonProperty("SourceCount") long sourceCount, 
                @JsonProperty("Id") long id,
                @JsonProperty("Warnings") List<Warnings> warnings) {
            this.matchCount = matchCount;
            this.reportUrl = reportUrl;
            this.significance = significance;
            this.sourceCount = sourceCount;
            this.id = id;
            this.warnings = warnings;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Report{" + "matchCount=" + matchCount + ", reportUrl=" + reportUrl + ", significance=" + significance + ", sourceCount=" + sourceCount + ", id=" + id );
            for (Warnings warning: this.warnings) {
                sb.append(", ");
                sb.append(warning.toString());
            }
            sb.append('}');
            return sb.toString();
        }
       
        @JsonIgnoreProperties(ignoreUnknown=true)
        public static final class Warnings {

            public final String warningType;
            public final String excerpt;
            public final String message;

            @JsonCreator
            public Warnings(@JsonProperty("WarningType") String warningType, @JsonProperty("Excerpt") String excerpt, @JsonProperty("Message") String message) {
                this.warningType = warningType;
                this.excerpt = excerpt;
                this.message = message;
            }

            @Override
            public String toString() {
                return "Warnings{" + "warningType=" + warningType + ", excerpt=" + excerpt + ", message=" + "..." + '}';
            }
        }        
    }
    
    @Override
    public String toString() {
        return "SubmissionsResponse{" + "id=" + id + ", externalId=" + externalId + ", filename=" + filename + ", mimeType=" + mimeType + ", timestamp=" + timestamp + ", subject=" + subject + ", message=" + message + ", anonymous=" + anonymous + 
                ", status=" + ObjToString(status) + ", document=" + ObjToString(document) + ", report=" + ObjToString(report) + "}";
    }

    static String ObjToString(Object o) {
        if (o != null) {
            return o.toString();
        }
        return "null";
    }
    
}