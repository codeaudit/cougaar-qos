/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.core.qos.rss;

import org.cougaar.core.qos.metrics.Constants;

import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.RSS;


public class AgentFlowDS 
    extends CougaarDS 
{
    private static final String SOURCE_AGENT = "sourceAgent".intern();
    private static final String DESTINATION_AGENT = "destinationAgent".intern();
    private static final DataValue NO_VALUE = DataValue.NO_VALUE;


    public AgentFlowDS(Object[] parameters, DataScope parent) 
	throws DataScope.ParameterError
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
    protected DataScope preferredParent(RSS root) {
	return root;
    }

    // Two Parameter which are Agent Names
    protected void verifyParameters(Object[] parameters) 
	throws DataScope.ParameterError
    {
	if (parameters == null || parameters.length != 2) {
	    throw new DataScope.ParameterError("AgentFlowDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new DataScope.ParameterError("AgentFlowDS: wrong parameter 1 type");
	} else {
	    String source = (String) parameters[0];
	    bindSymbolValue(SOURCE_AGENT, source);
	}
	if (!(parameters[1] instanceof String)) {
	    throw new DataScope.ParameterError("AgentFlowDS: wrong parameter 2 type");
	} else {
	    String destination = (String) parameters[1];
	    bindSymbolValue(DESTINATION_AGENT, destination);
	}
    }

    abstract static class Formula 
	extends DataFormula
    {

	abstract String getKey();
	
	protected DataValue defaultValue() {
	    return NO_VALUE;
	}

	protected void initialize(DataScope scope) {
	    super.initialize(scope);
	    String sourceAgent = (String) scope.getValue(SOURCE_AGENT);
	    String destinationAgent = (String) scope.getValue(DESTINATION_AGENT);
	    String key = "AgentFlow" +KEY_SEPR+ 
		sourceAgent  +KEY_SEPR+ 
		destinationAgent +KEY_SEPR+ 
		getKey();

	    Object[] parameters = { key };
	    DataScopeSpec spec = new DataScopeSpec("com.bbn.quo.data.IntegraterDS", 
						   parameters);
	    DataScope dependency = RSS.instance().getDataScope(spec);
	    registerDependency(dependency, "Formula");
	}

	protected DataValue doCalculation(DataFormula.Values values) {
	    DataValue computedValue = values.get("Formula");
	    DataValue defaultValue = defaultValue();
	    return DataValue.mostCredible(computedValue, defaultValue);
	}

    }


    public static class MsgRate1SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_RATE_1_SEC_AVG;
	}
    }	

    public static class MsgRate10SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_RATE_10_SEC_AVG;
	}
    }	

    public static class MsgRate100SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_RATE_100_SEC_AVG;
	}
    }	

    public static class MsgRate1000SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_RATE_1000_SEC_AVG;
	}
    }	


    public static class ByteRate1SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTE_RATE_1_SEC_AVG;
	}
    }	

    public static class ByteRate10SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTE_RATE_10_SEC_AVG;
	}
    }	

    public static class ByteRate100SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTE_RATE_100_SEC_AVG;
	}
    }	

    public static class ByteRate1000SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTE_RATE_1000_SEC_AVG;
	}
    }	


}

