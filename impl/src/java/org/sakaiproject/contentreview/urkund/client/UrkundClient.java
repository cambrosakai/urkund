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

import java.util.List;

/**
 *
 * @author joer0037
 */
public interface UrkundClient {

    List<SubmissionsResponse> getReports(String externalId) throws UrkundException;

    SubmissionsResponse submitDocument(UrkundSubmission submission) throws UrkundException;
    
}
