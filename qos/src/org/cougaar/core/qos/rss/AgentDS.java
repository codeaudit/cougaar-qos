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
import org.cougaar.core.service.TopologyEntry;
import org.cougaar.core.service.TopologyReaderService;
import org.cougaar.core.qos.metrics.Constants;

import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.RSS;
import com.bbn.quo.data.RSSUtils;

public class AgentDS 
    extends DataScope 
{
    private static final String AGENTNAME = "agentname".intern();


    public AgentDS(Object[] parameters, DataScope parent) 
	throws DataScope.ParameterError
    {
	super(parameters, parent);
    }

    protected boolean useParentPath() {
	return false;
    }

    // Node DataScopes can be the first element in a path.  They must
    // find or make the corresponding HostDS and return that as the
    // preferred parent.
    protected DataScope preferredParent(RSS root) {
	ServiceBroker sb = (ServiceBroker) root.getProperty("ServiceBroker");
	TopologyReaderService svc = (TopologyReaderService)
	    sb.getService(this,TopologyReaderService.class, null);
	String agentname = (String) getSymbolValue(AGENTNAME);
	TopologyEntry entry = svc.getEntryForAgent(agentname);
        String node = entry != null ? entry.getNode() : null;

	// What do we do if the node isn't known?
	if (node == null) {
	    node = "FosterNode"; // nice
	}

	// System.err.println("### Node of " +agentname+ "=" +node);



	Object[] params = { node };
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
	    throw new DataScope.ParameterError("NodeDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new DataScope.ParameterError("NodeDS: wrong parameter type");
	} else {
	    // could canonicalize here
	    String agentname = (String) parameters[0];
	    // System.err.println("#### Created AgentDS for " +agentname);
	    bindSymbolValue(AGENTNAME, agentname);
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
	    String agentName = (String) scope.getValue(AGENTNAME);
	    String key = "Agent" +KEY_SEPR+ agentName +KEY_SEPR+ getKey();

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

    abstract static class MonotonicLongFormula extends Formula {
	int granularity = 1000;
	protected DataValue doCalculation(DataFormula.Values vals){
	    DataValue newValue = vals.get("Formula");
	    DataValue cachedValue= getCachedValue();
	    long  longNew = newValue.getLongValue();
	    long  longCached = cachedValue.getLongValue();
	    if (longNew - longCached > granularity) {
		return newValue; 
	    } else {
		return cachedValue;
	    }	    
	}
    }

    abstract static class AlarmFormula extends DataFormula {

	abstract String getKey();
	
	protected DataValue defaultValue() {
	    return new DataValue(0);
	}
	

	protected void initialize(DataScope scope) {
	    super.initialize(scope);
	    DataFormula baseFormula=getScope().getFormula(getKey());
	    registerDependency(baseFormula, "Formula");
	    DataFormula alarm = RSSUtils.getPathFormula("Alarm():OneSecond");
	    registerDependency(alarm, "Alarm");
	}

	protected DataValue doCalculation(DataFormula.Values vals)
	{
	    long sendTime = vals.get("Formula").getLongValue();
	    long alarmTime = vals.get("Alarm").getLongValue();
	    DataValue elapsed = new DataValue(0);

// 	    String agentName = (String) getScope().getValue(AGENTNAME);
// 	    System.err.println("Agent "+agentName+ 
// 			       " send="+sendTime+
// 			       " alarm="+alarmTime+
// 			       " delta=" + (alarmTime - sendTime));

	    if (alarmTime > sendTime) {
		long seconds= (long) Math.floor((alarmTime-sendTime)/1000.0);
		elapsed.setValue(seconds);
	    }
	    elapsed.setCredibility(vals.minCredibility());
	    return elapsed;
	}

	
    }


    public static class LastHeard extends AlarmFormula {
	String getKey() {
	    return "HeardTime";
	}
    }

    public static class LastSpoke extends AlarmFormula {
	String getKey() {
	    return "SpokeTime";
	}
    }


    public static class OneSecondLoadAvg extends Formula {
	String getKey() {
	    return Constants.ONE_SEC_LOAD_AVG;
	}
    }	

    // JAZ both HeardTime and SpokeTime need to be Monotonic, and they
    // need to be hooked into LastHeard and LastSpoke

    //The raw integrater values can not be used because there is no
    //ordering between threads, so an old thread could publish a
    //HeardTime that is actually before the current HeardTime
    public static class HeardTime extends MonotonicLongFormula {
	String getKey() {
	    return "HeardTime";
	}
    }	

    public static class SpokeTime extends MonotonicLongFormula {
	String getKey() {
	    return "SpokeTime";
	}
    }	



}

