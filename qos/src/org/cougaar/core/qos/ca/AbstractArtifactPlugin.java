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
 * This class represents the base implementation of a {@link
 * CoordinationArtifact}. It registers the Artifact with the broker
 * and provides basic bookkeepiing for the {@link FacetProvider}s.
 * Instantiation of FacetProviders must be handled in subclasses, via
 * the makeFacetProvider method.
 *
 */
abstract public class AbstractArtifactPlugin
    extends ParameterizedPlugin
    implements CoordinationArtifact
{

    private ArrayList artifacts;

    /**
     *  Instantiable subclasses must provide this method.  Its job is
     *  to create new FacetProviders, given a ConnectionSpec.
     */
    abstract public FacetProvider makeFacetProvider(CoordinationArtifact owner, 
						    ConnectionSpec spec);

    protected AbstractArtifactPlugin()
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
	FacetProvider provider = findOrMakeFacetProvider(spec);
	if (provider != null) provider.provideFacet(spec, player, blackboard);
    }

    private FacetProvider findOrMakeFacetProvider(ConnectionSpec spec)
    {
	synchronized (artifacts) {
	    for (int i=0; i<artifacts.size(); i++) {
		FacetProvider fp = (FacetProvider) 
		    artifacts.get(i);
		if (fp.matches(spec)) return fp;
	    }
	    
	    // None around yet; make a new one
	    FacetProvider fp = makeFacetProvider(this, spec);
	    artifacts.add(fp);
	    return fp;
	}
    }

    public void start()
    {
	super.start();

	ServiceBroker sb = getServiceBroker();
	FacetBroker fb = (FacetBroker) 
	    sb.getService(this, FacetBroker.class, null);
	String kind = getArtifactKind();
	fb.registerCoordinationArtifact(this);
	sb.releaseService(this, FacetBroker.class, fb);
    }

    public void triggerExecute()
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
	    FacetProvider fp = (FacetProvider) copy.get(i);
	    fp.execute(blackboard);
	}
    }



}
