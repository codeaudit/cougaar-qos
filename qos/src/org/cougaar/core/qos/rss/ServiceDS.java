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

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.qos.metrics.Constants;

import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.RSS;

public class ServiceDS 
    extends CougaarDS
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
	String nodeID = node_id_svc.getMessageAddress().toString();

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
	    DataValue computedValue = values.get("Formula");
	    DataValue defaultValue = defaultValue();
	    return DataValue.mostCredible(computedValue, defaultValue);
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

    public static class CPULoadMJips1SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_MJIPS_1_SEC_AVG;
	}
    }	
    public static class CPULoadMJips10SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_MJIPS_10_SEC_AVG;
	}
    }	
    public static class CPULoadMJips100SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_MJIPS_100_SEC_AVG;
	}
    }	
    public static class CPULoadMJips1000SecAvg extends Formula {
	String getKey() {
	    return Constants.CPU_LOAD_MJIPS_1000_SEC_AVG;
	}
    }	

}

