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
import com.bbn.quo.rmi.Contract;
import com.bbn.quo.rmi.SysCond;
import com.bbn.quo.rmi.ExpectedCapacitySC;

import org.cougaar.core.society.MessageAddress;
import org.cougaar.core.society.MulticastMessageAddress;
import org.cougaar.core.society.TrustStatusService;
import org.cougaar.core.society.TrustStatusServiceImpl;
import org.cougaar.core.mts.MessageTransportRegistry;
import org.cougaar.core.mts.StandardAspect;
import org.cougaar.core.mts.TrafficMaskingGeneratorService;
import org.cougaar.core.qos.quo.Utils;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.monitor.ResourceMonitorService;
import org.cougaar.core.qos.monitor.QosMonitorService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Observer;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;


public class TrafficMaskAspect extends StandardAspect
{
    private static String CONTRACT_IFACE = 
	"org::cougaar::lib::quo::TrafficMask";

    private QuoKernel kernel;
    private HashMap qoskets;
    private ResourceMonitorService rms;
    private QosMonitorService qms;
    private TrustStatusService tss;
    private TrafficMaskingGeneratorService tmgs;
    private String local_host;
    private Timer timer = new Timer(true);
    private boolean inited = false;
    private ValueSC USE_MASKING;
    private ValueSC TRUST;
    private MessageTransportRegistry registry;


    public TrafficMaskAspect() {
	super();
	try {
	    local_host = java.net.InetAddress.getLocalHost().getHostAddress();
	} catch (java.net.UnknownHostException ex) {
	    local_host = "127.0.0.1";
	}
    }


    private class ExpectedCapacityUpdater extends TimerTask {
	private MessageAddress agent;
	private String host;
	private ExpectedCapacitySC syscond;

	ExpectedCapacityUpdater(MessageAddress agent,
				ExpectedCapacitySC syscond)
	{
	    this.agent = agent;
	    this.syscond = syscond;
	}

	public void run() {
	    String new_host = rms.getHostForAgent(agent);
	    if (new_host == null) return;
	    if (host == null || !host.equals(new_host)) {
		host = new_host;
		System.out.println("===== New host " + host +
				   " for agent " + agent);
		try {
		    syscond.setHosts(local_host, host);
		} catch (java.rmi.RemoteException ex) {
		    ex.printStackTrace();
		}
	    }
	}
    }


    private class NodeUpdater extends TimerTask {
	public void run() {
	    MulticastMessageAddress addr = 
		(MulticastMessageAddress) MessageAddress.SOCIETY;
	    Iterator itr = registry.findRemoteMulticastTransports(addr);
	    while (itr.hasNext()) {
		MessageAddress node_ref = (MessageAddress) itr.next();
		if (!qoskets.containsKey(node_ref)) qosketInit(node_ref);
	    }
	}
    }

    private class TrustObserver implements Observer {
	TrustObserver() {
	    if (tss != null) tss.registerSocietyTrustObserver(this);
	}

	public void update(Observable obs, Object value) {
	    if (TRUST != null) {
		if (obs instanceof TrustStatusServiceImpl) {
		    int trust = 
			((TrustStatusServiceImpl) obs).getSocietyTrust();
		    try {
			TRUST.longValue(trust);
		    } catch (java.rmi.RemoteException remote_ex) {
			remote_ex.printStackTrace();
		    }
		}
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
		tmgs.setRequestParameters(destination, 1000, 1000);
		System.err.println(destination + " masking on");
	    }


	    public void turnOff() {
		tmgs.setRequestParameters(destination, -1, 0);
		System.err.println(destination + " masking off");
	    }
	
	}

