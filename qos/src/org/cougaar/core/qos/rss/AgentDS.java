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

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.core.service.wp.AddressEntry;
import org.cougaar.core.service.wp.WhitePagesService;

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
    static final String TOPOLOGY = "topology";


    public AgentDS(Object[] parameters, DataScope parent) 
	throws DataScope.ParameterError
    {
	super(parameters, parent);
    }

    protected boolean useParentPath() {
	return false;
    }

    String getAgentName() {
	return (String) getSymbolValue(AGENTNAME);
    }

    // Node DataScopes can be the first element in a path.  They must
    // find or make the corresponding HostDS and return that as the
    // preferred parent.
    protected DataScope preferredParent(RSS root) {
	ServiceBroker sb = (ServiceBroker) root.getProperty("ServiceBroker");
	WhitePagesService svc = (WhitePagesService)
	    sb.getService(this, WhitePagesService.class, null);
	String agentname = (String) getSymbolValue(AGENTNAME);
        String node = null;
	try {
	    AddressEntry entry = svc.get(agentname, TOPOLOGY);
	    if (entry == null) {
		System.err.println("# Can't find node for agent " +agentname);
		node = "FosterNode";
	    } else {
		node = entry.getURI().getPath().substring(1);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    node = "FosterNode";
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
	    throw new DataScope.ParameterError("AgentDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new DataScope.ParameterError("AgentDS: wrong parameter type");
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
	    return DataValue.mostCredible(computedValue, defaultValue);
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
	    DataValue formulaDV = vals.get("Formula");
	    DataValue alarmDV = vals.get("Alarm");
	    if (formulaDV == null || alarmDV == null) {
		// Callback before both dependencies were registered.
		// Punt.
		return DataValue.NO_VALUE;
	    }
	    long sendTime = formulaDV.getLongValue();
	    long alarmTime = alarmDV.getLongValue();
	    long elapsed = 0;

// 	    String agentName = (String) getScope().getValue(AGENTNAME);
// 	    System.err.println("Agent "+agentName+ 
// 			       " send="+sendTime+
// 			       " alarm="+alarmTime+
// 			       " delta=" + (alarmTime - sendTime));

	    if (alarmTime > sendTime) {
		elapsed = (long) Math.floor((alarmTime-sendTime)/1000.0);
	    }
	    return new DataValue(elapsed, vals.minCredibility());
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

    public static class LastSpokeError extends AlarmFormula {
	String getKey() {
	    return "SpokeErrorTime";
	}
    }

    // HeardTime, SpokeTime and SpokeErrorTime need to be Monotonic, and they
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

    public static class SpokeErrorTime extends MonotonicLongFormula {
	String getKey() {
	    return "SpokeErrorTime";
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


    public static class MsgIn1SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_IN_1_SEC_AVG;
	}
    }	

    public static class MsgIn10SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_IN_10_SEC_AVG;
	}
    }	

    public static class MsgIn100SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_IN_100_SEC_AVG;
	}
    }	

    public static class MsgIn1000SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_IN_1000_SEC_AVG;
	}
    }	


    public static class MsgOut1SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_OUT_1_SEC_AVG;
	}
    }	

    public static class MsgOut10SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_OUT_10_SEC_AVG;
	}
    }	

    public static class MsgOut100SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_OUT_100_SEC_AVG;
	}
    }	

    public static class MsgOut1000SecAvg extends Formula {
	String getKey() {
	    return Constants.MSG_OUT_1000_SEC_AVG;
	}
    }	


    public static class BytesIn1SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_IN_1_SEC_AVG;
	}
    }	

    public static class BytesIn10SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_IN_10_SEC_AVG;
	}
    }	

    public static class BytesIn100SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_IN_100_SEC_AVG;
	}
    }	

    public static class BytesIn1000SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_IN_1000_SEC_AVG;
	}
    }	


    public static class BytesOut1SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_OUT_1_SEC_AVG;
	}
    }	

    public static class BytesOut10SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_OUT_10_SEC_AVG;
	}
    }	

    public static class BytesOut100SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_OUT_100_SEC_AVG;
	}
    }	

    public static class BytesOut1000SecAvg extends Formula {
	String getKey() {
	    return Constants.BYTES_OUT_1000_SEC_AVG;
	}
    }	


    public static class PersistSizeLast extends Formula {
	String getKey() {
	    return Constants.PERSIST_SIZE_LAST;
	}
    }	


}

