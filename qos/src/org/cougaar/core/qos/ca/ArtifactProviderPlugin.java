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
abstract public class ArtifactProviderPlugin
    extends ParameterizedPlugin
    implements ArtifactProvider
{

    public abstract String getArtifactKind();
    public abstract FacetProviderImpl 
	makeFacetProvider(ArtifactProviderPlugin owner, 
			  ConnectionSpec spec);

    private ArrayList artifacts;

    protected ArtifactProviderPlugin()
    {
	this.artifacts = new ArrayList();
    }

    // By default an ArtifactProvider can handle any spec
    public boolean matches(ConnectionSpec spec)
    {
	return true;
    }


    public void provideFacet(ConnectionSpec spec, RolePlayer player)
    {
	FacetProviderImpl provider = findOrMakeFacetProvider(spec);
	if (provider != null) provider.provideFacet(spec, player, blackboard);
    }

    private FacetProviderImpl findOrMakeFacetProvider(ConnectionSpec spec)
    {
	synchronized (artifacts) {
	    for (int i=0; i<artifacts.size(); i++) {
		FacetProviderImpl impl = (FacetProviderImpl) 
		    artifacts.get(i);
		if (impl.matches(spec)) return impl;
	    }
	    
	    // None around yet; make a new one
	    FacetProviderImpl impl = makeFacetProvider(this, spec);
	    artifacts.add(impl);
	    return impl;
	}
    }

    public void start()
    {
	super.start();

	ServiceBroker sb = getServiceBroker();
	FacetBroker fb = (FacetBroker) 
	    sb.getService(this, FacetBroker.class, null);
	String kind = getArtifactKind();
	fb.registerCoordinationArtifactProvider(kind, this);
	sb.releaseService(this, FacetBroker.class, fb);
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
	synchronized (artifacts) {
	    copy = new ArrayList(artifacts);
	}
	for (int i=0; i<copy.size(); i++) {
	    FacetProviderImpl impl = (FacetProviderImpl) copy.get(i);
	    impl.execute(blackboard);
	}
    }



}
