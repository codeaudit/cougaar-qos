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


import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.community.Community;
import org.cougaar.core.service.community.CommunityResponse;
import org.cougaar.core.service.community.CommunityResponseListener;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.community.CommunityChangeListener;
import org.cougaar.core.service.community.CommunityChangeEvent;
import org.cougaar.core.service.community.Entity;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

/**
 * This utility class helps simplify Community lookup.  For now it
 * comes in two variants, which are inner class extensions.
 */
abstract public class CommunityFinder
    extends Observable
    implements CommunityResponseListener, CommunityChangeListener
	       
{
    private String community_name;
    private Community community;

    protected String filter;
    protected UnaryPredicate predicate;
    protected CommunityService svc;
    protected Logger logger;

    public CommunityFinder(CommunityService svc,
			   String filter,
			   UnaryPredicate predicate)
    {
	this.svc = svc;
	this.filter = filter;
	this.predicate = predicate;
	logger = Logging.getLogger("org.cougaar.core.qos.ca.CommunityFinder");
    }


    abstract public void postQuery();

    protected void go()
    {
	svc.addListener(this);
    }

    public synchronized void communityChanged(CommunityChangeEvent e) 
    {
	if (logger.isDebugEnabled())
	    logger.debug("CommunityChangeEvent " + e);
	boolean repost; // only notify observers once
	repost = this.community_name == null;
	if (repost) postQuery();
    }

    private void getCommunity(String community_name, Community community)
    {
	if (community != null) {
	    this.community = community;
	    return;
	}

	CommunityResponseListener crl = 
	    new CommunityResponseListener () {
		public void getResponse(CommunityResponse response)
		{
		    Object xxx = response.getContent();
		    if (xxx instanceof Community) {
			CommunityFinder.this.community = (Community) xxx;
		    }
		}
	    };
	this.community = svc.getCommunity(community_name, crl);
    }

    private boolean shouldNotify(Community community)
    {
	return predicate == null || predicate.execute(community);
    }

    private synchronized boolean foundCommunity(String community_name,
						Community community) 
    {
	if (logger.isDebugEnabled())
	    logger.debug("Community = " + community_name);

	if (this.community_name == null) {
	    getCommunity(community_name, community); 
	    if (!shouldNotify(this.community)) {
		this.community = null;
		return false;
	    }
	    
	    this.community_name = community_name;
	    setChanged();
	    notifyObservers(community_name);
	    clearChanged();
	    if(logger.isDebugEnabled())
		logger.debug(countObservers() + 
			     " observers notified that community=" 
			     +community_name);
	}
	return true;

    }

    protected void handleResponse(Object candidate)
    {
	if (logger.isDebugEnabled())
		    logger.debug("Response was candidate "+ candidate);
	boolean remove = false;
	if (candidate instanceof Community) {
	    Community comm = (Community) candidate;
	    remove = foundCommunity(comm.getName(), comm);
	} else if (candidate instanceof String) {
	    remove = foundCommunity((String) candidate, null);
	} else {
	    if (logger.isErrorEnabled())
		logger.error("Response was " +candidate+
			     " of class " +candidate.getClass());
	}
	if (remove) svc.removeListener(this);

    }

    protected void handleCollectionResponse(Collection result) 
    {
	Iterator itr = result.iterator();
	while (itr.hasNext()) handleResponse(itr.next());
    }

    public void getResponse(CommunityResponse response) 
    {
	if (logger.isDebugEnabled())
	     logger.debug("CommunityResponse callback " +response.getStatus());
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


    /**
     * This variant of CommunityFinder is useful for finding
     * Communities without assuming any particular Agent is a member.
     * A filter supplied as a constructor parameter describes the
     * community.
     */
    public static class ForAny extends CommunityFinder {
	public ForAny(CommunityService svc, 
		      String filter, 
		      UnaryPredicate predicate)
	{
	    super(svc, filter, predicate);
	    go();
	}

	public void postQuery() {
	    if (logger.isDebugEnabled())
		    logger.debug("Posted Manager Subscription filter="+
				 filter);
	    Collection results = svc.searchCommunity(null, filter, true, 
						     Community.COMMUNITIES_ONLY,
						     this);
	    if (results != null) {
		handleCollectionResponse(results);
	    }
	}
    }


    /**
     * This variant of CommunityFinder is useful for finding
     * Communities of which a given Agent is a member.  A filter
     * supplied as a constructor parameter describes the community.
     */
    public static class ForAgent extends CommunityFinder {
	private MessageAddress agentID;

	public ForAgent(CommunityService svc, 
			String filter, 
			UnaryPredicate predicate,
			MessageAddress agentID)
	{
	    super(svc, filter, predicate);
	    this.agentID = agentID;
	    go();
	}

	public void postQuery() {
	    if (logger.isDebugEnabled())
		    logger.debug("Posted Member Subscription filter="+
				 filter);
	    Collection results = 
		svc.listParentCommunities(agentID.getAddress(), 
					  filter);
	    if (results != null) {
		handleCollectionResponse(results);
	    }
	}
	
    }

    /**
     * Constructs a filter that matches one Community attribute.
     */
    public static String makeFilter(String attribute, String value)
    {
	return "(" +attribute+ "=" +value+ ")";
    }

    /**
     * Constructs a filter that matches two Community attributes.
     */
    public static String makeFilter(String attribute1, String value1,
				    String attribute2, String value2)
    {
	return "(&(" 
	    +attribute1+ "=" +value1+ ")("
	    +attribute2+ "=" +value2+ "))";
    }


    /**
     * Constructs a filter that matches N Community attributes.
     */
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
    


    private static class MemberHasRoleP implements UnaryPredicate
    {
	String entity_name;
	String role;
	MemberHasRoleP(String entity_name, String role)
	{
	    this.entity_name = entity_name;
	    this.role = role;
	}

	public boolean execute(Object comm)
	{
	    Community community = (Community) comm;
	    Logger log = 
		Logging.getLogger("org.cougaar.core.qos.ca.CommunityFinder");
	    if (log.isInfoEnabled())
		log.info("Searching for member " +entity_name+
			  " of community " +community.getName()+ 
			  " with role " +role);

	    Entity entity = community.getEntity(entity_name);
	    boolean result = false;
	    if (entity != null) {
		Attributes attrs = entity.getAttributes();
		Attribute attr = attrs.get("Role");
		result = (attr != null && attr.contains(role));
	    }

	    if (log.isInfoEnabled()) {
		log.info(result ? "Succeeded" : "Failed");
	    }

	    return result;
	}

    }

    public static UnaryPredicate memberHasRole(String entity, String role)
    {
	return new MemberHasRoleP(entity, role);
    }

}
