/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import org.cougaar.core.mts.MT;
import com.bbn.quo.rmi.QuoKernel;


public class ServerWrapper
  extends MTInstrumentedServerAdapter
{

    ServerWrapper () 
	throws java.rmi.RemoteException
    {
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
