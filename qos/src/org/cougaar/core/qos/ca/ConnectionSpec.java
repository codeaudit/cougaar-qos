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

/**
 * This simple struct provides a specification for a role in
 * particular {@link CoordinationArtifact}.  It's used by {@link
 * RolePlayer}s to get themselves connected.
 */
public class ConnectionSpec
{
    /**
     * The Coordination Artifact kind.  Each kind has one or more
     * roles, and each role is visible to clients as a 'facet'.  The
     * specific role for any particular Connection spec is given in
     * the 'role' instance variable.
    */
    public String ca_kind;

    /**
     * The specific role for the given Coordination Artifact kind.
     */
    public String role;

    /**
     * A plist that can distinguish the instances of the given
     * Coordination Artifact kind from one another.
    */
    public Properties ca_parameters;

    /**
     * A plist includes all the instance-specific data for a
     * particular Facet instance.
    */
    public Properties role_parameters;


    public ConnectionSpec(String ca_kind, Properties ca_parameters, 
			  String role, Properties role_parameters)
			  
    {
	this.ca_kind = ca_kind;
	this.ca_parameters = ca_parameters;
	this.role = role;
	this.role_parameters = role_parameters;
    }

    public String logString()
    {
	return "<CA Spec " +ca_kind+  ":" +role +">";
    }
}
