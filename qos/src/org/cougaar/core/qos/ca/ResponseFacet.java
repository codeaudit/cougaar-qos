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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.util.UID;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.util.UnaryPredicate;


/**
 * An abstraction of the response role in the {@link
 * QueryResponseCoordinationArtifactTemplate}.  The {@link Facet} methods are
 * implemented here, leaving subclasses only to implement the abstract
 * methods of this class.
 */
public class ResponseFacet
    extends FacetImpl
    implements QueryCoordArtConstants
{
    private String managerRole;
    private HashSet completedUIDs;
    private ResponseRelay lastResponse; // for cleaning up
    private IncrementalSubscription querySub;

    protected ResponseFacet(CoordinationArtifact owner,
			    ServiceBroker sb,
			    ConnectionSpec spec,
			    RolePlayer player)
    {
	super(owner, sb, spec, player);
	Properties role_parameters = spec.role_parameters;
	completedUIDs = new HashSet();

	String communityType = 
	    spec.ca_parameters.getProperty(COMMUNITY_TYPE_ATTRIBUTE);
	managerRole = 
	    role_parameters.getProperty(MANAGER_ATTRIBUTE);

	String filter = 
	    CommunityFinder.makeFilter(COMMUNITY_TYPE_ATTRIBUTE, 
				       communityType);
	findCommunityForAgent(filter);
    }



    public AttributeBasedAddress makeABA(String communityName)
    {
	return 
	    AttributeBasedAddress.getAttributeBasedAddress(communityName, 
							   "Role", 
							   managerRole);
    }


    public void setupSubscriptions(BlackboardService blackboard) 
    {
	querySub = (IncrementalSubscription)
	    blackboard.subscribe(QueryPred);
    }

    public void execute(BlackboardService blackboard)
    {
	if (querySub == null || !querySub.hasChanged()) return;

	Enumeration en;
	// observe added relays
	en = querySub.getAddedList();
	while (en.hasMoreElements()) {
	    QueryRelay relay = (QueryRelay) en.nextElement();
	    processQuery(relay);
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

    // Process facts
    public void processFactBase(BlackboardService blackboard)
    {
	if (!factsHaveChanged()) return;

	for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
	    if (log.isDebugEnabled()) 
		log.debug("Processing fact " + frev.getFact());
	    if (frev.isAssertion()) {
		Object fact = frev.getFact();
		sendReply(fact, blackboard);
	    } else {
		// no retractions yet
	    }
	}
    }

    private void sendReply(Object reply, BlackboardService blackboard)
    {
	// Pass back response relay
	UID uid = nextUID();
	//String s = "Response Matrix";
	long timestamp = System.currentTimeMillis();
	    
	// Remove old respsonse, reassign
	if(lastResponse != null) {
	    blackboard.publishRemove(lastResponse);
	}

	ResponseRelay response = 
	    new ResponseRelayImpl(uid, getAgentID(), getABA(), 
				  reply,
				  timestamp);
	    
	    
	if (log.isDebugEnabled()) {
	    log.debug("Responding to query: " // + queryUID
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

	Object fact = query.getQuery();
	if (log.isDebugEnabled())
	    log.debug("Updated Fact" +fact);
	getPlayer().factAsserted(fact, getReceptacle());
    }


    /**
     * QueryRelay subscription predicate, which matches QueryRelays where 
     * local manager address matches either the source or target.
     */ 
    
    
    private UnaryPredicate QueryPred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof QueryRelay) {
		    return true;
		} else {
		    return false;
		}
	    }
	};
    

}

