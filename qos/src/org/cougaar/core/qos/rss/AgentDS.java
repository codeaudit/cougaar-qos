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
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.core.service.wp.AddressEntry;
import org.cougaar.core.service.wp.WhitePagesService;

import com.bbn.rss.AbstractContextInstantiater;
import com.bbn.rss.ContextInstantiater;
import com.bbn.rss.DataFormula;
import com.bbn.rss.DataValue;
import com.bbn.rss.RSS;
import com.bbn.rss.ResourceContext;
import com.bbn.ResourceStatus.ResourceNode;


public class AgentDS 
    extends CougaarDS
{

    static void register()
    {
	ContextInstantiater cinst = new AbstractContextInstantiater() {
		public ResourceContext instantiateContext(String[] parameters, 
							  ResourceContext parent)
		    throws ParameterError
		{
		    return new AgentDS(parameters, parent);
		}

		public Object identifyParameters(String[] parameters) 
		{
		    if (parameters == null || parameters.length != 1) 
			return null;
		    return  parameters[0];
		}		

		
	    };
	registerContextInstantiater("Agent", cinst);
    }

    private static final String AGENTNAME = "agentname".intern();
    static final String TOPOLOGY = "topology";
    static final String UNKNOWN_NODE = "FosterNode";

    public AgentDS(String[] parameters, ResourceContext parent) 
	throws ParameterError
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
    protected ResourceContext preferredParent(RSS root) {
	String agentname = (String) getSymbolValue(AGENTNAME);
	String nodename = null;
	ServiceBroker sb = (ServiceBroker) root.getProperty("ServiceBroker");
	AgentTopologyService ats = (AgentTopologyService)
	    sb.getService(this, AgentTopologyService.class, null);
	if (ats != null) {
	    nodename=ats.getAgentNode(MessageAddress.getMessageAddress(agentname));
	} else {
	    // AgentTopologyService not loaded.  Try a direct WP
	    // call, even though it can give an inconsistent picture.
	    WhitePagesService svc = (WhitePagesService)
		sb.getService(this, WhitePagesService.class, null);
	    try {
		AddressEntry entry = svc.get(agentname, TOPOLOGY, -1);
		if (entry == null) {
		    if (logger.isWarnEnabled())
			logger.warn("Can't find node for agent " +agentname);
		} else {
		    nodename = entry.getURI().getPath().substring(1);
		}
	    } catch (Exception ex) {
		// log?
	    }

	}

	String[] params = { nodename == null ? UNKNOWN_NODE : nodename };
	ResourceNode node = new ResourceNode();
	node.kind = "Node";
	node.parameters = params;
	ResourceNode[] path = { node };
	ResourceContext parent = root.getPathContext(path);
	setParent(parent);
	return parent;
    }


    protected void verifyParameters(String[] parameters) 
	throws ParameterError
    {
	if (parameters == null || parameters.length != 1) {
	    throw new ParameterError("AgentDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new ParameterError("AgentDS: wrong parameter type");
	} else {
	    // could canonicalize here
	    String agentname = (String) parameters[0];
	    bindSymbolValue(AGENTNAME, agentname);
	    historyPrefix = "Agent" +KEY_SEPR+ agentname;
	}
    }


    protected DataFormula instantiateFormula(String kind)
    {
	if (kind.equals("LastHeard")) {
	    return new LastHeard();
	} else if (kind.equals("LastSpoke")) {
	    return new LastSpoke();
	} else if (kind.equals("LastSpokeError")) {
	    return new LastSpokeError();
	} else if (kind.equals("HeardTime")) {
	    return new HeardTime();
	} else if (kind.equals("SpokeTime")) {
	    return new SpokeTime();
	} else if (kind.equals("SpokeErrorTime")) {
	    return new SpokeErrorTime();
	} else if (kind.equals(Constants.PERSIST_SIZE_LAST)) {
	    return new PersistSizeLast();
	} else if (kind.equals(Constants.CPU_LOAD_AVG) ||
		   kind.equals(Constants.CPU_LOAD_MJIPS) ||
		   kind.equals(Constants.MSG_IN) ||
		   kind.equals(Constants.BYTES_IN) ||
		   kind.equals(Constants.MSG_OUT) ||
		   kind.equals(Constants.BYTES_OUT)) {
	    return new DecayingHistoryFormula(historyPrefix, kind);
	} else {
	    return null;
	}
    }






    abstract static class Formula 
	extends DataFormula
    {

	abstract String getKey();
	
	protected DataValue defaultValue() {
	    return new DataValue(0);
	}
	

	protected void initialize(ResourceContext context) {
	    super.initialize(context);
	    String agentName = (String) context.getValue(AGENTNAME);
	    String key = "Agent" +KEY_SEPR+ agentName +KEY_SEPR+ getKey();

	    String[] parameters = { key };
	    ResourceNode node = new ResourceNode();
	    node.kind = "Integrater";
	    node.parameters = parameters;
	    ResourceNode formula = new ResourceNode();
	    formula.kind = "Formula";
	    formula.parameters = new String[0];
	    ResourceNode[] path = { node, formula };
	    DataFormula dependency = RSS.instance().getPathFormula(path);
	    registerDependency(dependency, "Formula");
	}

	protected DataValue doCalculation(DataFormula.Values values) {
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
	

	protected void initialize(ResourceContext context) {
	    super.initialize(context);

	    DataFormula baseFormula = context.getFormula(getKey(), null);
	    registerDependency(baseFormula, "Formula");

	    ResourceNode node = new ResourceNode();
	    node.kind = "Alarm";
	    node.parameters = new String[0];
	    ResourceNode formula = new ResourceNode();
	    formula.kind = "OneSecond";
	    formula.parameters = new String[0];
	    ResourceNode[] path = { node, formula };
	    DataFormula alarm = RSS.instance().getPathFormula(path);
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


	    if (alarmTime > sendTime) {
		elapsed = (long) Math.floor((alarmTime-sendTime)/1000.0);
	    }
	    return new DataValue(elapsed, vals.minCredibility(),
				 "seconds", "unknown");
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



    public static class PersistSizeLast extends Formula {
	String getKey() {
	    return Constants.PERSIST_SIZE_LAST;
	}
    }	




}

