/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import java.rmi.Naming;

import org.cougaar.core.mts.MT;
import com.bbn.quo.rmi.QuoKernel;

class CompressWrapper extends MTCompressAdapter implements CougaarWrapper
{
    private Object lookup(String url) {
	try {
	    return Naming.lookup(url);
	} catch (java.rmi.NotBoundException notBound) {
	    System.err.println("Name not bound: " + url);
	    return null;
	} catch (java.rmi.RemoteException ex) {
	    System.err.println("Error looking up " + url + 
				   ": " + ex);
	    return null;
	} catch (java.net.MalformedURLException badUrl) {
	    System.err.println("Bad url: " + url);
	    return null;
	}
    }

    public void connect(MT server,
			MTInstrumented delegate,
			String kernelURL,
			boolean kernelIntegrated,
			boolean kernelGui) 
	throws java.rmi.RemoteException
    {
	QuoKernel kernel = null;

	if (kernelIntegrated) {
	    kernel = Utils.getKernel();
	} else {
	    Object raw = lookup(kernelURL);
	    if (raw == null) return;
	    kernel = (QuoKernel) raw;
	}
	
	System.out.println("QuoKernel: " + kernel);
	if (kernelGui) kernel.newFrame();

	linkRemoteObject(server);
	setInstrumentedServer(delegate);
	initSysconds(kernel);
	initCallbacks();
	linkContract(kernel);

    }

}

