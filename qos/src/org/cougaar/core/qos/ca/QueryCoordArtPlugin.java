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
import java.util.Map;
import java.util.Observable;
import java.util.Properties;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
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
 * This plugin is the generic implementation of the query side of a
 * multicast query/response rpc Cooordination Artifact.  It handles the
 * Community and Relay muck, leaving instantiable extensions only a
 * few domain-specific tasks to deal with, as described in the
 * abtract methods. 
 * 
 * In the CA scheme, multicast query/response rpc CA has two roles,
 * query and respond.  This entity provides facets for the query
 * role. The corresponding response plugin is {@link ResponseCoordArtPlugin}.
 * The flow of control is as follows: <ol> <li>A RolePlayer for the
 * query role asserts a query as a Fact. </li> <li>The FacetProvider
 * for the query detects the new Fact, constructs a QueryRelay from
 * the Fact and transmits in the usual way through the
 * Blackboard. </li> <li>Each FacetProvider for the response role
 * receives the QueryRelay in the usual way on the Blackboard and
 * asserts a corresponding Fact for the query. </li> <li>The
 * RolePlayers for the response role detect the new query Fact and
 * assert a response Fact. </li><li>The FacetProviders for the
 * response detect the new response Fact, constructs a ResponseRelay
 * from the Fact and transmits in the usual way through the
 * Blackboard. </li><li>The FacetProvider for the query role receives
 * the ResponseRelay in the usual way on the Blackboard and asserts a
 * corresponding Fact for the response. </li><li>The RolePlayer for
 * the query role detects the new response Fact and process it. </li>
 * </ol>
 *
 */
