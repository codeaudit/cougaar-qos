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
 * This plugin is the generic implementation of a multicast
 * query/response rpc {@link CoordinationArtifactTemplate}.  The
 * {@link CoordinationArtifact}s it creates handles the Community and
 * Relay muck, leaving instantiable extensions only a few
 * domain-specific tasks to deal with, as described in the abtract
 * methods.
 * 
 * In the CA scheme, multicast query/response rpc CA has two roles,
 * query and respond.  The flow of control is as follows: <ol> <li>A
 * RolePlayer for the query role asserts a query as a Fact. </li>
 * <li>The Facet for the query detects the new Fact, constructs a
 * QueryRelay from the Fact and transmits in the usual way through the
 * Blackboard. </li> <li>Each Facet for the response role receives the
 * QueryRelay in the usual way on the Blackboard and asserts a
 * corresponding Fact for the query. </li> <li>The RolePlayers for the
 * response role detect the new query Fact and assert a response
 * Fact. </li><li>The Facets for the response detect the new
 * response Fact, constructs a ResponseRelay from the Fact and
 * transmits in the usual way through the Blackboard. </li><li>The
 * Facet for the query role receives the ResponseRelay in the
 * usual way on the Blackboard and asserts a corresponding Fact for
 * the response. </li><li>The RolePlayer for the query role detects
 * the new response Fact and processes it. </li> </ol>
 *
 */
abstract public class QueryResponseCoordinationArtifactTemplate
    extends CoordinationArtifactTemplatePlugin
    implements QueryCoordArtConstants
{
    public QueryResponseCoordinationArtifactTemplate() 
    {
    }
    


    abstract protected class QueryResponseCA 
	extends CoordinationArtifactImpl
    {
	private String artifactId;


	protected QueryResponseCA(CoordinationArtifactTemplate owner,
				  ConnectionSpec spec)
	{
	    super(owner, spec);

	    // User the community type as the artifactId for now
	    this.artifactId = spec.ca_parameters.getProperty(ArtifactIdAttr);
	}
	
	public String getArtifactId()
	{
	    return artifactId;
	}


	protected QueryFacet makeQueryFacet(ServiceBroker sb,
					    ConnectionSpec spec, 
					    RolePlayer player)
	{
	    return new QueryFacet(this, sb, spec, player);
	}


	/**
	 * Make a CA-specific ResponseFacet.
	 */
	protected ResponseFacet makeResponseFacet(ServiceBroker sb,
						  ConnectionSpec spec, 
						  RolePlayer player)
	{
	    return new ResponseFacet(this, sb, spec, player);
	}

   
	protected Facet makeFacet(ConnectionSpec spec, RolePlayer player)
	{
	    ServiceBroker sb = getServiceBroker();
	    if (spec.role.equals(RequestorRole))
		return makeQueryFacet(sb, spec, player);
	    else if (spec.role.equals(ReceiverRole))
		return makeResponseFacet(sb, spec, player);
	    else
		throw new RuntimeException("Bogus role in spec: " + spec);
	}
    }

}
