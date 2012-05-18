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

package org.cougaar.core.qos.frame;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.ca.ConnectionSpec;
import org.cougaar.core.qos.ca.CoordArtConstants;
import org.cougaar.core.qos.ca.CoordinationArtifactImpl;
import org.cougaar.core.qos.ca.CoordinationArtifactProvider;
import org.cougaar.core.qos.ca.CoordinationArtifactProviderImpl;
import org.cougaar.core.qos.ca.Facet;
import org.cougaar.core.qos.ca.RolePlayer;

/**
 * This class is implements a Coordination Artifact for reading and
 * writing {@link Frame}s.  The two roles are Producer (generates new
 * Frames and modifications to Frames, typically derived from facts),
 * and Consumer (takes action when Frames are created or modified,
 * typically by asserting facts).
 *
 * @see FactToFrameFacetImpl
 * @see FrameToFactFacetImpl
 */
abstract public class FrameCoordinationArtifactProvider
    extends CoordinationArtifactProviderImpl
{

    private static final String FrameCA = "FrameCA";
    private static final String ConsumerRole = "Consumer";
    private static final String ProducerRole = "Producer";

    public FrameCoordinationArtifactProvider(ServiceBroker sb) 
    {
	super(FrameCA, sb);
    }
    

    abstract protected class FrameCA 
	extends CoordinationArtifactImpl
	implements CoordArtConstants
    {
	private String artifactId;


	protected FrameCA(CoordinationArtifactProvider owner,
			  ConnectionSpec spec)
	{
	    super(owner, spec);
	    this.artifactId = spec.ca_parameters.getProperty(ArtifactIdAttr);
	}
	
	abstract protected FrameToFactFacetImpl
	    makeFrameToFactFacet(ServiceBroker sb,
				 ConnectionSpec spec, 
				 RolePlayer player);

	abstract protected FactToFrameFacetImpl
	    makeFactToFrameFacet(ServiceBroker sb,
				 ConnectionSpec spec, 
				 RolePlayer player);

	public String getArtifactId()
	{
	    return artifactId;
	}


	@Override
   protected Facet makeFacet(ConnectionSpec spec, RolePlayer player)
	{
	    ServiceBroker sb = getServiceBroker();
	    if (spec.role.equals(ConsumerRole)) {
		return makeFrameToFactFacet(sb, spec, player);
	    } else if (spec.role.equals(ProducerRole)) {
		return makeFactToFrameFacet(sb, spec, player);
	    } else {
		throw new RuntimeException("Bogus role in spec: " + spec);
	    }
	}
    }

}
