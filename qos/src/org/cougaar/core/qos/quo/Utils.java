/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.core.qos.quo;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.impl.KernelImpl;

public class Utils 
{


    public static QuoKernel getKernel() {
	java.util.Properties props = new java.util.Properties();
	String kconf = System.getProperty("org.cougaar.lib.quo.kernel.config");
	if (kconf != null) {
	    try {
		java.io.InputStream is = 
		    new java.io.FileInputStream(kconf);
		props.load(is);
		is.close();
	    } catch (Exception e) {
		System.err.println("Error loading kernel properties from " + 
				   kconf + ": " + e);
	    }
	}
	props.put("EvaluatorThread", "false");
	QuoKernel kernel = KernelImpl.getKernelReference(props);
//  	try { 
//  	    kernel.newFrame(); 
//  	    kernel.setDebug(QuoKernel.DEBUG_ALL);
//  	}
//  	catch (java.rmi.RemoteException ex) {}
	return kernel;
    }


}
