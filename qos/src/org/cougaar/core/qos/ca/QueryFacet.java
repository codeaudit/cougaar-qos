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

import java.util.Enumeration;
import java.util.Properties;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.util.UID;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.util.UnaryPredicate;

abstract public class QueryFacet 
    extends FacetImpl
    implements QueryCoordArtConstants
{
    /**
     * The implementation of this method in instantiable extensions
     * would return true or false depending on whether or not the
     * given relay was of interest to the Artifact instance.
     */
    public abstract boolean acceptResponse(ResponseRelay response);


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

    private String managerAttr;
    private RelayReclaimer reclaimer = null;
    private String communityRole, communityType;
    private IncrementalSubscription responseSub;

    protected QueryFacet(FacetProviderImpl owner,
			 ServiceBroker sb,
			 ConnectionSpec spec, 
			 RolePlayer player)
    {
	super(owner, sb, spec, player);
	    



	communityType = 
	    spec.ca_parameters.getProperty(COMMUNITY_TYPE_ATTRIBUTE);

	Properties role_parameters = spec.role_parameters;
	managerAttr = 
	    role_parameters.getProperty(MANAGER_ATTRIBUTE);
	communityRole = 
	    role_parameters.getProperty(RESPONDERS_COMMUNITY_ROLE_ATTRIBUTE);
	reclaimer = new RelayReclaimer(sb);

	String filter = 
	    CommunityFinder.makeFilter(COMMUNITY_TYPE_ATTRIBUTE, 
				       communityType,
				       managerAttr, 
				       getAgentID().getAddress());
	findCommunityForAny(filter);
    }


    /*
     * 	subscribe to ResponseRelays from yourself & all DOSNODEs
     */
    public void setupSubscriptions(BlackboardService blackboard) 
    {
	responseSub = (IncrementalSubscription)
	    blackboard.subscribe(ResponsePred);
    }

    public void execute(BlackboardService blackboard)
    {
	if (responseSub == null || !responseSub.hasChanged()) return;

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
	    processResponse(response);
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

    public AttributeBasedAddress makeABA(String communityName)
    {
	return 
	    AttributeBasedAddress.getAttributeBasedAddress(communityName, 
							   "Role", 
							   communityRole);
    }


    // Fact processing
    public void processFactBase(BlackboardService blackboard)
    {
	if (!factsHaveChanged()) return;
	for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
	    if (log.isDebugEnabled()) 
		log.debug("Processing fact " + frev.getFact());
	    if (frev.isAssertion()) {
		Fact fact = frev.getFact();
		// Should only be one and should be a RequestFact
		sendQuery(fact, blackboard);
	    } else {
		// no retractions yet
	    }
	}
    }

    protected void sendQuery(Fact fact, BlackboardService blackboard)
    {
	if (log.isDebugEnabled()) {
	    log.debug("sendQueries()");
	}

	AttributeBasedAddress aba = getABA();
	if (aba == null) return; 
	// too early, but this shouldn't happen

	UID uid = nextUID();
	Object query = transformQuery(fact);
	long timestamp = System.currentTimeMillis();
	QueryRelay qr = 
	    new QueryRelayImpl(uid, getAgentID(), aba, query, communityType,
			       timestamp);
	if (log.isInfoEnabled()) {
	    log.info("Sending QueryRelay from " +getAgentID() +
		      " to all nodes in community: " + getCommunity());
	}
	publishQuery(qr, blackboard);
    }

    protected void publishQuery(QueryRelay query, BlackboardService blackboard)
    {
	blackboard.publishAdd(query);
	reclaimer.add(query, blackboard);
    }



    // Relay processing
    /**
     * Handle a single response.  The default is to assert it
     * immediately to the player.  Subclasses can override.
     */
    protected void processResponse(ResponseRelay response)
    {
	Fact responseFact = transformResponse(response);
	if (log.isDebugEnabled())
	    log.debug("Tranformed " +response+ " into " +responseFact);
	if (responseFact != null) getPlayer().factAsserted(responseFact, this);
    }



    /**
     * ResponseRelay subscription predicate, which matches QueryRelays where 
     * local manager address matches either the source or target.
     */ 
    
    
    private UnaryPredicate ResponsePred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof ResponseRelay) {
		    ResponseRelay relay = (ResponseRelay) o;
		    if (log.isDebugEnabled())
			log.debug("testing response relay" +relay);
		    String responseCommunity = relay.getCommunity();
		    return responseCommunity.equals(communityType) &&
			acceptResponse(relay);
		} else {
		    return false;
		}
	    }
	};
    
}