abstract public class QueryCoordArtPlugin
    extends FacetProviderPlugin
    implements CoordArtConstants
{
    private LoggingService log;
    private UIDService uids;
    private IncrementalSubscription responseSub;
    private MessageAddress agentId;
    private RelayReclaimer reclaimer = null;
    private List facets = new ArrayList();
    
    private class RelayReclaimer
    {
	private static final long CLEANUP_TIMEOUT = 300000; // 5 min
	HashMap relays = new HashMap();

	// should only be called from add()
	private void reclaim() 
	{
	    long now = System.currentTimeMillis();
	    if (log.isDebugEnabled())
		log.debug("Entering reclaimer");
	    Iterator itr = relays.entrySet().iterator();
	    while (itr.hasNext()) {
		Map.Entry entry = (Map.Entry) itr.next();
		QueryRelay relay = (QueryRelay) entry.getKey();
		long expiration_time = ((Long) entry.getValue()).longValue();
		if (expiration_time <= now) {
		    blackboard.publishRemove(relay);
		    itr.remove();
		    if (log.isDebugEnabled())
			log.debug("Removing QueryRelay: " + relay);
		} else {
		    if (log.isDebugEnabled())
			log.debug("QueryRelay: " +relay+
				 " expires later");
		}
	    }
	}

	void add(QueryRelay relay)
	{
	    synchronized (relays) {
		if (log.isDebugEnabled())
		    log.debug("Adding QueryRelay: " + relay);
		reclaim();
		long expiration = System.currentTimeMillis() + CLEANUP_TIMEOUT;
		relays.put(relay, new Long(expiration));
		if (log.isDebugEnabled())
		log.debug("relays.size: " + relays.size());		
	    }
	}
    }

    abstract protected class QueryFacet 
	extends FacetImpl
	implements QueryCoordArtConstants
    {
	/**
	 * The implementation of this method in instantiable extensions
	 * would transform the given fact, as received from a querying
	 * RolePlayer, into an Object suitable for transmission as the
	 * content of QueryRelay.
	 */
	public abstract Object transformQuery(Fact fact);

	/**
	 * The implementation of this method in instantiable extensions
	 * would transform the given response, as received from a
	 * ResponseCoordArtPlugin via relay, into a Fact that will be
	 * propagated out to the querying RolePlayer.
	 */
	public abstract Fact transformResponse(ResponseRelay response);

	private CommunityFinder finder;
	private String communityType;
	private String communityRole;
	private Community community;
	private String communityName;
	private AttributeBasedAddress aba;

	protected QueryFacet(ConnectionSpec spec, RolePlayer player)
	{
	    super(QueryCoordArtPlugin.this, spec, player);
	    Properties role_parameters = spec.role_parameters;
	    communityType = 
		role_parameters.getProperty(COMMUNITY_TYPE_ATTRIBUTE);
	    communityRole = 
		role_parameters.getProperty(RESPONDERS_COMMUNITY_ROLE_ATTRIBUTE);
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
					       communityType,
					       MANAGER_ATTRIBUTE, 
					       agentId.getAddress());
		finder = new CommunityFinder.ForAny(commService, filter);
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
								   communityRole);
		linkPlayer();
		triggerExecute();
	    }
	}

	protected Community getCommunity()
	{
	    if (finder == null)
		return null;
	    else
		return finder.getCommunity();
	}


	// Fact processing
	private void processFactBase()
	{
	    for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
		if (log.isDebugEnabled()) 
		    log.debug("Processing fact " + frev.getFact());
		if (frev.isAssertion()) {
		    Fact fact = frev.getFact();
		    // Should only be one and should be a RequestFact
		    sendQuery(fact);
		} else {
		    // no retractions yet
		}
	    }
	}

	protected void sendQuery(Fact fact)
	{
	    if (log.isDebugEnabled()) {
		log.debug("sendQueries()");
	    }

	    if (community == null) return; 
	    // too early, but this shouldn't happen

	    UID uid = uids.nextUID();
	    Object query = transformQuery(fact);
	    long timestamp = System.currentTimeMillis();
	    QueryRelay qr = 
		new QueryRelayImpl(uid, agentId, aba, query, timestamp);
	    if (log.isShoutEnabled()) {
		log.shout("Sending QueryRelay from " +agentId +
			  " to all nodes in community: " + community);
	    }
	    publishQuery(qr);
	}

	protected void publishQuery(QueryRelay query)
	{
	    blackboard.publishAdd(query);
	    reclaimer.add(query);
	}



	// Relay processing
	/**
	 * Handle a single response.  The default is to assert it
	 * immediately to the player.  Subclasses can override.
	 */
	protected void processResponse(ResponseRelay response)
	{
	    Fact responseFact = transformResponse(response);
	    if (responseFact != null) getPlayer().factAsserted(responseFact);
	}

    }




    public QueryCoordArtPlugin() 
    {
    }
    
    /**
     * The implementation of this method in instantiable extensions
     * would return the name of the specific Coordination Artifact.
     */
    public abstract String getArtifactKind();

    /**
     * Make a CA-specific QueryFacet.
     */
    public abstract QueryFacet makeQueryFacet(ConnectionSpec spec, 
					      RolePlayer player);


    /**
     * The implementation of this method in instantiable extensions
     * would return true or false depending on whether or not the
     * given relay was of interest to the Artifact instance.
     */
    public abstract boolean acceptResponse(ResponseRelay response);


    // get services
    public void load() 
    {
	super.load();
	
	ServiceBroker sb = getServiceBroker();
	
	
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);
	
	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);

	reclaimer = new RelayReclaimer();

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


	if (log.isDebugEnabled()) {
	    log.debug("loaded");
	}
    }
   
    protected Facet makeClientFacet(ConnectionSpec spec, RolePlayer player)
    {
	Facet facet  = makeQueryFacet(spec, player);
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

    public void unload() 
    {
	if (uids != null) {
	    getServiceBroker().releaseService(this, UIDService.class, uids);
	    uids = null;
	}
	// unregister with the FacetBroker
    }
    
    /*
     * 	subscribe to ResponseRelays from yourself & all DOSNODEs
     */
    protected void setupSubscriptions() 
    {
	responseSub = (IncrementalSubscription)
	    blackboard.subscribe(ResponsePred);
    }
    
    
    
    
    // Two circumstances in which this runs:
    // (1) subscription (ResponsePred)
    // (2) new fact assertion or retraction in our fact base
    protected void execute() 
    {
	if (log.isDebugEnabled()) log.debug("Execute");
	List copy = null;
	synchronized (facets) {
	    copy = new ArrayList(facets);
	}
	if (responseSub.hasChanged()) evaluateResponses(copy);
	processFactBase(copy);
    }


    private void processFactBase(List activeFacets)
    {
	for (int i=0; i<activeFacets.size(); i++) {
	    QueryFacet facet = (QueryFacet) activeFacets.get(i);
	    if (facet.factsHaveChanged()) facet.processFactBase();
	}
    }



    /**
     * Handle a single response.  The default is to assert it
     * immediately to the player.  Subclasses can override.
    */
    protected void processResponse(ResponseRelay response,  List activeFacets)
    {
	for (int i=0; i<activeFacets.size(); i++) {
	    QueryFacet facet = (QueryFacet) activeFacets.get(i);
	    facet.processResponse(response);
	}
    }


    /*
     * Receive process ResponseRelays
     * Do cleanup of outstanding stale QueryRelays
     */
    protected void evaluateResponses(List activeFacets) 
    {	
	if (log.isDebugEnabled()) {
	    log.debug("In evaluateResponses()");
	}
	
	// Evaluate response subscription
	if(responseSub.hasChanged()) 
	    {
		Enumeration en;
		
		// observe added relays
		en = responseSub.getAddedList();
		while (en.hasMoreElements()) {
		    ResponseRelay response = (ResponseRelay) 
			en.nextElement();
		    if (log.isDebugEnabled()) {
			log.debug("Observed added ResponseSub"+response);
		    }		    
		    
		    // Assert the data to the RolePlayer
		    blackboard.publishRemove(response);
		    responseSub.remove(response);
		    processResponse(response, activeFacets);
		}
		
		
		// observe changed relays
		en = responseSub.getChangedList();
		while (en.hasMoreElements()) {
		    ResponseRelay tr = (ResponseRelay)
			en.nextElement();
		    // Should not happen
		    if (log.isDebugEnabled()) {
			log.debug("Observed changed ResponseSub "+tr);
		    }
		}
		
		// removed relays
		 en = responseSub.getRemovedList();
		 while (en.hasMoreElements()) {
		    ResponseRelay tr = (ResponseRelay) 
			en.nextElement();
		    if (log.isDebugEnabled()) {			
			log.debug("Observed removed ResponseSub"+tr);
		    }
		}
	    }
    }
	
      
    /**
     * ResponseRelay subscription predicate, which matches QueryRelays where 
     * local manager address matches either the source or target.
     */ 
    
    
    private UnaryPredicate ResponsePred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof ResponseRelay) {
		    ResponseRelay relay = (ResponseRelay) o;
		    return acceptResponse(relay);
		} else {
		    return false;
		}
	    }
	};
    

    
}
