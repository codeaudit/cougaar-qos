/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>

 */

package org.cougaar.qos.qrs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.cougaar.util.log.Logger;

/**
 * This is really just an interface to the db stored in
 * RSSNetUtilities.SiteAddress.
 */
public class SitesDB implements Constants {
    private static final String MAGIC = "Site_Flow_";
    private static final int MAGIC_LENGTH = MAGIC.length();

    private final Logger logger;

    public SitesDB() {
        logger = Logging.getLogger(SitesDB.class);
    }

    public SiteAddress lookup(String address) {
        for (SiteAddress site : SiteAddress.elements()) {
            if (site.contains(address)) {
                return site;
            }
        }
        return null;
    }

    private void addPropertyKey(String key) {
        if (key.startsWith(MAGIC)) {
            int start = MAGIC_LENGTH;
            int end = key.indexOf(KEY_SEPR, start);
            if (end != -1) {
                String mask = key.substring(start, end);
                SiteAddress.getSiteAddress(mask);
                start = end + 1;
                end = key.indexOf(KEY_SEPR, start);
                if (end != -1) {
                    mask = key.substring(start, end);
                    SiteAddress.getSiteAddress(mask);
                }
            }
        }
    }

    public void populate(InputStream stream) {
        try {
            InputStreamReader isr = new InputStreamReader(stream);
            BufferedReader rdr = new BufferedReader(isr);
            String key = rdr.readLine();
            while (key != null) {
                addPropertyKey(key);
                key = rdr.readLine();
            }
            rdr.close();
        } catch (java.io.IOException ex) {
            logger.error(null, ex);
            return;
        }
    }

    public void populate(URL url) {
        try {
            InputStream stream = url.openStream();
            populate(stream);
        } catch (java.io.IOException ex) {
            logger.error(null, ex);
            return;
        }
    }

}
