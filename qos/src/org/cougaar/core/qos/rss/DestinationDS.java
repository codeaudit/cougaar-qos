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

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import org.cougaar.core.qos.metrics.Constants;



import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.RSS;

public class DestinationDS 
    extends DataScope 
{
    private static final String DESTINATION = "destination".intern();


    public DestinationDS(Object[] parameters, DataScope parent) 
	throws DataScope.ParameterError
    {
	super(parameters, parent);
    }

    protected void verifyParameters(Object[] parameters) 
	throws DataScope.ParameterError
    {
	if (parameters == null || parameters.length != 1) {
	    throw new DataScope.ParameterError("DestinationDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new DataScope.ParameterError("DestinationDS: wrong parameter type");
	} else {
	    // could canonicalize here
	    String destination = (String) parameters[0];
	    bindSymbolValue(DESTINATION, destination);
	}
    }


    abstract static class Formula 
	extends DataFormula
    {

	abstract String getKey();
	
	protected DataValue defaultValue() {
	    return new DataValue(0);
	}
	

	protected void initialize(DataScope scope) {
	    super.initialize(scope);
	    String dest = (String) scope.getValue(DESTINATION);
	    String node = (String) scope.getValue(NodeDS.NODENAME);
	    String key = "Node" +KEY_SEPR+ node	
		+KEY_SEPR+ "Destination" 
		+KEY_SEPR+ dest +KEY_SEPR+ 
		getKey();

	    Object[] parameters = { key };
	    DataScopeSpec spec = new DataScopeSpec("com.bbn.quo.data.IntegraterDS", 
						   parameters);
	    DataScope dependency = RSS.instance().getDataScope(spec);
	    registerDependency(dependency, "Formula");
	}

	protected DataValue doCalculation(DataFormula.Values values) {
	    // System.err.println("### Recalculating " +getKey());
	    DataValue computedValue = values.get("Formula");
	    DataValue defaultValue = defaultValue();
	    if (computedValue.atLeastAsCredibleAs(defaultValue)) {
		return computedValue; 
	    } else {
		return defaultValue;
	    }
	}

    }


    public static class MsgFrom1SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_FROM_1_SEC_AVG;
	}
    }	

    public static class MsgFrom10SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_FROM_10_SEC_AVG;
	}
    }	

    public static class MsgFrom100SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_FROM_100_SEC_AVG;
	}
    }	

    public static class MsgFrom1000SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_FROM_1000_SEC_AVG;
	}
    }	


    public static class MsgTo1SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_TO_1_SEC_AVG;
	}
    }	

    public static class MsgTo10SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_TO_10_SEC_AVG;
	}
    }	

    public static class MsgTo100SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_TO_100_SEC_AVG;
	}
    }	

    public static class MsgTo1000SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_TO_1000_SEC_AVG;
	}
    }	


    public static class BytesFrom1SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_FROM_1_SEC_AVG;
	}
    }	

    public static class BytesFrom10SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_FROM_10_SEC_AVG;
	}
    }	

    public static class BytesFrom100SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_FROM_100_SEC_AVG;
	}
    }	

    public static class BytesFrom1000SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_FROM_1000_SEC_AVG;
	}
    }	


    public static class BytesTo1SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_TO_1_SEC_AVG;
	}
    }	

    public static class BytesTo10SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_TO_10_SEC_AVG;
	}
    }	

    public static class BytesTo100SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_TO_100_SEC_AVG;
	}
    }	

    public static class BytesTo1000SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_TO_1000_SEC_AVG;
	}
    }	


    public static class PersistSizeLast extends Formula {
	String getKey() {
	    return Constants.PERSIST_SIZE_LAST;
	}
    }	


}

