/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;


import org.cougaar.lib.quo.*;

import java.rmi.Naming;

import org.cougaar.core.service.LoggingService;
import org.cougaar.core.mts.MT;

import com.bbn.quo.NetUtilities;
import com.bbn.quo.ParsedReference;
import com.bbn.quo.data.Utilities;
import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.Contract;
import com.bbn.quo.rmi.impl.RmiUtilities;


class CompressWrapper extends MTCompressAdapter implements CougaarWrapper
{
    public void connect(MT server, 
			MTInstrumented delegate,
			LoggingService loggingService) 
	throws java.rmi.RemoteException
    {
	QuoKernel kernel = Utils.getKernel();
	String clientHost = null;
	String serverHost = null; 
	ParsedReference remoteRef =  RmiUtilities.parseReference(server);

	linkRemoteObject(server);
	setInstrumentedServer(delegate);
	setLoggingService(loggingService);
	initSysconds(kernel);
	initCallbacks();

	try {
	    clientHost = 
		Utilities.canonicalizeAddress(NetUtilities.getHostAddress());
	    serverHost = Utilities.canonicalizeAddress(remoteRef.host);
	} catch (java.net.UnknownHostException unknown_host) {
	    loggingService.error(null, unknown_host);
	} catch (Throwable t) {
	    loggingService.error(null, t);
	}


	try {
	    String contractName = "Compress" +clientHost + 
		" ->" + serverHost;
	    String iface = "org.cougaar.lib.quo.Compress";
	    Contract contract = initContract(contractName, iface, kernel);
	    set_contract_Compress(contract);

	} catch (java.rmi.RemoteException ex) {
	    loggingService.error(null, ex);
	}

    }

}

