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
import com.bbn.quo.rmi.Contract;
import com.bbn.quo.rmi.ValueSC;

import org.cougaar.core.mts.DestinationLink;
import org.cougaar.core.mts.SSLRMILinkProtocol;


public class RemoteSSLAspect extends QuoAspect
{
    private static String CONTRACT_IFACE = 
	"org::cougaar::lib::quo::RemoteSSL";

    private static ValueSC USE_SSL;

    private synchronized ValueSC Get_USE_SSL () 
	throws java.rmi.RemoteException

    {
	if (USE_SSL == null) {
	    USE_SSL = getBooleanValueSC("UseSSL",
					"org.cougaar.lib.quo.UseSSL");
	}
	return USE_SSL;
    }


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



    private class SSLDestinationLink extends DestinationLinkRemoteSSLAdapter {

	SSLDestinationLink(DestinationLink link) {

	    quoInit();
	    setDestinationLink(link);

	    try {
		setServices(rms, Get_TRUST(), Get_USE_SSL());
		initSysconds(kernel);
		initCallbacks();
		String name = "SSL_" + link.getDestination(); 
		Contract contract = 
		    initContract(name, CONTRACT_IFACE, kernel);
		set_contract_RemoteSSL(contract);
	    } catch (java.rmi.RemoteException ex) {
		loggingService.error(null, ex);
	    }
	    linkRemoteObject(link);
	}

    }

}
