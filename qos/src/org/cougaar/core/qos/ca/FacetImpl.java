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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.community.Community;
import org.cougaar.core.util.UID;
import org.cougaar.multicast.AttributeBasedAddress;


/**
 * Default implementation for Facet.
 */
abstract public class FacetImpl // should be abstract
    implements Facet,  Observer
{
    private RolePlayer player;
    private ConnectionSpec spec;
    private FacetProvider owner;
    private SimpleQueue factQueue;
    private Community community;
    private String communityName;
    private CommunityFinder finder;
    private AttributeBasedAddress aba;
    private ServiceBroker sb;
    private CommunityService commService;
    private UIDService uids;
    private MessageAddress agentId;

    protected LoggingService log;


    private static class SimpleQueue extends LinkedList {
	Object next() {
	    return removeFirst();
	}
    }

    /**
     * Hook for domain-specific Facet implementations to construct an
     * ABA given a community.
     */
    public abstract AttributeBasedAddress makeABA(String communityName);


    protected FacetImpl(FacetProvider owner, 
			ServiceBroker sb,
			ConnectionSpec spec, 
			RolePlayer player)
    {
	this.player = player;
	this.spec = spec;
	this.owner = owner;
	// The factQueue consists of FactRevision instances
	this.factQueue = new SimpleQueue();

	this.sb = sb;
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);

	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);
	

	// get agent id
	AgentIdentificationService agentIdService = (AgentIdentificationService) 
	    sb.getService(this, 
			  AgentIdentificationService.class, 
			  null);
	if (agentIdService == null) {
	    throw new RuntimeException("Unable to obtain agent-id service");
	}

	agentId = agentIdService.getMessageAddress();
	sb.releaseService(this, 
			  AgentIdentificationService.class, 
			  agentIdService);
	if (agentId == null) {
	    throw new RuntimeException("Agent id is null");
	}


	commService = (CommunityService)
	    sb.getService(this, CommunityService.class, null);

    }


    // when is this called?
    public void unload() 
    {
	if (uids != null) {
	    sb.releaseService(this, UIDService.class, uids);
	    uids = null;
	}
    }



    RolePlayer getPlayer()
    {
	return player;
    }

    FacetProvider getOwner()
    {
	return owner;
    }


    CommunityService getCommunityService()
    {
	return commService;
    }

    MessageAddress getAgentID()
    {
	return agentId;
    }


    AttributeBasedAddress getABA()
    {
	return aba;
    }


    UID nextUID()
    {
	return uids.nextUID();
    }

    protected Community getCommunity()
    {
	return community;
    }


    protected void findCommunityForAny(String filter)
    {
	finder = new CommunityFinder.ForAny(commService, filter);
	finder.addObserver(this);
    }

    protected void findCommunityForAgent(String filter)
    {
	finder = new CommunityFinder.ForAgent(commService, filter, agentId);
	finder.addObserver(this);
    }


    // Observer.
    //
    // Used for CommunityFinder callbacks.
    public void update(Observable obs, Object value)
    {
	if (log.isDebugEnabled())
	    log.debug("CommunityFinder " +obs+ " callback returned " 
		      + value);
	if (obs instanceof CommunityFinder) {
	    CommunityFinder finder = (CommunityFinder) obs;
	    this.communityName = (String) value;
	    this.community = finder.getCommunity();
	    this.aba = makeABA(communityName);
	    linkPlayer();
	    owner.triggerExecute();
	}
    }



    protected void linkPlayer()
    {
	if (log.isInfoEnabled())
	    log.info("Linking " +player+ " to " +this);
	player.facetAvailable(spec, this);
    }




    protected boolean factsHaveChanged()
    {
	// TBD
	return true;
    }


    // Artifact-specific Providers get at the new facts this way.
    protected FactRevision nextFact()
    {
	synchronized (factQueue) {
	    if (factQueue.isEmpty())
		return null;
	    else
		return (FactRevision) factQueue.next();
	}
    }



    private void addRevision(FactRevision entry)
    {
	synchronized (factQueue) {
	    factQueue.add(entry);
	}
	owner.triggerExecute();
    }


    // The next two methods are up calls from facets.  The actual
    // handling of facts runs in its own thread, associated with the
    // queue.   See the two subsequent methods.
    public void assertFact(Fact fact)
    {
	FactRevision entry = new FactAssertion(fact);
	addRevision(entry);
    }


    public void retractFact(Fact fact)
    {
	FactRevision entry = new FactRetraction(fact);
	addRevision(entry);
    }


    public String getArtifactId()
    {
	return owner.getArtifactId();
    }
}
