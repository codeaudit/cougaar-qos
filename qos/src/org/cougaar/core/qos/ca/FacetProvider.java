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

import org.cougaar.core.service.BlackboardService;

/**
 * This interface describes the equivalent of a closure of a {@link
 * CoordinationArtifact} over a runtime state.  The state includes at
 * least the defining paramaters and a set of {@link Facet}s, and can
 * include whatever else is relevant for any particular Artifact kind.
 */
public interface FacetProvider
{
    /**
     * Returns true iff the FacetProvider can provided the requested
     * {@link Facet}.  Ordinarily this depends only on the kind field
     * of the spec.
    */
    public boolean matches(ConnectionSpec spec);

    /**
     * Does the linkage between the {@link Facet} and the RolePlayer
     * for the specified role.
    */
    public void provideFacet(ConnectionSpec spec, 
			     RolePlayer player,
			     BlackboardService blackboard);


    /**
     * Runs code in a Blackboard transaction.  This will be invoked
     * from within the corresponding {@link CoordinationArtifact}
     * plugin's execute method.
     */
    public void execute(BlackboardService service);
}
