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

import org.cougaar.core.qos.quo.Utils;

import org.cougaar.core.mts.DestinationLink;
import org.cougaar.core.mts.SSLRMILinkProtocol;
import org.cougaar.core.mts.StandardAspect;
import org.cougaar.core.society.Message;
import org.cougaar.core.society.TrustStatusService;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.monitor.ResourceMonitorService;
import org.cougaar.core.qos.monitor.QosMonitorService;


public class RemoteSSLAspect extends StandardAspect
{
    private ResourceMonitorService rms;
    private QosMonitorService qms;
    private TrustStatusService tss;

    public Object getDelegate(Object delegate, Class type) 
    {
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
	ServiceBroker sb = getServiceBroker();

	if (rms == null) {
	    Object svc = 
		sb.getService(this, ResourceMonitorService.class, null);
	    if (svc == null) {
		System.err.println("### Can't find ResourceMonitorService");
	    } else {
		rms = (ResourceMonitorService) svc;
		System.out.println("%%% Got ResourceMonitorService!");
	    }
	}

	if (tss == null) {
	    Object svc = 
		sb.getService(this, TrustStatusService.class, null);
	    if (svc == null) {
		System.err.println("### Can't find TrustStatusService");
	    } else {
		tss = (TrustStatusService) svc;
		System.out.println("%%% Got TrustStatusService!");
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


    private class SSLDestinationLink extends DestinationLinkRemoteSSLAdapter {

	SSLDestinationLink(DestinationLink link) {
	    QuoKernel kernel = Utils.getKernel();
	    if (Boolean.getBoolean("org.cougaar.lib.quo.kernel.gui")) {
		try {
		    kernel.newFrame();
		} catch (java.rmi.RemoteException ex) {
		    ex.printStackTrace();
		}
	    }

	    ensureServices();
	    setDestinationLink(link);
	    setServices(rms,qms);

	    try {
		initSysconds(kernel);
		initCallbacks();
		linkContract(kernel);
	    } catch (java.rmi.RemoteException ex) {
		ex.printStackTrace();
	    }
	    linkRemoteObject(link);
	}

    }

}
