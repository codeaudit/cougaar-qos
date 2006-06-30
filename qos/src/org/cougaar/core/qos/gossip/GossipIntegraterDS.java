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

package org.cougaar.core.qos.gossip;

import com.bbn.rss.AbstractContextInstantiater;
import com.bbn.rss.ContextInstantiater;
import com.bbn.rss.DataFormula;
import com.bbn.rss.ResourceContext;
import com.bbn.rss.IntegraterDS;

/**
 * Special IntegratorDS used to integrate information from RSS
 * Datafeeds, but which the Gossip subsytem can be subscribed to
 * without messing up the standard IntegratorDS.
 */

public class GossipIntegraterDS extends IntegraterDS {

    public static void registerContext() {
	ContextInstantiater cinst = new AbstractContextInstantiater() {
		public ResourceContext instantiateContext(String[] parameters, 
							  ResourceContext parent)
		    throws ParameterError
		{
		    return new GossipIntegraterDS(parameters, parent);
		}

		public Object identifyParameters(String[] parameters) 
		{
		    if (parameters == null || parameters.length != 1) 
			return null;
		    return  parameters[0];
		}		

		
	    };
	registerContextInstantiater("GossipIntegrater", cinst);
    }

    public GossipIntegraterDS(String[] keys, ResourceContext parent) 
	throws ParameterError {
	super(keys, parent);
    }


    protected DataFormula instantiateFormula(String kind) {
	if (kind.equals("GossipFormula")) {
	    return new GossipFormula();
	} else {
	    return super.instantiateFormula(kind);
	}
    }

    static class GossipFormula extends IntegraterDS.Formula {
    }

}
