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
package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;


import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.ValueSC;
import com.bbn.quo.rmi.SysCond;

import org.cougaar.planning.plugin.node.TrustStatusService;
import org.cougaar.planning.plugin.node.TrustStatusServiceImpl;
import org.cougaar.core.mts.StandardAspect;
import org.cougaar.core.mts.TrafficMaskingGeneratorService;
import org.cougaar.core.component.ServiceBroker;

import java.util.Observer;
import java.util.Observable;


abstract public class QuoAspect extends StandardAspect
{
    protected QuoKernel kernel;
    protected TrustStatusService tss;
    protected TrafficMaskingGeneratorService tmgs;

    private boolean inited = false;



    public QuoAspect() {
	super();
    }




    protected synchronized ValueSC getBooleanValueSC (String name,
						      String initialValueProperty) 
	throws java.rmi.RemoteException

    {
	SysCond syscond =  kernel.bindSysCond(name,
					      "com.bbn.quo.rmi.ValueSC",
					      "com.bbn.quo.ValueSCImpl");
	ValueSC vsc = (ValueSC) syscond;

	boolean initialValue = 	Boolean.getBoolean(initialValueProperty);
	vsc.booleanValue(initialValue);

	return vsc;
    }



    // The Trust code may move elsewhere later
    private static ValueSC TRUST;

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
			loggingService.error(null, remote_ex);
		    }
		}
	    }
	}
    }



    protected synchronized ValueSC Get_TRUST () 
	throws java.rmi.RemoteException

    {
	if (TRUST == null) {
	    try {
              SysCond sc = kernel.bindSysCond("TrustObserver",
                                              "com.bbn.quo.rmi.ValueSC",
                                              "com.bbn.quo.ValueSCImpl");
              TRUST = (ValueSC) sc;
              //TRUST.longValue(10);
              TRUST.longValue(tss.getSocietyTrust());

		new TrustObserver();
	    } catch (java.rmi.RemoteException remote_ex) {
		loggingService.error(null, remote_ex);
	    }

	}
	return TRUST;
    }






    protected void ensureServices() {
	ServiceBroker sb = getServiceBroker();

	if (tss == null) {
	    Object svc = 
		sb.getService(this, TrustStatusService.class, null);
	    if (svc != null) {
		tss = (TrustStatusService) svc;
	    }
	}


	if (tmgs == null) {
	    Object svc = 
		sb.getService(this, TrafficMaskingGeneratorService.class, null);
	    if (svc != null) {
		tmgs = (TrafficMaskingGeneratorService) svc;
	    }
	}

	if (tss != null && tmgs != null) {
	    inited = true;
	}
    }


    protected synchronized boolean quoInit() {
	if (inited) return true;  // already inited

	ensureServices();
	if (!inited) return false; // not ready yet

	kernel = Utils.getKernel();

	return true;

    }


}
