/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ULTRALOG (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.lib.quo;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.ValueSC;
import com.bbn.quo.rmi.SysCond;
import com.bbn.quo.rmi.ExpectedBandwidthSC;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MulticastMessageAddress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


public class TrafficMaskAspect extends QuoAspect
{
    private static final String CONTRACT_IFACE = 
	"org::cougaar::lib::quo::TrafficMask";
    private static final int NODE_PERIOD = 30000;
    private static final int TRAFFIC_PERIOD = 15000;
    private static final int TRAFFIC_SIZE = 3000;

    private HashMap qoskets;
    private Timer timer = new Timer(true);
    private ValueSC USE_MASKING;
    private TimerTask nodeUpdater;


    public TrafficMaskAspect() {
	super();
    }

    private class NodeUpdater extends TimerTask {
	public void run() {
	    MulticastMessageAddress addr = 
		(MulticastMessageAddress) MessageAddress.SOCIETY;
	    Iterator itr = getRegistry().findRemoteMulticastTransports(addr);
	    while (itr.hasNext()) {
		MessageAddress node_ref = (MessageAddress) itr.next();
		if (!qoskets.containsKey(node_ref)) qosketInit(node_ref);
	    }
	}
    }


    private class TrafficMaskQosketImpl extends TrafficMaskQosketSkel {
	private MessageAddress destination;

	TrafficMaskQosketImpl(MessageAddress destination) {
	    this.destination = destination;
	}

	private class TrafficMaskControlImpl implements TrafficMaskControl {
	    // What the hell is this doing in Callback?
	    public void sendMessage(String junk) {
	    }

	    public void turnOn() {
		tmgs.setRequestParameters(destination, 
					  TRAFFIC_PERIOD, 
					  TRAFFIC_SIZE);
	    }


	    public void turnOff() {
		tmgs.setRequestParameters(destination, -1, 0);
	    }
	
	}

	public void initSysconds(QuoKernel kernel) 
	    throws java.rmi.RemoteException
	{
	    useMask = Get_USE_MASKING();
	    trust = Get_TRUST();
	    Bandwidth = 
		(ExpectedBandwidthSC) 
		rms.getExpectedBandwidthForAgentSyscond(destination);

	}

	public void initCallbacks() {
	    trafficControl = new TrafficMaskControlImpl();
	}

	void initContract(String name) 
	    throws java.rmi.RemoteException
	{
	    quo_TrafficMask = initContract(name, CONTRACT_IFACE, kernel);
	}
    }


    private synchronized ValueSC Get_USE_MASKING () 
	throws java.rmi.RemoteException

    {
	if (USE_MASKING == null) {
	    USE_MASKING = getBooleanValueSC("UseMasking",
					    "org.cougaar.lib.quo.UseMasking");
	}
	return USE_MASKING;
    }
 


    // No delegates, this aspect used for side-effect only
    public Object getDelegate(Object delegate, Class type) {
	quoInit();
	return null;
    }

    protected synchronized boolean quoInit() {
	if (nodeUpdater != null) return true;

	boolean result = super.quoInit();
	if (!result) return false;

	qoskets = new HashMap();

	nodeUpdater = new NodeUpdater();
	timer.schedule(nodeUpdater, 0, NODE_PERIOD);

	return true;

    }


    private void qosketInit(MessageAddress node_ref) {
	TrafficMaskQosketImpl qk = new TrafficMaskQosketImpl(node_ref);
	try {
	    qk.initSysconds(kernel);
	    qk.initCallbacks();
	    String name = "Traffic_" + node_ref;
	    qk.initContract(name);
	    qoskets.put(node_ref, qk);
	} catch (java.rmi.RemoteException ex) {
	    debugService.error(null, ex);
	}
    }

}
