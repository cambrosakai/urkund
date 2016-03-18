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

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author joer0037
 */
public class LoggingFilter implements ClientRequestFilter {

    private static final Log log = LogFactory.getLog(LoggingFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        StringBuilder sb = new StringBuilder();
        MultivaluedMap<String, String> headers = requestContext.getStringHeaders();

        sb.append("URI:").append('\n');
        sb.append(requestContext.getUri());

        sb.append("\nHeaders:").append('\n');
        for (Object key : headers.keySet()) {
            sb.append(key + "=" + headers.get(key)).append('\n');
        }

        if (requestContext.getEntity() != null) {
            sb.append("\nContent:").append('\n');
            sb.append(requestContext.getEntity().toString()).append('\n');
        }

        log.debug(sb);
    }

}
