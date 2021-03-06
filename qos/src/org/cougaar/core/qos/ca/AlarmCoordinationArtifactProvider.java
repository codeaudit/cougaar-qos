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

import org.cougaar.core.component.ServiceBroker;

/**
 * Coordination Artifact for COUGAAR Alarms
 */
abstract public class AlarmCoordinationArtifactProvider
    extends CoordinationArtifactProviderImpl
{

    private static final String AlarmCA = "AlarmCA";
    public static final String USE_THREADS = "UseThreads";

    public AlarmCoordinationArtifactProvider(ServiceBroker sb) 
    {
	super(AlarmCA, sb);
    }
    

    abstract protected class AlarmCA 
	extends CoordinationArtifactImpl
	implements CoordArtConstants
    {
	private static final String SleeperRole = "Sleeper";
	private String artifactId;


	protected AlarmCA(CoordinationArtifactProvider owner,
			  ConnectionSpec spec)
	{
	    super(owner, spec);

	    // User the community type as the artifactId for now
	    this.artifactId = spec.ca_parameters.getProperty(ArtifactIdAttr);
	}
	
	abstract protected SleeperFacet 
	    makeThreadSleeperFacet(ServiceBroker sb,
				   ConnectionSpec spec, 
				   RolePlayer player);

	abstract protected SleeperFacet 
	    makeAlarmSleeperFacet(ServiceBroker sb,
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
	    if (spec.role.equals(SleeperRole)) {
		String useThreads = spec.ca_parameters.getProperty(USE_THREADS);
		if (useThreads != null && useThreads.equalsIgnoreCase("true"))
		    return makeThreadSleeperFacet(sb, spec, player);
		else
		    return makeAlarmSleeperFacet(sb, spec, player);
	    } else {
		throw new RuntimeException("Bogus role in spec: " + spec);
	    }
	}
    }

}
