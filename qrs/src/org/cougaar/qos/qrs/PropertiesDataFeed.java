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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cougaar.util.log.Logger;

/**
 * A DataFeed which returns fixed values that it reads from a java properties
 * data in a url. Handy for supplying defaults.
 */
public class PropertiesDataFeed extends AbstractDataFeed implements Constants {
    private static final String KEY_SUFFIX = KEY_SEPR + "value";
    private static final double DEFAULT_VALUE = 0.0;
    private static final double FEED_DEFAULT_CREDIBILITY = SYS_DEFAULT_CREDIBILITY;

    protected final Logger log;
    private final Properties props;
    private URI uri;  // conceptually final but Java makes it too hard to declare it
    private long captureTime;

    /**
     * The args should be command-line style, and should include the '-url'
     * followed by the url of the property data. These args would typically
     * appear in a kernel config file.
     */
    public PropertiesDataFeed(String[] args) {
        log = Logging.getLogger(getClass());
        props = new Properties();
        if (args != null) {
            parseArgs(args);
            initialize();
        } else {
            log.error("No args");
        }
    }

    public PropertiesDataFeed(String propertiesUrl) {
        log = Logging.getLogger(PropertiesDataFeed.class);
        props = new Properties();
        if (propertiesUrl == null) {
            log.error(this.getName() + ":PropertiesDataFeed: no url");
            return;
        }
        try {
            uri = new URI(propertiesUrl);
            initialize();
        } catch (URISyntaxException e) {
            log.error("Malformed URL " + propertiesUrl);
        }
    }
    
    protected void parseArgs(String[] args) {
        String urlString = null;
        int i = 0;
        for (i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-url")) {
                urlString = args[++i];
            }
        }
        if (urlString != null) {
            try {
                uri = new URI(urlString);
            } catch (URISyntaxException e) {
                log.error("Malformed URL " + urlString);
            }
        } else {
            log.error("No -url argument");
        }
    }

    protected void initialize() {
       // Done in the initializer thread to guarantee that values will be available
       // for lookup.
       capture();
    }
    
    // This is called out as a method so that it can be
    // overridden, for example to handle ConfigFinder URIs.
    protected InputStream openURI(URI uri) throws IOException {
        try {
            return uri.toURL().openStream();
        } catch (MalformedURLException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    protected Properties capture() {
        if (uri == null) {
            log.error(":PropertiesDataFeed: no URL");
            return null;
        }

        InputStream stream = null;

        try {
            stream = openURI(uri);
            props.clear();
            captureTime = System.currentTimeMillis();
            synchronized (props) {
                props.load(stream);
            }
            if (log.isDebugEnabled()) {
                StringWriter raw = new StringWriter();
                PrintWriter writer = new PrintWriter(raw);
                writer.print(uri);
                writer.print(" Contents:");
                props.list(writer);
                log.debug(raw.toString());
                writer.close();
            }
        } catch (IOException load_error) {
            log.error("URI="+ uri + " :PropertiesDataFeed: " +load_error.getMessage());
            log.debug(null, load_error);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // don't care
                }
            }
        }
        return props;
    }

    protected Map<String, DataValue> collectValues() {
        Map<String, DataValue> keys = new HashMap<String, DataValue>();
        synchronized (props) {
            for (Object key : props.keySet()) {
                String keyString = (String) key;
                if (keyString.endsWith(KEY_SUFFIX)) {
                    int end = keyString.length() - KEY_SUFFIX.length();
                    String baseKey = keyString.substring(0, end);
                    keys.put(baseKey, lookup(baseKey));
                }
            }
        }
        return keys;
    }
    // no-op
    public void removeListenerForKey(DataFeedListener listener, String key) {
    }

    // no-op
    public void addListenerForKey(DataFeedListener listener, String key) {
    }

    public DataValue lookup(String key) {
        double value;
        double credibility;
        String provenance;
        String units;
        synchronized (props) {
            value = DEFAULT_VALUE;
            credibility = FEED_DEFAULT_CREDIBILITY;
            provenance = null;
            units = null;
            String value_string = props.getProperty(key + KEY_SUFFIX);
            if (value_string != null) {
                try {
                    value = Double.parseDouble(value_string);
                } catch (NumberFormatException bad_value) {
                    log.error(this.getName() + ":PropertiesDataFeed:", bad_value);
                }

                String credibility_string = props.getProperty(key + KEY_SEPR + "credibility");
                // no credibility -> use the default credibility
                if (credibility_string != null) {
                    try {
                        credibility = Double.parseDouble(credibility_string);
                    } catch (NumberFormatException bad_credibility) {
                        log.error(this.getName() + ":PropertiesDataFeed:", bad_credibility);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(this.getName() + ":PropertiesDataFeed for " + uri
                                + ": no credibility for " + key);
                    }
                }
                provenance = props.getProperty(key + KEY_SEPR + "provenance", uri.toString());
                units = props.getProperty(key + KEY_SEPR + "units");
            } else {
                // no value in Properties -> key is invalid
                if (log.isDebugEnabled()) {
                    log.debug(this.getName() + ":PropertiesDataFeed for " + uri
                            + ": no value for " + key);
                }
                credibility = 0.0;
            }
        }
        return new DataValue(new Double(value), credibility, units, provenance, captureTime, 0l);

    }
}
