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


/**
 * See the package javadoc for more details on CoordinationArtifacts.
 * The primary job of the Artifact is to link {@link Facet}s to {@link
 * RolePlayer}s, as specified by a {@link ConnectionSpec}.
 */
public interface CoordinationArtifact
{
    /**
     * Returns true iff the Artifact matches given spec.  This will
     * only be called with specs that the Artifact's provider has
     * already said it supports.  The default implementation in {@link
     * CoordinationArtifactImpl} therefore returns true for all specs
     * in which the artifact-id field matches the artifact's id.
    */
    public boolean matches(ConnectionSpec spec);

    /**
     * Does the linkage between a {@link Facet}, which is generally
     * created on the fly based on the spec, and the player. The
     * default implementation in {@link CoordinationArtifactImpl}
     * handles this by deferring the creation to an abstract method.
     * 
    */
    public void provideFacet(ConnectionSpec spec, RolePlayer player);


    /**
     * Returns the {@link CoordinationArtifactProvider} kind.
     */
    public String getArtifactKind();

    /**
     * Returns the Artifact's id (not to be confused with the
     * Provider's kind).
    */
    public String getArtifactId();

}
