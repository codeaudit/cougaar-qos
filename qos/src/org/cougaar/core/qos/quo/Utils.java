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


    public synchronized static QuoKernel getKernel() {
	java.util.Properties props = new java.util.Properties();
	String kconf = System.getProperty(RSSLink.RSS_PROPFILE);
	if (kconf != null) {
	    try {
		java.io.InputStream is = 
		    new java.io.FileInputStream(kconf);
		props.load(is);
		is.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	props.put("EvaluatorThread", "false");
	QuoKernel kernel = KernelImpl.getKernelReference(props);


	if (Boolean.getBoolean("org.cougaar.lib.quo.kernel.gui")) {
	    try {
		kernel.newFrame();
	    } catch (java.rmi.RemoteException ex) {
		ex.printStackTrace();
	    }
	}

	return kernel;
    }


}
