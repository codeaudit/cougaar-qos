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
import java.util.Observer;


/**
 * Default implementation for the client Facet.
 */
public class FacetImpl // should be abstract
    implements Facet,  Observer
{
    private RolePlayer player;
    private ConnectionSpec spec;
    private FacetProviderPlugin owner;
    private SimpleQueue factQueue;

    private static class SimpleQueue extends LinkedList {
	Object next() {
	    return removeFirst();
	}
    }

    protected FacetImpl(FacetProviderPlugin owner, 
			ConnectionSpec spec, 
			RolePlayer player)
    {
	this.player = player;
	this.spec = spec;
	this.owner = owner;
	// The factQueue consists of FactRevision instances
	this.factQueue = new SimpleQueue();
    }


    protected RolePlayer getPlayer()
    {
	return player;
    }

    // Remove this when this class becomes abstract
    public void update(java.util.Observable obs, Object value)
    {
    }


    protected void linkPlayer()
    {
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


}
