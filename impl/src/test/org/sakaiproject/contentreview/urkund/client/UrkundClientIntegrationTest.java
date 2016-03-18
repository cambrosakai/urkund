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
import static org.easymock.EasyMock.*;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 *
 * @author joer0037
 * Tests creating actual requests to urkund
 */
@RunWith(EasyMockRunner.class) 
public class UrkundClientIntegrationTest extends EasyMockSupport {

    @Mock
    ServerConfigurationService serverConfigurationService;

    @TestSubject
    final UrkundClientImpl client = new UrkundClientImpl();
    
    final String EID = "EID_1";
    
    final UrkundClient dummyClient = new UrkundClientMock();

    
    @Before
    public void setUp() {
        expect(serverConfigurationService.getString("urkund.service.url")).andReturn("https://secure.urkund.com/api/rest");
        expect(serverConfigurationService.getString("urkund.service.username")).andReturn("change-this-username");
        expect(serverConfigurationService.getString("urkund.service.password")).andReturn("change-this-password");
        expect(serverConfigurationService.getString("urkund.service.receiver")).andReturn("change-this-email");
        expect(serverConfigurationService.getString("urkund.lang", "en-US")).andReturn("sv-SE");

        replay(serverConfigurationService);

        client.init();
        
        verify(serverConfigurationService);
    }

    /**
     * Test of submitDocument method, of class UrkundClientImpl.
     */
    //@Test
    public void testSubmitDocument() {
        System.out.println("submitDocument with externalId: " + EID);
        UrkundSubmission submission = new UrkundSubmission();
        
        submission.setFilename("testdoc1.docx");
        submission.setDocument(TestUtil.readFile("/files/testdoc1.docx"));

        submission.setAnon(false);
        submission.setExternalId(EID);
        submission.setMessage("an_example_message");
        submission.setSubject("an_example_subject");
        submission.setSubmitterEmail("submitter-email@domain.com");

        SubmissionsResponse response = client.submitDocument(submission);
        assertNotNull(response);
        assertEquals(EID, response.externalId);
        assertEquals("Submitted", response.status.state);
        
        System.out.println(response);
    }

    /**
     * Test of getReports method, of class UrkundClientImpl.
     */
    //@Test
    public void testGetReports() {
        System.out.println("getReports with externalId: " + EID);
        
        List<SubmissionsResponse> reports = client.getReports(EID);
        assertTrue(reports.size() > 0);
        assertEquals(reports.get(0).externalId, EID);
        
        System.out.println(reports.get(0));
    }

    @Test
    public void setupTest() {
        // Just to make maven test happy
    }
}
