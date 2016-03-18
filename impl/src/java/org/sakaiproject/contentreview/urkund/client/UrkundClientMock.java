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

import java.util.ArrayList;
import java.util.List;


public class UrkundClientMock implements UrkundClient {

    @Override
    public List<SubmissionsResponse> getReports(String externalId) throws UrkundException {
        char lastChar = externalId.charAt(externalId.length());
        boolean accepted = lastChar > '7';
        float sign = accepted ? 0.3f : 0.5f;
                
        SubmissionsResponse response = new SubmissionsResponse(99, externalId, "a_mocked_filename.doc", "application/octet-stream", "2015-08-24T22:33:44+1", 
                new SubmissionsResponse.Status(accepted ? "Accepted" : "Analyzed", "Status message"), 
                new SubmissionsResponse.Document(false, "2015-08-24T33:44:55+1", "https://api.urkund.se/download_report", 
                        new SubmissionsResponse.Document.OptOutInfo("https://api.urkund.se/optout_document", "Optout description"), 101),
                new SubmissionsResponse.Report(lastChar-20, "https://api.urkund.se/report_url", sign, lastChar-30, 102),
                "Mock subject", "Mock message", false);
        
        ArrayList<SubmissionsResponse> list = new ArrayList<SubmissionsResponse>();
        list.add(response);
        return list;
    }

    @Override
    public SubmissionsResponse submitDocument(UrkundSubmission submission) throws UrkundException {
        SubmissionsResponse response = new SubmissionsResponse(99, submission.getExternalId(), submission.getFilename(), "application/octet-stream", "2015-08-24T22:33:44+1", 
                new SubmissionsResponse.Status( "Status message", "Submitted"), 
                new SubmissionsResponse.Document(false, "2015-08-24T33:44:55+1", "https://api.urkund.se/download_report", 
                        new SubmissionsResponse.Document.OptOutInfo("https://api.urkund.se/optout_document", "Optout description"), 101),
                null,
                submission.getSubject(), submission.getMessage(), false);
        
        return response;
    }
    
}
