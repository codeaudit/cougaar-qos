/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

/**
 * A DataFeed which returns fixed values that it reads from a java
 * properties data in a url.  Handy for supplying defaults.  */
public class PropertiesDataFeed extends AbstractDataFeed implements Constants
{
    private static final long PROPERTIES_TIME = System.currentTimeMillis();
    private static final double DEFAULT_VALUE = 0.0;
    private static final double FEED_DEFAULT_CREDIBILITY = 
	SYS_DEFAULT_CREDIBILITY;

    private Properties props = new Properties();
    private String urlString;
    private Logger logger;
    /**
     * The args should be command-line style, and should include the
     * '-url' followed by the url of the property data.  These args
     * would typically appear in a kernel config file. */
    public PropertiesDataFeed(String[] args) {
	super();
	
	logger = Logging.getLogger(PropertiesDataFeed.class);

	if (args == null) {
	    logger.error(this.getName() +":PropertiesDataFeed: no args");
	    return;
	}

	String url = null;
	int i=0;
	for (i=0; i<args.length; i++) {
	    String arg = args[i];
	    if (arg.equals("-url"))
		url = args[++i];
	    else
		logger.error(this.getName() +
				   ":PropertiesDataFeed: unknown argument " 
				   + arg);
	}

	initialize(url);
    }

    public PropertiesDataFeed(String properties_url) {
	logger = Logging.getLogger(PropertiesDataFeed.class);
	initialize(properties_url);
    }

    protected InputStream openURL(String urlString) {
	URL url;
	try {
	    url = new URL(urlString);
	}
	catch (java.net.MalformedURLException bad_url) {
	    logger.error(urlString +":PropertiesDataFeed:", bad_url);
	    return null;
	}

	try {
	    return url.openStream();
	}
	catch (java.io.IOException open_error) {
	    logger.error(url +":PropertiesDataFeed:", open_error);
	    return null;
	}
	
    }

    private void initialize(String properties_url) {
	
	if (properties_url == null) {
	    logger.error(":PropertiesDataFeed: no URL");
	    return;
	}

	InputStream stream = openURL(properties_url);
	if (stream == null) return;

	// good url
	urlString=properties_url;

	try {
	    props.load(stream);
	}
	catch (java.io.IOException load_error) {
	    logger.error(properties_url +":PropertiesDataFeed:", load_error);
	}

	try {
	    stream.close();
	}
	catch (java.io.IOException close_error) {
	    logger.error(properties_url +"PropertiesDataFeed:", close_error);
	}

	if (logger.isDebugEnabled()) {
	    StringWriter raw = new StringWriter();
	    PrintWriter writer = new PrintWriter(raw);
	    writer.print(properties_url);
	    writer.print(" Contents:");
	    props.list(writer);
	    logger.debug(raw.toString());
	    writer.close();
	}
	
    }
    
    // no-op
    public void removeListenerForKey(DataFeedListener listener, String key) {
    }

    
    // no-op
    public void addListenerForKey(DataFeedListener listener, String key) {
    }

    public DataValue lookup(String key) {
	double value = DEFAULT_VALUE;
	double credibility = FEED_DEFAULT_CREDIBILITY;
	String provenance = null;
	String units = null;
	String value_string = props.getProperty(key +KEY_SEPR+ "value");
	if (value_string != null) {
	    try {
		value = Double.parseDouble(value_string);
	    } 
	    catch (NumberFormatException bad_value) {
		logger.error(this.getName() +":PropertiesDataFeed:", bad_value);
	    }

	    String credibility_string = 
		props.getProperty(key +KEY_SEPR+ "credibility");
	    // no credibility -> use the default credibility 
	    if (credibility_string != null) {
		try {
		    credibility = Double.parseDouble(credibility_string);
		} 
		catch (NumberFormatException bad_credibility) {
		    logger.error(this.getName() +":PropertiesDataFeed:",
				 bad_credibility);
		}
	    } else {
		if (logger.isDebugEnabled())
		    logger.debug(this.getName() +
				       ":PropertiesDataFeed for " 
				       + urlString +
				       ": no credibility for "
				       + key);
	    }
	    provenance = props.getProperty(key +KEY_SEPR+ "provenance", 
					   urlString);
	    units = props.getProperty(key +KEY_SEPR+ "units");
	} else {
	    // no value in Properties -> key is invalid
	    if (logger.isDebugEnabled())
		logger.debug(this.getName() +
				   ":PropertiesDataFeed for " + urlString +
				   ": no value for " + key);
	    credibility = 0.0;
	}

	return new DataValue(new Double(value), credibility, units, provenance,
			      PROPERTIES_TIME, 0l);
	

    }

}





