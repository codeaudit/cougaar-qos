/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;


import org.cougaar.core.mts.MT;
import com.bbn.quo.rmi.QuoKernel;

import java.rmi.server.RMISocketFactory;

public class ServerWrapper
  extends MTInstrumentedServerAdapter
{

    ServerWrapper (int server_port,
		   RMISocketFactory client_socfac, 
		   RMISocketFactory server_socfac) 
	throws java.rmi.RemoteException
    {
	super(server_port, client_socfac, server_socfac);
    }	


    public void connect(QuoKernel kernel, MT server)
	throws java.rmi.RemoteException
    {
	initSysconds(kernel);
	initCallbacks();
	linkContract(kernel);

	// Set the instrumented delegate's "remote" object pointer
	// (which is not the same as the basic server-side delegate's
	// "remote" object).

	setServer(server);

      
    }

}
