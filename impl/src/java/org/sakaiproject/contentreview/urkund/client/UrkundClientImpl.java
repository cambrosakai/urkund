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

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import org.sakaiproject.component.api.ServerConfigurationService;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author joer0037
 */
public class UrkundClientImpl implements UrkundClient {

    private Client client;
    private WebTarget baseTarget;
    private String receiver;
    private String language;

    private ServerConfigurationService serverConfigurationService;

    private static final Log log = LogFactory.getLog(UrkundClientImpl.class);

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void init() {

        String serviceUrl = serverConfigurationService.getString("urkund.service.url");
        String user = serverConfigurationService.getString("urkund.service.username");
        String pass = serverConfigurationService.getString("urkund.service.password");
        receiver = serverConfigurationService.getString("urkund.service.receiver");
        language = serverConfigurationService.getString("urkund.lang", "en-US");

        System.setProperty("javax.ws.rs.client.ClientBuilder", "org.apache.cxf.jaxrs.client.spec.ClientBuilderImpl");
        System.setProperty("javax.ws.rs.ext.RuntimeDelegate", "org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl");

        client = ClientBuilder.newClient()
                .register(new Authenticator(user, pass))
                .register(new JacksonJsonProvider())
                .register(new LoggingFilter());

        baseTarget = client.target(serviceUrl).path("submissions/{receiver}/{externalId}");
    }

    private Builder createRequest(WebTarget target, String receiver, String externalId) {
        return target
                .resolveTemplate("receiver", receiver)
                .resolveTemplate("externalId", externalId)
                .request(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public SubmissionsResponse submitDocument(UrkundSubmission submission) throws UrkundException {
        log.info("submitDocument() with submission:\n" + submission);

        Builder req = createRequest(baseTarget, receiver, submission.getExternalId())
                .header(HttpHeaders.ACCEPT_LANGUAGE, language)
                .header(HttpHeaders.CONTENT_TYPE, submission.getSupportedContentType())
                .header("x-urkund-filename", submission.getFilenameEncoded())
                .header("x-urkund-submitter", submission.getSubmitterEmail())
                .header("x-urkund-subject", submission.getSubject())
                .header("x-urkund-message", submission.getMessage())
                .header("x-urkund-anonymous", submission.isAnon());

        Response resp = req.post(Entity.entity(submission.getDocument(), submission.getSupportedContentType() /*MediaType.APPLICATION_OCTET_STREAM_TYPE*/));
        SubmissionsResponse subResponse = null;

        if (resp.getStatus() != 202) {
            log.error("Error when submitting document.");
            parseError(resp);
        } else {
            subResponse = resp.readEntity(SubmissionsResponse.class);
        }

        return subResponse;
    }

    private void parseError(Response resp) throws UrkundException {
        ErrorInfoResponse respError = new ErrorInfoResponse("Unknown urkund response error", "");
        try {
            respError = resp.readEntity(ErrorInfoResponse.class);
        } catch (Exception e) {
            respError = new ErrorInfoResponse(resp.readEntity(String.class), "");
        } finally {
            throw new UrkundException(respError, resp.getStatus());
        }
    }

    @Override
    public List<SubmissionsResponse> getReports(String externalId) throws UrkundException {
        log.info("getReports() with: externalId=" + externalId);

        Response resp = createRequest(baseTarget, receiver, externalId)
                .header("Accept-Language", language)
                .get();

        List<SubmissionsResponse> subResponse = new ArrayList<SubmissionsResponse>();

        if (resp.getStatus() != 200) {
            log.error("Error when getting reports.");
            parseError(resp);
        } else {
            subResponse = resp.readEntity(new GenericType<List<SubmissionsResponse>>() {
            });
        }

        return subResponse;
    }

}
