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

import org.cougaar.core.qos.quo.Utils;

import org.cougaar.core.mts.DestinationLink;
import org.cougaar.core.mts.SSLRMILinkProtocol;
import org.cougaar.core.mts.StandardAspect;
import org.cougaar.core.society.Message;
import org.cougaar.core.society.TrustStatusService;
import org.cougaar.core.society.TrustStatusServiceImpl;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.monitor.ResourceMonitorService;
import org.cougaar.core.qos.monitor.QosMonitorService;

import java.util.Observer;
import java.util.Observable;


public class RemoteSSLAspect extends StandardAspect
{
    private static String CONTRACT_IFACE = "org::cougaar::lib::quo::RemoteSSL";


    private ResourceMonitorService rms;
    private QosMonitorService qms;
    private TrustStatusService tss;
    private QuoKernel kernel;
    private TrustObserver trustObserver;

    public Object getDelegate(Object delegate, Class type) {
	if (type == DestinationLink.class) {
	    DestinationLink link = (DestinationLink) delegate;
	    if (link.getProtocolClass() == SSLRMILinkProtocol.class)
		return new SSLDestinationLink(link);
	    else 
		return null;
	} else {
	    return null;
	}
    }


    private void ensureServices() {
	if (kernel == null) kernel = Utils.getKernel();

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
		trustObserver = new TrustObserver();
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
    }


    private class TrustObserver implements Observer {
	private ValueSC syscond;

	TrustObserver() {
	    try {
		SysCond sc = kernel.bindSysCond("TrustObserver",
						"com.bbn.quo.rmi.ValueSC",
						"com.bbn.quo.ValueSCImpl");
		syscond = (ValueSC) sc;
		syscond.longValue(10);
	    } catch (java.rmi.RemoteException remote_ex) {
		remote_ex.printStackTrace();
	    }

	    if (tss != null) tss.registerSocietyTrustObserver(this);
	}

	ValueSC getSysCond() {
	    return syscond;
	}

	public void update(Observable obs, Object value) {
	    if (syscond != null) {
		if (obs instanceof TrustStatusServiceImpl) {
		    int trust = 
			((TrustStatusServiceImpl) obs).getSocietyTrust();
		    try {
			syscond.longValue(trust);
		    } catch (java.rmi.RemoteException remote_ex) {
			remote_ex.printStackTrace();
		    }
		}
	    }
	}
    }

    private class SSLDestinationLink extends DestinationLinkRemoteSSLAdapter {

	SSLDestinationLink(DestinationLink link) {

	    ensureServices();
	    setDestinationLink(link);
	    setServices(rms, trustObserver.getSysCond());

	    try {
		initSysconds(kernel);
		initCallbacks();
		String name = "SSL_" + link.getDestination(); 
		Contract contract = initContract(name, CONTRACT_IFACE, kernel);
		set_contract_RemoteSSL(contract);
	    } catch (java.rmi.RemoteException ex) {
		ex.printStackTrace();
	    }
	    linkRemoteObject(link);
	}

    }

}
