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
 * See xxx for more detailes on CoordinationArtifacts.  At the most
 * abstract level it has three jobs.  It must be able to identity its
 * kind, it must be able to decide whether or not it can handle any
 * given {@link ConnectionSpec}, and it if it can, it must handle the
 * linkage to a {@link RolePlayer} for that spec.
 */
public interface CoordinationArtifact
{
    /**
     * Returns the logical kind/type of the Artifact (not to be
     * confused with the class).
    */
    public String getArtifactKind();

    /**
     * Returns true iff the Artifact can handle the given spec.  This
     * will only be called with specs whose ca_kind field is already
     * known to match the Artifact's kind.  The default implementation
     * in {@link AbstractArtifactPlugin} therefore returns true
     * for all specs.
    */
    public boolean matches(ConnectionSpec spec);

    /**
     * Does the linkage between a {@link Facet}, which is generally
     * created on the fly based on the spec and the player. The
     * default implementation in {@link AbstractArtifactPlugin}
     * handles this by deferring the creation of {@link
     * FacetProvider}s to an abstract method.
     * 
    */
    public void provideFacet(ConnectionSpec spec, RolePlayer player);


    /**
     * Signals that the Artifact should execute in a blackboard
     * transaction.
    */
    public void triggerExecute();

}
