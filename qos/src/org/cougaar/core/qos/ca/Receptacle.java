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
 * A Receptacle represents a {@link RolePlayer}'s viewport into some
 * role of a {@link CoordinationArtifact}.  This is the player's only
 * interface to the artifact.  The correspdonding entity in the
 * Artifact is a {@link Facet}. This naming convention is deliberately
 * suggestive of the CORBA Component Model (CCM), since the functions
 * of these two interfaces are roughly equivalent to the similarly
 * named entities in CCM.  By analogy, the {@link
 * CoordinationArtifact} itself corresponds to a CCM Component.
 */
public interface Receptacle extends BlackboardExecutor
{
    /**
     * Used by clients to assert a new fact into the Facet's
     * fact-base. The fact itself is a blackbox.
     */
    public void assertFact(Object fact);


    /**
     * Used by clients to retract a fact from the Facet's
     * fact-base. The fact itself is a blackbox.
     */
    public void retractFact(Object fact);


    /**
     * Returns the kind of the {@link CoordinationArtifactProvider}
    */
    public String getArtifactKind();

    /**
     * Returns the id of the specific {@link CoordinationArtifact}
     * (not to be confused with the Provider kind) which owns this
     * Facet.
    */
    public String getArtifactId();

}
