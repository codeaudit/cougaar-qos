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
 * This class represents the piece of a Coordination Artifact that
 * exists in one particular jvm (the full artifact is distributed).
 * In the general case it's nothing but a set of parameters that are
 * used to distinguish this one from others of the same kind, and a
 * set of roles, each of which can be linked to one or more
 * RolePlayers through facets.  Subclasses can provide domain-specific
 * state.
 *
 */
abstract public class FacetProviderPlugin
    extends ParameterizedPlugin
    implements FacetProvider
{
    private Properties parameters;
    private List facets = new ArrayList();

    protected FacetProviderPlugin()
    {
	this.parameters = new Properties();
    }

    /**
     * The implementation of this method in instantiable extensions
     * would return the name of the specific Coordination Artifact.
     */
    public abstract String getArtifactKind();


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


    // Extensions of can make specific kinds of facets.  Here we make
    // the generic one.
    abstract protected Facet makeClientFacet(ConnectionSpec spec, 
					     RolePlayer player);



    // FacetProvider
    public boolean matches(ConnectionSpec spec)
    {
	if (spec.ca_parameters == null && parameters == null) return true;
	if (spec.ca_parameters == null || parameters == null) return false;
	return spec.ca_parameters.equals(parameters);
    }


    public void provideFacet(ConnectionSpec spec, RolePlayer player)
    {
	Facet facet = makeClientFacet(spec, player);
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

    protected void triggerExecute()
    {
	BlackboardService bbs = getBlackboardService();
	if (bbs != null) {
	    bbs.signalClientActivity();
	} else {
	}
    }

    protected void setupSubscriptions() 
    {
    }
    
    // Two circumstances in which this runs:
    // (1) subscription (ResponsePred)
    // (2) new fact assertion or retraction in our fact base
    protected void execute() 
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



}
