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
 * See xxx for more detailes on CoordinationArtifactTemplates.
 * Templates provide the general description of a family of
 * {@link CoordinationArtifact}s, all of which have the same set of
 * roles and parameters.
 */
public interface CoordinationArtifactTemplate
{
    /**
     * Returns the kind of Artifacts the Template can make.
    */
    public String getArtifactKind();

    /**
     * Returns true iff the Template can handle the given spec.  The
     * default implementation in {@link
     * CoordinationArtifactTemplatePlugin} returns true iff the spec's
     * ca_kind equals the Template's artifact kind.
    */
    public boolean supports(ConnectionSpec spec);

    /**
     * Does the linkage between a {@link Facet} and a {@link
     * CoordinationArtifact}, which is generally created on the fly
     * based on the spec and the player. The default implementation in
     * {@link CoordinationArtifactTemplatePlugin} looks for an
     * existing artifact that matches.  If it can't find one it
     * handles this by deferring the creation of a new
     * CoordinationArtifact to an abstract method.
     * 
    */
    public void provideFacet(ConnectionSpec spec, RolePlayer player);

    /**
     * Signals that the Artifact should execute in a blackboard
     * transaction.
    */
    public void triggerExecute();
}
