/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;


import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.impl.KernelImpl;

public class Utils 
{
    private static final String KERNEL_PROPERTIES = 
	"org.cougaar.lib.quo.kernel.properties";

    private static void readKernelProperties(Properties props) {
	String kconf = System.getProperty(KERNEL_PROPERTIES);
	if (kconf != null) {
	    InputStream is = null;
	    try {
		try {
		    URL url = new URL(kconf);
		    is = url.openStream();
		} catch (java.net.MalformedURLException mal) {
		    // try it as a filename
		    is = new FileInputStream(kconf);
		}
		
		props.load(is);
		is.close();
	    } catch (java.io.IOException e) {
	    }
	}

    }

    public synchronized static QuoKernel getKernel() {
	Properties kprops = new Properties();
	readKernelProperties(kprops);
	kprops.put("quoKernel.EvaluatorThread", "false");
	kprops.put("quoKernel.Export", "false");
	String nodeName = System.getProperty("org.cougaar.node.name");
  	kprops.put("quoKernel.Title",  "QuOKernel-" +  nodeName);

	QuoKernel kernel = KernelImpl.getKernelReference(kprops);


	if (Boolean.getBoolean("org.cougaar.lib.quo.kernel.gui")) {
	    try {
		kernel.newFrame();
// 		if (Debug.debug(QUO)) {
// 		    kernel.setDebug(com.bbn.quo.corba.QuoKernel.DEBUG_ALL);
// 		}
	    } catch (java.rmi.RemoteException ex) {
	    }
	}

	return kernel;
    }



}
