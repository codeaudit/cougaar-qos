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

import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;

/**
 * This interface describes the broker service that finds artifact
 * facets for {@link RolePlayer}s.
 */
public interface CoordinationArtifactBroker
{
    /**
     * When an Template plugin is avaliable for use, it should call
     * this. This is handled automatically for Templates that extend
     * {@link CoordinationArtifactTemplatePlugin},
    */
    public void registerCoordinationArtifactTemplate(CoordinationArtifactTemplate artifactTemplate);


    /**
     * This is an asynchronous call that binds a RolePlayer to a
     * Facet.  The desired Facet is described by the spec.  When a
     * CoordinationArtifact that can provide the Facet is available,
     * the facetAvailable callback will be invoked on the RolePlayer.
     * This callback could happen inline.
    */
    public void requestFacet(ConnectionSpec spec, RolePlayer rolePlayer);

}
    
