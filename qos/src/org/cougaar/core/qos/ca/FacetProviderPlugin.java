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
import java.util.Properties;
import java.util.Set;


import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.CircularQueue;

/**
 * This class represents the piece of a Coordination Artifact that
 * exists in one particular jvm (the full artifact is distributed).
 * In the general case it's nothing but a set of parameters that are
 * used to distinguish this one from others of the same kind, and a
 * set of roles, each of which can be linked to one or more
 * RolePlayers through facets.  Subclasses can provide domain-specific
 * state.
 *
 * This class is a Runnable so that it can run a Schedulable for a
 * queue of fact revisions (assertions and retractions).
 */
abstract public class FacetProviderPlugin
    extends ComponentPlugin
    implements Runnable, FacetProvider
{
    private Properties parameters;
    private HashMap facets;
    private Schedulable sched;
    private CircularQueue queue;




    private abstract class FactRevision implements Runnable {
	Fact fact;
	Facet facet;
	FactRevision(Fact fact, Facet facet) {
	    this.fact = fact;
	    this.facet = facet;
	}
    }

    private class FactAssertion extends FactRevision {
	FactAssertion(Fact fact, Facet facet) {
	    super(fact, facet);
	}

	public void run() {
	    factAsserted(fact, facet); // plugin handling -- blackboard etc.
	}
    }

    private class FactRetraction extends FactRevision {
	FactRetraction(Fact fact, Facet facet) {
	    super(fact, facet);
	}

	public void run() {
	    factRetracted(fact, facet); // plugin handling -- blackboard etc.
	}
    }
	

    protected FacetProviderPlugin(Properties properties, ServiceBroker sb)
    {
	this.parameters = properties;
	this.facets = new HashMap();
	this.queue = new CircularQueue();
	ThreadService tsvc = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	this.sched = tsvc.getThread(this, this, "Fact Propagater");
	this.sched.start();
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

    // Handle the facts, super-simple version
    public void run() 
    {
	long max = System.currentTimeMillis() + 500; // arbitary max run time
	boolean restart = false;
	FactRevision entry = null;
	while (true) {
	    synchronized (queue) {
		if (queue.isEmpty()) {
		    break;
		} else if (System.currentTimeMillis() >= max) {
		    restart = true;
		    break;
		} else {
		    entry = (FactRevision) queue.next();
		}
	    }
	    entry.run();
	}
	if (restart) sched.start();
    }


    // The next two methods are up calls from facets.  The actual
    // handling of facts runs in its own thread, associated with the
    // queue.   See the two subsequent methods.
    void assertFact(Facet facet, Fact fact)
    {
	FactRevision entry = new FactAssertion(fact, facet);
	synchronized (queue) {
	    queue.add(entry);
	}
	sched.start();
    }

    void retractFact(Facet facet, Fact fact)
    {
	FactRevision entry = new FactRetraction(fact, facet);
	synchronized (queue) {
	    queue.add(entry);
	}
	sched.start();
    }


    // The next two methods are run in the queue thread and provide
    // any local handling of facts.  The default is to notify all
    // facets. 

    protected void factAsserted(Fact fact, Facet assertingFacet)
    {
	// Notify the facets.  These could happen in parallel.
	synchronized (facets) {
	    Iterator itr =facets.values().iterator();
	    while (itr.hasNext()) {
		Facet facet = (Facet) itr.next();
		facet.assertFact(fact);
	    }
	}
    }

    protected void factRetracted(Fact fact, Facet retractingFacet)
    {
	// Notify the facets. These could happen in parallel
	synchronized (facets) {
	    Iterator itr =facets.values().iterator();
	    while (itr.hasNext()) {
		Facet facet = (Facet) itr.next();
		facet.retractFact(fact);
	    }
	}
    }

}