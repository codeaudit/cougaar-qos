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
import java.util.Iterator;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;


import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.community.Community;
import org.cougaar.core.service.community.Entity;
import org.cougaar.core.util.UID;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.util.UnaryPredicate;

/**
 * An abstraction of the query role in the {@link
 * QueryResponseCoordinationArtifactTemplate}.  The {@link Facet} methods are
 * implemented here, leaving subclasses only to implement the abstract
 * methods of this class.
 */
abstract public class QueryFacet 
    extends FacetImpl
    implements QueryCoordArtConstants
{
    private String managerAttr;
    private RelayReclaimer reclaimer = null;
    private String communityRole;
    private IncrementalSubscription responseSub;

    protected QueryFacet(CoordinationArtifact owner,
			 ServiceBroker sb,
			 ConnectionSpec spec, 
			 RolePlayer player)
    {
	super(owner, sb, spec, player);
	    



	String communityType = 
	    spec.ca_parameters.getProperty(COMMUNITY_TYPE_ATTRIBUTE);

	Properties role_parameters = spec.role_parameters;
	managerAttr = 
	    role_parameters.getProperty(MANAGER_ATTRIBUTE);
	communityRole = 
	    role_parameters.getProperty(RESPONDERS_COMMUNITY_ROLE_ATTRIBUTE);
	if (log.isDebugEnabled()) {
	    log.debug("Value of " +MANAGER_ATTRIBUTE+ " is " +managerAttr);
	    log.debug("Value of " +RESPONDERS_COMMUNITY_ROLE_ATTRIBUTE+ " is "
		      +communityRole);
	}
	reclaimer = new RelayReclaimer(sb);

	String filter = 
	    CommunityFinder.makeFilter(COMMUNITY_TYPE_ATTRIBUTE, 
				       communityType,
				       managerAttr, 
				       getAgentID().getAddress());
	findCommunityForAny(filter);
    }

    abstract protected boolean acceptFact(Object fact);


    public AttributeBasedAddress makeABA(String communityName)
    {
	return 
	    AttributeBasedAddress.getAttributeBasedAddress(communityName, 
							   "Role", 
							   communityRole);
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


    // Fact processing
    public void processFactBase(BlackboardService blackboard)
    {
	if (!factsHaveChanged()) return;
	for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
	    if (log.isDebugEnabled()) 
		log.debug("Processing fact " + frev.getFact());
	    if (frev.isAssertion()) {
		Object fact = frev.getFact();
		// Should only be one and should be a RequestFact
		sendQuery(fact, blackboard);
	    } else {
		// no retractions yet
	    }
	}
    }

    protected void sendQuery(Object query, BlackboardService blackboard)
    {
	if (log.isDebugEnabled()) {
	    log.debug("sendQueries()");
	}

	AttributeBasedAddress aba = getABA();
	if (aba == null) return; 
	// too early, but this shouldn't happen

	UID uid = nextUID();
	long timestamp = System.currentTimeMillis();
	QueryRelay qr = 
	    new QueryRelayImpl(uid, getAgentID(), aba, query, timestamp);
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
	Object responseFact = response.getReply();
	if (log.isDebugEnabled())
	    log.debug("Tranformed " +response+ " into " +responseFact);
	if (responseFact != null) 
	    getPlayer().factAsserted(responseFact, getReceptacle());
    }


    protected Receptacle makeReceptacle()
    {
	return new QueryReceptacleImpl();
    }


    private class QueryReceptacleImpl 
	extends ReceptacleImpl
	implements QueryReceptacle
    {
	public int getReceiverCount()
	{
	    Community community = getCommunity();
	    int count = 0;
	    Iterator itr = community.getEntities().iterator();
	    if (log.isDebugEnabled())
		log.debug("Counting members of " +community.getName()+ 
			  " with Role " +communityRole);
	    while (itr.hasNext()) {
		Entity entity = (Entity) itr.next();
		Attributes attrs = entity.getAttributes();
		Attribute attr = attrs.get("Role");
		if (attr != null && attr.contains(communityRole)) ++count;
	    }
	    if (log.isDebugEnabled())
		log.debug("Counted " +count+ 
			  " members of " +community.getName()+ 
			  " with Role " +communityRole);
	    return count;
	}
    }



    /**
     * ResponseRelay subscription predicate, which matches QueryRelays where 
     * local manager address matches either the source or target.
     */ 
    
    
    private UnaryPredicate ResponsePred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof ResponseRelay) {
		    Object fact = ((ResponseRelay) o).getReply();
		    return acceptFact(fact);
		} else {
		    return false;
		}
	    }
	};
    
}
