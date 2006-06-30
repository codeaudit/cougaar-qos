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

package org.cougaar.core.qos.rss;

import org.cougaar.core.qos.metrics.Constants;

import com.bbn.rss.AbstractContextInstantiater;
import com.bbn.rss.ContextInstantiater;
import com.bbn.rss.DataFormula;
import com.bbn.rss.DataValue;
import com.bbn.rss.RSS;
import com.bbn.rss.ResourceContext;


/**
 * This RSS ResourceContext represents message flow between two
 * Agents.  This is a top-level context in the RSS inheritance tree,
 * and is identified by a pair of Agent names (source and destination,
 * in that order).  It supports two messaging rate formulas, for byte
 * counst and message counts respectively
 */
public class AgentFlowDS 
    extends CougaarDS 
{

    static void register()
    {
	ContextInstantiater cinst = new AbstractContextInstantiater() {
		public ResourceContext instantiateContext(String[] parameters, 
							  ResourceContext parent)
		    throws ParameterError
		{
		    return new AgentFlowDS(parameters, parent);
		}

		public Object identifyParameters(String[] parameters) 
		{
		    if (parameters == null || parameters.length != 1) 
			return null;
		    String src = (String) parameters[0];
		    String dst = (String) parameters[1];
		    return src +"->"+ dst;
		}		

		
	    };
	registerContextInstantiater("AgentFlow", cinst);
    }

    private static final String SOURCE_AGENT = "sourceAgent".intern();
    private static final String DESTINATION_AGENT = "destinationAgent".intern();
    private static final DataValue NO_VALUE = DataValue.NO_VALUE;


    public AgentFlowDS(String[] parameters, ResourceContext parent) 
	throws ParameterError
    {
	super(parameters, parent);
    }

    protected boolean useParentPath() {
	return false;
    }

    //AgentFlow should really to have an IpFlow as a Parent so that
    //they can get the path capacity.  Also they need to have the
    //Source and Destination hosts as perents in order to predict serialization cost
    // The Flow Layering needs new Modeling primitives TBD later.
    //JAZ Standalone for now
    protected ResourceContext preferredParent(RSS root) {
	return root;
    }

    // Two Parameter which are Agent Names
    protected void verifyParameters(String[] parameters) 
	throws ParameterError
    {
	String source = null;
	String destination = null;
	if (parameters == null || parameters.length != 2) {
	    throw new ParameterError("AgentFlowDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new ParameterError("AgentFlowDS: wrong parameter 1 type");
	} else {
	    source = (String) parameters[0];
	    bindSymbolValue(SOURCE_AGENT, source);
	}
	if (!(parameters[1] instanceof String)) {
	    throw new ParameterError("AgentFlowDS: wrong parameter 2 type");
	} else {
	    destination = (String) parameters[1];
	    bindSymbolValue(DESTINATION_AGENT, destination);
	    historyPrefix = "AgentFlow" +KEY_SEPR+ 
		source  +KEY_SEPR+ 
		destination;
	}
    }


    protected DataFormula instantiateFormula(String kind)
    {
	if (kind.equals(Constants.MSG_RATE) ||
	    kind.equals(Constants.BYTE_RATE)) {
	    return new DecayingHistoryFormula(historyPrefix, kind);
	} else {
	    return null;
	}
    }


}

