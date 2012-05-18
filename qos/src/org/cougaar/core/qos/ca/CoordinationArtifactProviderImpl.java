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

import org.cougaar.core.component.ServiceBroker;

/**
 * This class represents the base implementation of a {@link
 * CoordinationArtifactProvider}. It registers the Provider with the
 * broker and provides basic bookkeepiing for the {@link
 * CoordinationArtifact}s.  Instantiation of CoordinationArtifactss
 * must be handled in subclasses, via the makeArtifact method.
 *
 */
abstract public class CoordinationArtifactProviderImpl
    implements CoordinationArtifactProvider
{

    private ArrayList artifacts;
    private ServiceBroker sb;
    private String kind;

    /**
     *  Instantiable subclasses must provide this method.  Its job is
     *  to create new CoordinationArtifacts, given a ConnectionSpec.
     */
    abstract public CoordinationArtifact makeArtifact(ConnectionSpec spec);

    protected CoordinationArtifactProviderImpl(String kind, ServiceBroker sb)
    {
	this.artifacts = new ArrayList();
	this.sb = sb;
	this.kind = kind;
	CoordinationArtifactBroker cab = sb.getService(this, CoordinationArtifactBroker.class, null);
	cab.registerCoordinationArtifactProvider(this);
	sb.releaseService(this, CoordinationArtifactBroker.class, cab);
    }


    protected ServiceBroker getServiceBroker()
    {
	return sb;
    }

    public String getArtifactKind()
    {
	return kind;
    }

    // By default handle all specs of the right kind
    public boolean supports(ConnectionSpec spec)
    {
	return spec.ca_kind.equals(getArtifactKind());
    }


    public void provideFacet(ConnectionSpec spec, RolePlayer player)
    {
	CoordinationArtifact artifact = findOrMakeArtifact(spec);
	if (artifact != null) artifact.provideFacet(spec, player);
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


}
