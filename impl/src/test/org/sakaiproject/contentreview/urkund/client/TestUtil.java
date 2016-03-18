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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joer0037
 */
public class TestUtil {
    
    static public InputStream readFile(String filename) {
        return TestUtil.class.getResourceAsStream(filename);
    }
    
     static public byte[] readFileAsBytes(String filename) {
        URL resource = TestUtil.class.getResource(filename);
        try {
            return Files.readAllBytes(Paths.get(resource.toURI()));
        } catch (URISyntaxException ex) {
            Logger.getLogger(TestUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