	public void initSysconds(QuoKernel kernel) 
	    throws java.rmi.RemoteException
	{
	    useMask = Get_USE_MASKING(kernel);
	    trust = Get_TRUST(kernel);
	    // Bandwidth = ...

	    SysCond syscond = 
		kernel.bindSysCond("Bandwidth from " + local_host +
				   " to " + destination,
				   "com.bbn.quo.rmi.ExpectedCapacitySC",
				   "com.bbn.quo.data.ExpectedCapacitySCImpl");
	    System.out.println("Created Bandwidth syscond");

	    Bandwidth = (ExpectedCapacitySC) syscond;
	    //force bandwidth to be low, incase host is not known yet
	    try {
		Bandwidth.setLong(1);
	    } catch (java.rmi.RemoteException ex) {
		ex.printStackTrace();	
	    }

	    TimerTask task1 = 
		new ExpectedCapacityUpdater(destination, Bandwidth);
	    timer.schedule(task1, 0, 5000);
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


    private synchronized ValueSC Get_USE_MASKING (QuoKernel kernel) 
	throws java.rmi.RemoteException

    {
	if (USE_MASKING == null) {
	    SysCond syscond =  kernel.bindSysCond("UseMasking",
						  "com.bbn.quo.rmi.ValueSC",
						  "com.bbn.quo.ValueSCImpl");
	    USE_MASKING = (ValueSC) syscond;

	    boolean useMasking = 
		Boolean.getBoolean("org.cougaar.lib.quo.UseMasking");
	    USE_MASKING.booleanValue(useMasking);
	}
	return USE_MASKING;
    }
 
    private synchronized ValueSC Get_TRUST (QuoKernel kernel) 
	throws java.rmi.RemoteException

    {
	if (TRUST == null) {
	    try {
		SysCond sc = kernel.bindSysCond("TrustObserver",
						"com.bbn.quo.rmi.ValueSC",
						"com.bbn.quo.ValueSCImpl");
		TRUST = (ValueSC) sc;
		TRUST.longValue(10);

		new TrustObserver();
	    } catch (java.rmi.RemoteException remote_ex) {
		remote_ex.printStackTrace();
	    }

	}
	return TRUST;
    }

    private void ensureServices() {
	ServiceBroker sb = getServiceBroker();

	if (tss == null) {
	    System.out.println("%%% Looking for TSS");
	    Object svc = 
		sb.getService(this, TrustStatusService.class, null);
	    if (svc == null) {
		System.err.println("### Can't find TrustStatusService");
	    } else {
		tss = (TrustStatusService) svc;
		System.out.println("%%% Got TrustStatusService!");
	    }
	}


	if (tmgs == null) {
	    System.out.println("%%% Looking for TMGS from " + sb);
	    Object svc = 
		sb.getService(this, TrafficMaskingGeneratorService.class, null);
	    if (svc == null) {
		System.err.println("### Can't find TrafficMaskingGeneratorService");
	    } else {
		tmgs = (TrafficMaskingGeneratorService) svc;
		System.out.println("%%% Got TrafficMaskingGeneratorService!");
	    }
	}

	if (rms == null) {
	    System.out.println("%%% Looking for RMS");
	    Object svc = 
		sb.getService(this, ResourceMonitorService.class, null);
	    if (svc == null) {
		System.err.println("### Can't find ResourceMonitorService");
	    } else {
		rms = (ResourceMonitorService) svc;
		System.out.println("%%% Got ResourceMonitorService!");
	    }
	}

	if (qms == null) {

	    Object svc = sb.getService(this, QosMonitorService.class, null);
	    if (svc == null) {
		System.err.println("### Can't find QosMonitorService");
	    } else {
		qms = (QosMonitorService) svc;
		System.out.println("%%% Got QosMonitorService!");
	    }
	}

	if (rms != null && qms != null && tss != null && tmgs != null) {
	    inited = true;
	}
    }

    // No delegates, this aspect used for side-effect only
    public Object getDelegate(Object delegate, Class type) {
	quoInit();
	return null;
    }

    private synchronized void quoInit() {
	if (inited) return;  // already inited

	ensureServices();
	if (!inited) return; // not ready yet

	registry = MessageTransportRegistry.getRegistry();
	kernel = Utils.getKernel();
	qoskets = new HashMap();

	TimerTask updater = new NodeUpdater();
	timer.schedule(updater, 0, 5000);

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
	    ex.printStackTrace();
	}
    }

}
