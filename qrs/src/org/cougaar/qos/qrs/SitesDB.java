/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;

import java.net.URL;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * This is really just an interface to the db stored in
 * RSSNetUtilities.SiteAddress.  */
public class SitesDB implements Constants
{
    private static final String MAGIC = "Site_Flow_";
    private static final int MAGIC_LENGTH = MAGIC.length();

    private Logger logger;

    public SitesDB() 
    {
	logger = Logging.getLogger(SitesDB.class);
    }

    public SiteAddress lookup (String address) 
    {
	Enumeration e = SiteAddress.elements();
	while (e.hasMoreElements()) {
	    SiteAddress site = 	(SiteAddress) e.nextElement();
	    if (site.contains(address) ) return site;
	}
	return null;
    }


    private void list () {
	logger.error("Listing Sites:");
	Enumeration e = SiteAddress.elements();
	while (e.hasMoreElements()) {
	    SiteAddress site = (SiteAddress) e.nextElement();
	    logger.error(site);
	}
    }



    private void addPropertyKey(String key) {
	if (key.startsWith(MAGIC)) {
	    int start = MAGIC_LENGTH;
	    int end = key.indexOf(KEY_SEPR, start);
	    if (end != -1) {
		String mask = key.substring(start, end);
		SiteAddress.getSiteAddress(mask);
		start = end+1;
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
	}
	catch (java.io.IOException ex) {
	    logger.error(null, ex);
	    return;
	}
    }

    public void populate(URL url) {
	try {
	    InputStream stream = url.openStream();
	    populate(stream);
	}
	catch (java.io.IOException ex) {
	    logger.error(null, ex);
	    return;
	}
    }




}


