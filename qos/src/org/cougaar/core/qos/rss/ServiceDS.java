/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import org.cougaar.core.component.ServiceBroker;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.service.TopologyEntry;
import org.cougaar.core.service.TopologyReaderService;
import org.cougaar.core.qos.metrics.Constants;

import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.RSS;
import com.bbn.quo.data.RSSUtils;

public class ServiceDS 
    extends DataScope 
{
    private static final String SERVICENAME = "servicename".intern();


    public ServiceDS(Object[] parameters, DataScope parent) 
	throws DataScope.ParameterError
    {
	super(parameters, parent);
    }

    protected boolean useParentPath() {
	return false;
    }

    // Service DataScopes can be the first element in a path.  They must
    // find or make the corresponding NodeDS and return that as the
    // preferred parent.
    protected DataScope preferredParent(RSS root) {
	ServiceBroker sb = (ServiceBroker) root.getProperty("ServiceBroker");
	NodeIdentificationService node_id_svc = (NodeIdentificationService)
	    sb.getService(this, NodeIdentificationService.class, null);
	String nodeID = node_id_svc.getNodeIdentifier().toString();

	Object[] params = { nodeID };
	DataScopeSpec spec = new DataScopeSpec("Node", params);
	DataScopeSpec[] path = { spec } ;
	DataScope parent = root.getDataScope(path);
	setParent(parent);
	return parent;
    }


    protected void verifyParameters(Object[] parameters) 
	throws DataScope.ParameterError
    {
	if (parameters == null || parameters.length != 1) {
	    throw new DataScope.ParameterError("ServiceDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new DataScope.ParameterError("ServiceDS: wrong parameter type");
	} else {
	    // could canonicalize here
	    String servicename = (String) parameters[0];
	    // System.err.println("#### Created ServiceDS for " +servicename);
	    bindSymbolValue(SERVICENAME, servicename);
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
	    String serviceName = (String) scope.getValue(SERVICENAME);
	    String key = "Service" +KEY_SEPR+ serviceName +KEY_SEPR+ getKey();

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



    public static class CPULoadAvg1SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_AVG_1_SEC_AVG;
	}
    }	
    public static class CPULoadAvg10SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_AVG_10_SEC_AVG;
	}
    }	
    public static class CPULoadAvg100SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_AVG_100_SEC_AVG;
	}
    }	
    public static class CPULoadAvg1000SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_AVG_1000_SEC_AVG;
	}
    }	

    public static class CPULoadJips1SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_JIPS_1_SEC_AVG;
	}
    }	
    public static class CPULoadJips10SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_JIPS_10_SEC_AVG;
	}
    }	
    public static class CPULoadJips100SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_JIPS_100_SEC_AVG;
	}
    }	
    public static class CPULoadJips1000SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_JIPS_1000_SEC_AVG;
	}
    }	

}

