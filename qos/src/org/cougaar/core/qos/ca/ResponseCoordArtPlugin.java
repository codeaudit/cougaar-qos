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

package org.cougaar.core.qos.ca;



import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.community.Community;
import org.cougaar.core.util.UID;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.util.UnaryPredicate;

/**
 * Member plugin which provides sparce traffic matrix snapshots when
 * asked to do so.
 */

/**
 * This plugin is the generic implementation of the response side of a
 * multicast query/response rpc Cooordination Artifact.  It handles the
 * Community and Relay muck, leaving instantiable extensions only a
 * few domain-specific tasks to deal with, as described in the
 * abtract methods. 
 * 
 * In the CA scheme, multicast query/response rpc CA has two roles:
 * query and respond.  This entity provides facets for the response
 * role. The corresponding query plugin is {@link QueryCoordArtPlugin}.
 */
abstract public class ResponseCoordArtPlugin
    extends FacetProviderPlugin
    implements CoordArtConstants
{
    private LoggingService log;
    private UIDService uids;
    private IncrementalSubscription querySub;
    private MessageAddress agentId;
    private List facets = new ArrayList();

    abstract protected class ResponseFacet
	extends FacetImpl
	implements QueryCoordArtConstants
    {
	/**
	 * The implementation of this method in instantiable extensions
	 * would transform the given response, as received via relay from
	 * a QueryCoordArtPlugin, into a Fact that will be propagated out
	 * to the responding RolePlayer.
	 */
	public abstract Fact transformQuery(QueryRelay relay);

	/**
	 * The implementation of this method in instantiable extensions
	 * would transform the given fact, as received from a responding
	 * RolePlayer, into an Object suitable for transmission as the
	 * content of ResponseRelay.
	 */
	public abstract Object transformResponse(Fact fact);

	private HashSet completedUIDs;
	private ResponseRelay lastResponse; // for cleaning up
	private CommunityFinder finder;
	private String communityType;
	private String managerRole;
	private Community community;
	private String communityName;
	private AttributeBasedAddress aba;

	protected ResponseFacet(ConnectionSpec spec, RolePlayer player)
	{
	    super(ResponseCoordArtPlugin.this, spec, player);
	    Properties role_parameters = spec.role_parameters;
	    communityType = 
		role_parameters.getProperty(COMMUNITY_TYPE_ATTRIBUTE);
	    managerRole = 
		role_parameters.getProperty(MANAGER_ATTRIBUTE);
	    startFinder();
	}

	void startFinder()
	{
	    ServiceBroker sb = getServiceBroker();


	    CommunityService commService = (CommunityService)
		sb.getService(this, CommunityService.class, null);
	    if (commService==null || agentId==null) {
		if(log.isWarnEnabled()) {
		    log.warn("Either community " + commService +" or agentId "  
			     + agentId + " is null, can not find community");
		}
	    }
	    else {
		String filter = 
		    CommunityFinder.makeFilter(COMMUNITY_TYPE_ATTRIBUTE, 
					       communityType);
		finder = new CommunityFinder.ForAgent(commService, filter, agentId);
		finder.addObserver(this);
		if(log.isDebugEnabled()) {
		    log.debug("New CommunityFinder loaded for " + agentId +
			      ", community = " + finder.getCommunityName());
		}
	    }
	}


	// Observer.
	//
	// Used for CommunityFinder callbacks.
	public void update(Observable obs, Object value)
	{
	    if (log.isDebugEnabled())
		log.debug("CommunityFinder " +obs+ " callback returned " 
			  + value);
	    if (obs == finder) {
		this.communityName = (String) value;
		this.community = finder.getCommunity();
		this.aba = 
		    AttributeBasedAddress.getAttributeBasedAddress(communityName, 
								   "Role", 
								   managerRole);
		linkPlayer();
		triggerExecute();
	    }
	}


	// Process facts
	private void processFactBase()
	{
	    for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
		if (log.isDebugEnabled()) 
		    log.debug("Processing fact " + frev.getFact());
		if (frev.isAssertion()) {
		    Fact fact = frev.getFact();
		    sendReply(fact);
		} else {
		    // no retractions yet
		}
	    }
	}

	private void sendReply(Fact fact)
	{
	    UID queryUID = (UID) fact.getAttribute(UidAttr);
	    // Pass back response relay
	    UID uid = uids.nextUID();
	    //String s = "Response Matrix";
	    long timestamp = System.currentTimeMillis();
	    
	    // Remove old respsonse, reassign
	    if(lastResponse != null) {
		blackboard.publishRemove(lastResponse);
	    }

	    Object payload = transformResponse(fact);
	    
	    ResponseRelay response = 
		new ResponseRelayImpl(uid, agentId, aba, 
				      payload, queryUID, timestamp);
	    
	    
	    if (log.isDebugEnabled()) {
		log.debug("Responding to query: " + queryUID
			  + " with new reply: " + response);
	    }
	    //blackboard.publishChange(tr);
	    blackboard.publishAdd(response);

	    lastResponse = response;

	}



	// process relays
	void processQuery(QueryRelay query)
	{
	    UID query_id = query.getUID();
	    // check cache
	    if(completedUIDs.contains(query_id)) {
		// ignore seen relay
		if(log.isDebugEnabled()) {
		    log.debug("Observed already seen relay: " + query);
		}
		return;
	    } else {
		// add relay to seen cache			
		completedUIDs.add(query_id);
		if (log.isDebugEnabled()) {
		    log.debug("Adding relay: "+query+ " to relays cache.");
		}
	    }
	    
	    if (log.isDebugEnabled()) {
		log.debug("Observed added relay"+query);
	    }

	    Fact fact = transformQuery(query);
	    getPlayer().factAsserted(fact);
	}

    }




    public ResponseCoordArtPlugin() 
    {
    }
    
    /**
     * The implementation of this method in instantiable extensions
     * would return the name of the specific Coordination Artifact.
     */
    public abstract String getArtifactKind();

    /**
     * The implementation of this method in instantiable extensions
     * would return true or false depending on whether or not the
     * given relay was of interest.
     */
    public abstract boolean acceptQuery(QueryRelay relay);

    /**
     * Make a CA-specific ResponseFacet.
     */
    public abstract ResponseFacet makeResponseFacet(ConnectionSpec spec, 
						    RolePlayer player);


    // get services
    public void load() 
    {
	super.load();
	
	ServiceBroker sb = getServiceBroker();
	
	
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);
	
	// get agent id
	AgentIdentificationService agentIdService =(AgentIdentificationService) 
	    getServiceBroker().getService(this, 
					  AgentIdentificationService.class, 
					  null);
	if (agentIdService == null) {
	    throw new RuntimeException("Unable to obtain agent-id service");
	}

	agentId = agentIdService.getMessageAddress();
	getServiceBroker().releaseService(this, 
					  AgentIdentificationService.class, 
					  agentIdService);
	if (agentId == null) {
	    throw new RuntimeException("Agent id is null");
	}

	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);

	// prefix all logging calls with our agent name
	log = LoggingServiceWithPrefix.add(log, agentId+": ");

	if (log.isDebugEnabled()) {
	    log.debug("loaded");
	}
    }
    
    public void unload() 
    {
	if (uids != null) {
	    getServiceBroker().releaseService(this, UIDService.class, uids);
	    uids = null;
	}
    }
    
    
    protected Facet makeClientFacet(ConnectionSpec spec, RolePlayer player)
    {
	Facet facet  = makeResponseFacet(spec, player);
	synchronized (facets) {
	    facets.add(facet);
	}
	return facet;
    }


    public void start()
    {
	super.start();

	ServiceBroker sb = getServiceBroker();
	FacetBroker fb = (FacetBroker) 
	    sb.getService(this, FacetBroker.class, null);
	String kind = getArtifactKind();
	fb.registerFacetProvider(kind, this);
	sb.releaseService(this, FacetBroker.class, fb);
    }

    /*
     * 	Subscribe to queryRelays
     */
    protected void setupSubscriptions() 
    {
	querySub = (IncrementalSubscription)
	    blackboard.subscribe(QueryPred);
    }
    
    
    
    
    // Two circumstances in which this runs:
    // (1) subscription (QueryPred)
    // (2) new fact assertion or retraction in our fact base
    protected void execute() 
    {
	if (log.isDebugEnabled()) log.debug("Execute");
	List copy = null;
	synchronized (facets) {
	    copy = new ArrayList(facets);
	}
	if (querySub.hasChanged()) replyToRelay(copy);
	processFactBase(copy);
    }
    


    private void processFactBase(List activeFacets)
    {
	for (int i=0; i<activeFacets.size(); i++) {
	    ResponseFacet facet = (ResponseFacet) activeFacets.get(i);
	    if (facet.factsHaveChanged()) facet.processFactBase();
	}
    }


    protected void processQuery(QueryRelay query,  List activeFacets)
    {
	for (int i=0; i<activeFacets.size(); i++) {
	    ResponseFacet facet = (ResponseFacet) activeFacets.get(i);
	    facet.processQuery(query);
	}
    }



    private void replyToRelay(List activeFacets) 
    {		
	Enumeration en;
	// observe added relays
	en = querySub.getAddedList();
	while (en.hasMoreElements()) {
	    QueryRelay relay = (QueryRelay) en.nextElement();
	    processQuery(relay, activeFacets);
	}
	
	// observe changed relays shouldn't happen, because the
	// manager should remove them when received log seen
	en = querySub.getChangedList();
	while (en.hasMoreElements()) {
	    QueryRelay tr = (QueryRelay) en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed changed "+tr);
	    }
	    
	    // don't pay attention
	}
	
	// removed relays
	en = querySub.getRemovedList();
	while (en.hasMoreElements()) {
	    QueryRelay tr = (QueryRelay) en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed removed relay: "+tr);
	    }
	}
    }




    /**
     * QueryRelay subscription predicate, which matches QueryRelays where 
     * local manager address matches either the source or target.
     */ 
    
    
    private UnaryPredicate QueryPred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof QueryRelay) {
		    QueryRelay relay = (QueryRelay) o;
		    return acceptQuery(relay);
		} else {
		    return false;
		}
	    }
	};
    

    
}
