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

import java.util.Observable;
import java.util.Observer;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.community.Community;
import org.cougaar.multicast.AttributeBasedAddress;


/**
 * Default implementation for Facets that deal with ABAs and
 * Communities.
 */
abstract public class CommunityFacetImpl
    extends FacetImpl
    implements Observer
{
    private Community community;
    private String communityName;
    private CommunityFinder finder;
    private AttributeBasedAddress aba;
    private CommunityService commService;

    /**
     * Hook for domain-specific Facet implementations to construct an
     * ABA given a community.
     */
    public abstract AttributeBasedAddress makeABA(String communityName);


    protected CommunityFacetImpl(CoordinationArtifact owner, 
				 ServiceBroker sb,
				 ConnectionSpec spec, 
				 RolePlayer player)
    {
	super(owner, sb, spec, player);
	commService = (CommunityService)
	    sb.getService(this, CommunityService.class, null);

    }


    protected AttributeBasedAddress getABA()
    {
	return aba;
    }

    protected Community getCommunity()
    {
	return community;
    }



    // Community helpers
    protected void findCommunityForAny(String filter,
				       UnaryPredicate predicate)
    {
	finder = new CommunityFinder.ForAny(commService, filter, predicate);
	finder.addObserver(this);
    }

    protected void findCommunityForAgent(String filter, 
					 UnaryPredicate predicate)
    {
	finder = 
	    new CommunityFinder.ForAgent(commService, filter, predicate,
					 getAgentID());
	finder.addObserver(this);
    }


    // Observer.  Used for CommunityFinder callbacks.
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
	}
    }



    protected Receptacle makeReceptacle()
    {
	return new CommunityReceptacleImpl();
    }



    protected class CommunityReceptacleImpl extends ReceptacleImpl {
	public Community getCommunity()
	{
	    return community;
	}

    }

}
