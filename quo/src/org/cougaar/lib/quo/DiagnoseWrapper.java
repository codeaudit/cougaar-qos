/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import java.rmi.Naming;

import org.cougaar.core.qos.quo.Utils;
import org.cougaar.core.service.LoggingService;

import org.cougaar.core.mts.MT;
import com.bbn.quo.rmi.QuoKernel;

class DiagnoseWrapper extends MTDiagnoseAdapter implements CougaarWrapper
{
    public void connect(MT server,
			MTInstrumented delegate,
			LoggingService loggingService) 
	throws java.rmi.RemoteException
    {
	QuoKernel kernel = Utils.getKernel();

	initSysconds(kernel);
	initCallbacks();
	linkContract(kernel);
	linkRemoteObject(server);
	setInstrumentedServer(delegate);
	setLoggingService(loggingService);

    }

}

