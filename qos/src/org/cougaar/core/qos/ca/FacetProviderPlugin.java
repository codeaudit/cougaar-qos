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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;


import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.thread.Schedulable;

/**
 * This class represents the piece of a Coordination Artifact that
 * exists in one particular jvm (the full artifact is distributed).
 * In the general case it's nothing but a set of parameters that are
 * used to distinguish this one from others of the same kind, and a
 * set of roles, each of which can be linked to one or more
 * RolePlayers through facets.  Subclasses can provide domain-specific
 * state.
 *
 * This class is an Observer so that it can be notified by a
 * CommunityFinder when the community is known.
 */
abstract public class FacetProviderPlugin
    extends ParameterizedPlugin
    implements FacetProvider, Observer
{
    private Properties parameters;
    private HashMap facets;
    private Schedulable sched;
    private SimpleQueue factQueue;

    private BlackboardService bbs;


    private static class SimpleQueue extends LinkedList {
	Object next() {
	    return removeFirst();
	}
    }

    // The factQueue consists of FactRevision instances

    protected FacetProviderPlugin()
    {
	this.parameters = new Properties();

	this.facets = new HashMap();
	this.factQueue = new SimpleQueue();
    }


    public void load()
    {
	super.load();

	// TBD: fill in this.parameters from the plugin params
	//String name = getParameter("name");
	//parameters.put("name", name);

	ServiceBroker sb = getServiceBroker();


    }

    public void start()
    {
	bbs = getBlackboardService();
    }


    // FacetProvider
    public boolean matches(ConnectionSpec spec)
    {
	if (spec.parameters == null && parameters == null) return true;
	if (spec.parameters == null || parameters == null) return false;
	return spec.parameters.equals(parameters);
    }


    public void provideFacet(ConnectionSpec spec, RolePlayer player)
    {
	String role = spec.role;
	Facet client_facet = null;
	synchronized (facets) {
	    client_facet = makeClientFacet(role, player);
	    Facet provider_facet = makeProviderFacet(role, player);
	    Set role_facets = (Set) facets.get(role); // multiple players/role
	    if (role_facets == null) {
		role_facets = new HashSet();
		facets.put(role, role_facets);
	    } 
	    role_facets.add(provider_facet);
	}
	player.facetAvailable(spec, client_facet);
    }


    protected void assertFactToRole(Fact fact, String role)
    {
	Set role_facets = null;
	synchronized (facets) {
	    Set _facets = (Set) facets.get(role); // multiple players/role
	    if (_facets != null) role_facets = new HashSet(_facets);
	}
	if (role_facets != null) {
	    Iterator itr = role_facets.iterator();
	    while (itr.hasNext()) {
		Facet facet = (Facet) itr.next();
		facet.assertFact(fact);
	    }
	}
    }

    // Extensions of can make specific kinds of facets.  Here we make
    // the generic one.
    protected Facet makeClientFacet(String role, RolePlayer player)
    {
	return new FacetImpl(this, role, player);
    }


    // Extensions of can make specific kinds of facets.  Here we make
    // the generic one.
    protected Facet makeProviderFacet(String role, RolePlayer player)
    {
	return new ProviderFacetImpl(player);
    }


    private void addRevision(FactRevision entry)
    {
	synchronized (factQueue) {
	    factQueue.add(entry);
	}
	if (bbs != null) bbs.signalClientActivity();
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


    // The next two methods are up calls from facets.  The actual
    // handling of facts runs in its own thread, associated with the
    // queue.   See the two subsequent methods.
    void assertFact(Facet facet, Fact fact)
    {
	FactRevision entry = new FactAssertion(fact, facet);
	addRevision(entry);
    }


    void retractFact(Facet facet, Fact fact)
    {
	FactRevision entry = new FactRetraction(fact, facet);
	addRevision(entry);
    }


}