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
import org.cougaar.core.service.wp.AddressEntry;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.core.service.wp.WhitePagesService;

import com.bbn.rss.AbstractContextInstantiater;
import com.bbn.rss.ContextInstantiater;
import com.bbn.rss.DataFormula;
import com.bbn.rss.DataValue;
import com.bbn.rss.RSS;
import com.bbn.rss.ResourceContext;
import com.bbn.ResourceStatus.ResourceNode;


/**
 * This RSS ResourceContext represents a COUGGAR Node.  Its parent in
 * the RSS hierarchy tree is the context for the Node's host.  It's
 * identified by the Node's name and has no locally defined formulas.
 */
public class NodeDS 
    extends CougaarDS
{
    static void register()
    {
	ContextInstantiater cinst = new AbstractContextInstantiater() {
		public ResourceContext instantiateContext(String[] parameters, 
							  ResourceContext parent)
		    throws ParameterError
		{
		    return new NodeDS(parameters, parent);
		}

		public Object identifyParameters(String[] parameters) 
		{
		    if (parameters == null || parameters.length != 1) 
			return null;
		    return  parameters[0];
		}		

		
	    };
	registerContextInstantiater("Node", cinst);
    }

    static final String NODENAME = "nodename".intern();
    static final String TOPOLOGY = "topology";
    static final String UNKNOWN_HOST_IP = "169.0.0.1";//DHCP No Address from server

    private DataFormula vm_size;

    static boolean isUnknownHost(String addr)
    {
	return addr.equals(UNKNOWN_HOST_IP);
    }


    public NodeDS(String[] parameters, ResourceContext parent) 
	throws ParameterError
    {
	super(parameters, parent);
    }


    protected boolean useParentPath() {
	return false;
    }

    String getNodeName() {
	return (String) getSymbolValue(NODENAME);
    }

    // Node DataScopes can be the first element in a path.  They must
    // find or make the corresponding HostDS and return that as the
    // preferred parent.
    protected ResourceContext preferredParent(RSS root) 
    {

	ServiceBroker sb = (ServiceBroker) root.getProperty("ServiceBroker");
	AgentTopologyService ats = (AgentTopologyService)
	    sb.getService(this, AgentTopologyService.class, null);
	String nodename = (String) getSymbolValue(NODENAME);
	String hostname = null;
	if (ats != null) {
	    hostname=ats.getNodeHost(MessageAddress.getMessageAddress(nodename));
	} else {
	    // AgentTopologyService not loaded.  Try a direct WP
	    // call, even though it can give an inconsistent picture.
	    WhitePagesService svc = (WhitePagesService)
		sb.getService(this, WhitePagesService.class, null);
	    try {
		AddressEntry entry = svc.get(nodename, TOPOLOGY, -1);
		if (entry == null) {
		    if (logger.isWarnEnabled())
			logger.warn("Can't find host for node " +nodename);
		} else {
		    hostname = entry.getURI().getHost();
		}
	    } catch (Exception ex) {
		// log this?
	    }
	}

	String[] params = { hostname == null ? UNKNOWN_HOST_IP : hostname };
	ResourceNode node = new ResourceNode();
	node.kind = "Host";
	node.parameters = params;
	ResourceNode[] path = { node } ;
	ResourceContext parent = root.getPathContext(path);
	setParent(parent);
	return parent;
    }


    protected void verifyParameters(String[] parameters) 
	throws ParameterError
    {
	if (parameters == null || parameters.length != 1) {
	    throw new ParameterError("NodeDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new ParameterError("NodeDS: wrong parameter type");
	} else {
	    // could canonicalize here
	    String nodename = (String) parameters[0];
	    bindSymbolValue(NODENAME, nodename);
	    historyPrefix = "Node" +KEY_SEPR+ nodename;
	}
    }


    protected DataFormula instantiateFormula(String kind)
    {
	if (kind.equals(Constants.CPU_LOAD_AVG) ||
	    kind.equals(Constants.CPU_LOAD_MJIPS) ||
	    kind.equals(Constants.MSG_IN) ||
	    kind.equals(Constants.BYTES_IN) ||
	    kind.equals(Constants.MSG_OUT) ||
	    kind.equals(Constants.BYTES_OUT)) {
	    return new DecayingHistoryFormula(historyPrefix, kind);
	} else if (kind.equals("VMSize")) {
	    // singleton
	    return findOrMakeVMSizeFormula();
	} else {
	    // No local formulas
	    return null;
	}
    }


    private synchronized DataFormula  findOrMakeVMSizeFormula()
    {
	if (vm_size == null) vm_size = new VMSize();
	return vm_size;
    }

    private class VMSize extends DataFormula {
	protected DataValue defaultValue() {
	    return DataValue.NO_VALUE;
	}
	

	protected void initialize(ResourceContext context) {
	    super.initialize(context);

	    ResourceNode node = new ResourceNode();
	    node.kind = "Alarm";
	    node.parameters = new String[0];
	    ResourceNode formula = new ResourceNode();
	    formula.kind = "FifteenSeconds";
	    formula.parameters = new String[0];
	    ResourceNode[] path = { node, formula };
	    DataFormula alarm = RSS.instance().getPathFormula(path);
	    registerDependency(alarm, "Alarm");
	}

	protected DataValue doCalculation(DataFormula.Values vals)
	{
	    long size = Runtime.getRuntime().totalMemory();
	    return new DataValue(size, Constants.SECOND_MEAS_CREDIBILITY,
				 "bytes", 
				 "Runtime.getRuntime().totalMemory()");
	}

	
    }
}
