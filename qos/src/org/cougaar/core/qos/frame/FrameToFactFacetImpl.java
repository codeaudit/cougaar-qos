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

package org.cougaar.core.qos.frame;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.ca.BeanToFactFacetImpl;
import org.cougaar.core.qos.ca.ConnectionSpec;
import org.cougaar.core.qos.ca.CoordinationArtifact;
import org.cougaar.core.qos.ca.RolePlayer;
import org.cougaar.util.UnaryPredicate;


/**
 * This class is the abstract implementation of the "Consumer" facet
 * of the Frame CoordinationArtifact.  That is, it creates facts
 * representing the creation of modification of {@link Frame}s.
 * 
 * @see FrameCoordinationArtifactProvider
 */
abstract public class FrameToFactFacetImpl
    extends BeanToFactFacetImpl
{

    private String frameset_name;

    protected FrameToFactFacetImpl(CoordinationArtifact owner,
				   ServiceBroker sb,
				   ConnectionSpec spec, 
				   RolePlayer player)
    {
	super(owner, sb, spec, player);
    }

    @Override
   protected void initialize(ConnectionSpec spec)
    {
	this.frameset_name = spec.ca_parameters.getProperty("frame-set");
    }

    @Override
   protected UnaryPredicate getPredicate()
    {
	return new UnaryPredicate() {
	    /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public boolean execute(Object o) {
		return (o instanceof DataFrame) &&
		    ((Frame) o).getFrameSet().getName().equals(frameset_name);
	    }
	};
    }


}
