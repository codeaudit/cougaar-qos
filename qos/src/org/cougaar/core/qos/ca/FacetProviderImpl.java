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
import java.util.List;
import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;

/**
 * This class represents the state of a particular parameterizarion of
 * some {@link CoordinationArtifact}.  It acts an intermediary between
 * {@link Facet}s, which it creates, and the Artifact they 'implement'
 * (conceptually, not in the Java sense).
 *
 */
abstract public class FacetProviderImpl
    implements FacetProvider
{
    private Properties parameters;
    private List facets = new ArrayList();
    private CoordinationArtifact owner;

    protected FacetProviderImpl(CoordinationArtifact owner, 
				ConnectionSpec spec)
    {
	if (spec.ca_parameters != null)
	    this.parameters = new Properties(spec.ca_parameters);
	this.owner = owner;
    }




    // Extensions of can make specific kinds of facets.  Here we make
    // the generic one.
    abstract protected Facet makeFacet(ConnectionSpec spec, 
				       RolePlayer player);



    // FacetProvider
    public boolean matches(ConnectionSpec spec)
    {
	if (spec.ca_parameters == null && parameters == null) return true;
	if (spec.ca_parameters == null || parameters == null) return false;
	return spec.ca_parameters.equals(parameters);
    }


    public void provideFacet(ConnectionSpec spec, 
			     RolePlayer player,
			     BlackboardService blackboard)
    {
	Facet facet = makeFacet(spec, player);
	synchronized (facets) {
	    facets.add(facet);
	}
	try {
	    blackboard.openTransaction();
	    facet.setupSubscriptions(blackboard);
	} catch (Exception ex) {
	    ex.printStackTrace();
	} finally {
	    blackboard.closeTransaction();
	}
    }

    
    // Two circumstances in which this runs:
    // (1) subscription (ResponsePred)
    // (2) new fact assertion or retraction in our fact base
    public void execute(BlackboardService blackboard) 
    {
	List copy = null;
	synchronized (facets) {
	    copy = new ArrayList(facets);
	}
	for (int i=0; i<copy.size(); i++) {
	    Facet facet = (Facet) copy.get(i);
	    facet.processFactBase(blackboard);
	    facet.execute(blackboard);
	}
    }


    public void triggerExecute()
    {
	owner.triggerExecute();
    }
}
