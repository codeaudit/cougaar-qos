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
 * CoordinationArtifactTemplate}. It registers the Template with the
 * broker and provides basic bookkeepiing for the {@link
 * CoordinationArtifact}s.  Instantiation of CoordinationArtifactss
 * must be handled in subclasses, via the makeArtifact method.
 *
 */
abstract public class CoordinationArtifactTemplatePlugin
    extends ParameterizedPlugin
    implements CoordinationArtifactTemplate
{

    private ArrayList artifacts;
    /**
     *  Instantiable subclasses must provide this method.  Its job is
     *  to create new CoordinationArtifacts, given a ConnectionSpec.
     */
    abstract public CoordinationArtifact makeArtifact(ConnectionSpec spec);

    protected CoordinationArtifactTemplatePlugin()
    {
	this.artifacts = new ArrayList();
    }

    // By default handle all specs of the right kind
    public boolean supports(ConnectionSpec spec)
    {
	return spec.ca_kind.equals(getArtifactKind());
    }


    public void provideFacet(ConnectionSpec spec, RolePlayer player)
    {
	CoordinationArtifact artifact = findOrMakeArtifact(spec);
	if (artifact != null) artifact.provideFacet(spec, player, blackboard);
    }

    private CoordinationArtifact findOrMakeArtifact(ConnectionSpec spec)
    {
	synchronized (artifacts) {
	    for (int i=0; i<artifacts.size(); i++) {
		CoordinationArtifact ca = (CoordinationArtifact) 
		    artifacts.get(i);
		if (ca.matches(spec)) return ca;
	    }
	    
	    // None around yet; make a new one
	    CoordinationArtifact ca = makeArtifact(spec);
	    artifacts.add(ca);
	    return ca;
	}
    }

    public void start()
    {
	super.start();

	ServiceBroker sb = getServiceBroker();
	CoordinationArtifactBroker cab = (CoordinationArtifactBroker) 
	    sb.getService(this, CoordinationArtifactBroker.class, null);
	String kind = getArtifactKind();
	cab.registerCoordinationArtifactTemplate(this);
	sb.releaseService(this, CoordinationArtifactBroker.class, cab);
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
	    CoordinationArtifact ca = (CoordinationArtifact) copy.get(i);
	    ca.execute(blackboard);
	}
	for (int i=0; i<copy.size(); i++) {
	    CoordinationArtifact ca = (CoordinationArtifact) copy.get(i);
	    ca.runRuleEngine(blackboard);
	}
	for (int i=0; i<copy.size(); i++) {
	    CoordinationArtifact ca = (CoordinationArtifact) copy.get(i);
	    ca.processFactBase(blackboard);
	}
    }



}
