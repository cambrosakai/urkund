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

/**
 *
 * Error Response from GET and POST https://secure.urkund.com/api/rest/submissions/{RECEIVER}/{EXTERNALID}
 * @author joer0037
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public final class ErrorInfoResponse {
    public final String message;
    public final String localisedMessage;

    @JsonCreator
    public ErrorInfoResponse(@JsonProperty("Message") String message, @JsonProperty("LocalisedMessage") String localisedMessage){
        this.message = message;
        this.localisedMessage = localisedMessage;
    }

}