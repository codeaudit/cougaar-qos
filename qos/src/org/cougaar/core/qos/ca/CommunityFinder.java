/*
 * <copyright>
 *  Copyright 1997-2001 Networks Associates Technology, Inc.
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
 * Created on September 12, 2001, 10:55 AM
 */
 
package org.cougaar.core.qos.ca;


import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.community.Community;
import org.cougaar.core.service.community.CommunityResponse;
import org.cougaar.core.service.community.CommunityResponseListener;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.community.CommunityChangeListener;
import org.cougaar.core.service.community.CommunityChangeEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

abstract public class CommunityFinder
    extends Observable
    implements CommunityResponseListener, CommunityChangeListener
	       
{
    private String community_name;
    private Community community;

    protected String filter;
    protected CommunityService svc;
    protected Logger logger;

    public CommunityFinder(CommunityService svc,
			   String filter)
    {
	this.svc = svc;
	this.filter = filter;
	logger = Logging.getLogger("org.cougaar.core.qos.ca.CommunityFinder");
    }


    abstract public void postQuery();

    protected void go()
    {
	svc.addListener(this);
    }

    public void communityChanged(CommunityChangeEvent e) 
    {
	if (logger.isDebugEnabled())
	    logger.debug("CommunityChangeEvent " + e);
	boolean repost; // only notify observers once
	synchronized (this) {
	    repost = this.community_name == null;
	}
	if (repost) postQuery();
    }

    private void foundCommunity(String community_found,
				Community community) 
    {
	if (logger.isDebugEnabled())
	    logger.debug("Community = " + community_found);
	boolean notify; // only notify observers once
	synchronized (this) {
	    notify = this.community_name == null;
	    if (notify) {
		this.community_name = community_found;
		if (community != null) {
		    this.community = community;
		} else {
		    // find the actual community
		    CommunityResponseListener crl = 
			new CommunityResponseListener () {
			    public void getResponse(CommunityResponse response)
			    {
				Object xxx = response.getContent();
				if (xxx instanceof Community) {
				    CommunityFinder.this.community = (Community)
					xxx;
				} else {
				    ; // ???
				}
			    }
			};
		    this.community = svc.getCommunity(community_name, crl);
		}
	    }
	}
	if (notify) {
	    setChanged();
	    notifyObservers(community_name);
	    clearChanged();
	    if(logger.isDebugEnabled())
		logger.debug(countObservers() + 
			     " observers notified that community=" 
			     +community_name);
	}
    }

    protected void handleResponse(Object candidate)
    {
	if (logger.isDebugEnabled())
		    logger.debug("Response was candidate "+ candidate);
	svc.removeListener(this);
	if (candidate instanceof Community) {
	    Community comm = (Community) candidate;
	    foundCommunity(comm.getName(), comm);
	} else if (candidate instanceof String) {
	    foundCommunity((String) candidate, null);
	} else {
	    if (logger.isErrorEnabled())
		logger.error("Response was " +candidate+
			     " of class " +candidate.getClass());
	}
    }

    // This is only called in response to a match of the exact
    // communuty type and manager attribute, so there better not be
    // more than one entry.
    protected void handleCollectionResponse(Collection result) {
	int count = result.size();
	if (count > 0) {
	    handleResponse(result.iterator().next());
	    if (count > 1 && logger.isWarnEnabled()) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(filter);
		buffer.append(" matched ");
		buffer.append(Integer.toString(count));
		buffer.append(" communities: ");
		Iterator itr = result.iterator();
		while (itr.hasNext()) {
		    buffer.append(itr.next());
		    buffer.append (" ");
		}
		logger.warn(buffer.toString());
	    }
	} else {
	    if (logger.isDebugEnabled())
		logger.debug("CommunityResponse is empty");
	}
    }

    public void getResponse(CommunityResponse response) 
    {
	if (logger.isDebugEnabled())
	     logger.debug("CommunityResponse " +response.getStatus());
	if (response.getStatus() == CommunityResponse.SUCCESS) {
	    Collection result = (Collection) response.getContent();
	    handleCollectionResponse(result);
	}
	    
    }

    public String getCommunityName() 
    {
	if (logger.isDebugEnabled()) 
	    logger.debug("getCommunityName() -> " + community_name);
	return community_name;
    }

    public Community getCommunity()
    {
	return community;
    }


    public static class ForAny extends CommunityFinder {
	public ForAny(CommunityService svc, String filter)
	{
	    super(svc, filter);
	    go();
	}

	public void postQuery() {
	    if (logger.isDebugEnabled())
		    logger.debug("Posted Manger Subscription filter="+
				 filter);
	    Collection results = svc.searchCommunity(null, filter, true, 
						     Community.COMMUNITIES_ONLY,
						     this);
	    if (results != null) {
		handleCollectionResponse(results);
	    }
	}
    }


    public static class ForAgent extends CommunityFinder {
	private MessageAddress agentID;

	public ForAgent(CommunityService svc, 
			String filter, 
			MessageAddress agentID)
	{
	    super(svc, filter);
	    this.agentID = agentID;
	    go();
	}

	public void postQuery() {
	    if (logger.isDebugEnabled())
		    logger.debug("Posted Member Subscription filter="+
				 filter);
	    Collection results = 
		svc.listParentCommunities(agentID.getAddress(), 
					  filter, 
					  this);
	    if (results != null) {
		handleCollectionResponse(results);
	    }
	}
	
    }

    // match one Community attribute
    public static String makeFilter(String attribute, String value)
    {
	return "(" +attribute+ "=" +value+ ")";
    }

    // match two Community attributes
    public static String makeFilter(String attribute1, String value1,
				    String attribute2, String value2)
    {
	return "(&(" 
	    +attribute1+ "=" +value1+ ")("
	    +attribute2+ "=" +value2+ "))";
    }


    // match N Community attributes
    public static String makeFilter(Map attributes)
    {
	StringBuffer buff = new StringBuffer();
	buff.append("(&");
	Iterator itr = attributes.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    buff.append("(");
	    buff.append(entry.getKey());
	    buff.append("=");
	    buff.append(entry.getValue());
	    buff.append(")");
	}
	buff.append(")");
	return buff.toString();
    }
    
}
