/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.core.qos.rss;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.qos.metrics.DataFeedRegistrationService;
import org.cougaar.core.qos.metrics.QosComponent;

import com.bbn.quo.data.PropertiesDataFeed;

import java.io.InputStream;
import java.net.URI;

public class ConfigFinderDataFeedComponent
    extends QosComponent
{
    public static final String CONFIG_PROTOCOL = "cougaarconfig";


    public ConfigFinderDataFeedComponent() 
    {
    }

    public void load() 
    {
	super.load();

	String urlString = getParameter("url");
	String name = getParameter("name");

	ServiceBroker sb = getServiceBroker();
	LoggingService logging = (LoggingService)
	    sb.getService(this, LoggingService.class, null);

	DataFeedRegistrationService svc = (DataFeedRegistrationService)
	    sb.getService(this, DataFeedRegistrationService.class, null);

	Feed feed = new Feed(urlString);
	svc.registerFeed(feed, name);
	
	// special Feed that has the URL for the sites file
	if (name.equalsIgnoreCase("sites")) {
	    if (logging.isDebugEnabled())
		logging.debug("Populating Sites with  url=" +urlString );
	    svc.populateSites(urlString);
	}
	sb.releaseService(this, DataFeedRegistrationService.class, svc);

    }

    private static class Feed extends PropertiesDataFeed {

	public Feed(String properties_url)
	{
	    super(properties_url);
	}

	protected InputStream openURL(String  urlString) 
	{
	    Logger logging = 
		Logging.getLogger(ConfigFinderDataFeedComponent.class);
	    URI uri = null;
	    try {
		uri = new URI(urlString);
	    } catch (java.net.URISyntaxException ex) {
		// log it
		if (logging.isErrorEnabled())
		    logging.error(null, ex);
		return null;
	    }
	    
	    String scheme = uri.getScheme();
	    String path = uri.getSchemeSpecificPart();

	    if (logging.isDebugEnabled()){
		logging.debug("scheme=" + scheme +
			      " Path=" + path);
	    }


	    if (scheme.equals(CONFIG_PROTOCOL)) {
		try {
		    if (logging.isDebugEnabled())
			logging.debug("Opening configuration URL=" + urlString);
		    ConfigFinder finder = ConfigFinder.getInstance();
		    return finder.open(path);
		} catch (java.io.IOException io_ex) {
		    if (logging.isErrorEnabled())
			logging.error("Could not open Config URL=" +urlString,
				      io_ex);
		    return null;
		}
	    } else {
		if (logging.isDebugEnabled())
		    logging.debug("Opening external URL=" + urlString);
		return super.openURL(urlString);
	    }
	}

    }

}

