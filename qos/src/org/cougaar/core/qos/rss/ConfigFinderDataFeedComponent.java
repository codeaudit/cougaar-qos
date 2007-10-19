/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.core.qos.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.cougaar.core.qos.metrics.DataFeedRegistrationService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.LoggingService;
import org.cougaar.qos.qrs.PropertiesDataFeed;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.annotations.Cougaar.Arg;
import org.cougaar.util.annotations.Cougaar.ObtainService;

/**
 * This Components uses the {@link DataFeedRegistrationService} to register an
 * RSS properties feed that uses the {@link ConfigFinder} to resolve the URL.
 */
public class ConfigFinderDataFeedComponent extends QosComponent {
    public static final String CONFIG_PROTOCOL = "cougaarconfig";

    @Arg(name="url")
    public String urlString;
    
    @Arg(name="name")
    public String name;
    
    @ObtainService
    public DataFeedRegistrationService svc;
    
    @ObtainService
    public LoggingService log;
    
    public ConfigFinderDataFeedComponent() {
    }

    public void load() {
        super.load();

        Feed feed = new Feed(urlString);
        svc.registerFeed(feed, name);

        // special Feed that has the URL for the sites file
        if (name.equalsIgnoreCase("sites")) {
            if (log.isDebugEnabled()) {
                log.debug("Populating Sites with  url=" + urlString);
            }
            svc.populateSites(urlString);
        }
        getServiceBroker().releaseService(this, DataFeedRegistrationService.class, svc);

    }

    private static class Feed extends PropertiesDataFeed {

        public Feed(String properties_url) {
            super(properties_url);
        }

        protected InputStream openURI(URI uri) throws IOException {
            String scheme = uri.getScheme();
            String path = uri.getSchemeSpecificPart();

            if (log.isDebugEnabled()) {
                log.debug("scheme=" + scheme + " Path=" + path);
            }

            if (scheme.equals(CONFIG_PROTOCOL)) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Opening configuration URL=" + uri);
                    }
                    ConfigFinder finder = ConfigFinder.getInstance();
                    return finder.open(path);
                } catch (IOException io_ex) {
                    throw new IOException("Could not open Config URL=" + uri);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Opening external URL=" + uri);
                }
                return super.openURI(uri);
            }
        }

    }

}
