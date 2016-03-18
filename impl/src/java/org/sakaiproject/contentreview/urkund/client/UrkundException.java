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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author joer0037
 */
public class UrkundException extends RuntimeException {

    private ErrorInfoResponse errorInfo;
    private int httpCode;

    private static final Log log = LogFactory.getLog(UrkundException.class);

    public UrkundException(ErrorInfoResponse errorInfo, int httpCode) {
        super(errorInfo.message);
        this.errorInfo = errorInfo;
        this.httpCode = httpCode;
        
        log.error("Urkund response error (http code " + httpCode + "): " + errorInfo.message);
    }

    @Override
    public String getLocalizedMessage() {
        return errorInfo.localisedMessage;
    }

    @Override
    public String getMessage() {
        return errorInfo.message;
    }
    
    public int getHttpCode() {
        return httpCode;
    }
    
}
