/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.core.qos.quo;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.impl.KernelImpl;

import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class Utils 
{


    public synchronized static QuoKernel getKernel() {
	return getKernel(new Properties());
    }

    public synchronized static QuoKernel getKernel(Properties props) {
	String kconf = System.getProperty(RSSLink.RSS_PROPFILE);
	if (kconf != null) {
	    try {
		InputStream is = new FileInputStream(kconf);
		props.load(is);
		is.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	props.put("quoKernel.EvaluatorThread", "false");
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
