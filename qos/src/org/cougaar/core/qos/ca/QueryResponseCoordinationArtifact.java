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
 * This plugin is the generic implementation of the query side of a
 * multicast query/response rpc Cooordination Artifact.  It handles the
 * Community and Relay muck, leaving instantiable extensions only a
 * few domain-specific tasks to deal with, as described in the
 * abtract methods. 
 * 
 * In the CA scheme, multicast query/response rpc CA has two roles,
 * query and respond.  This entity provides facets for the query
 * role. The corresponding response plugin is {@link ResponseCoordArtPlugin}.
 * The flow of control is as follows: <ol> <li>A RolePlayer for the
 * query role asserts a query as a Fact. </li> <li>The FacetProvider
 * for the query detects the new Fact, constructs a QueryRelay from
 * the Fact and transmits in the usual way through the
 * Blackboard. </li> <li>Each FacetProvider for the response role
 * receives the QueryRelay in the usual way on the Blackboard and
 * asserts a corresponding Fact for the query. </li> <li>The
 * RolePlayers for the response role detect the new query Fact and
 * assert a response Fact. </li><li>The FacetProviders for the
 * response detect the new response Fact, constructs a ResponseRelay
 * from the Fact and transmits in the usual way through the
 * Blackboard. </li><li>The FacetProvider for the query role receives
 * the ResponseRelay in the usual way on the Blackboard and asserts a
 * corresponding Fact for the response. </li><li>The RolePlayer for
 * the query role detects the new response Fact and process it. </li>
 * </ol>
 *
 */
abstract public class QueryResponseCoordinationArtifact
    extends ArtifactProviderPlugin
    implements QueryCoordArtConstants
{
    public QueryResponseCoordinationArtifact() 
    {
    }
    


    abstract protected class QueryResponseFacetProvider 
	extends FacetProviderImpl
    {
	/**
	 * Make a CA-specific QueryFacet.
	 */
	public abstract QueryFacet makeQueryFacet(ServiceBroker sb,
						  ConnectionSpec spec, 
						  RolePlayer player);


	/**
	 * Make a CA-specific ResponseFacet.
	 */
	public abstract ResponseFacet makeResponseFacet(ServiceBroker sb,
							ConnectionSpec spec, 
							RolePlayer player);

   
	protected QueryResponseFacetProvider(ArtifactProviderPlugin owner,
					     ConnectionSpec spec)
	{
	    super(owner, spec);
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
