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
import java.net.URL;
import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.Debug;
import org.cougaar.core.mts.DebugFlags;
import org.cougaar.core.mts.MessageTransportRegistryService;

public class Utils implements DebugFlags
{


    private static final String RSS_PROPERTIES = "org.cougaar.rss.properties";

    public static void readRSSProperties(Properties props) {
	String kconf = System.getProperty(RSS_PROPERTIES);
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
		e.printStackTrace();
	    }
	}

    }

    public synchronized static QuoKernel getKernel() {
	Properties kprops = new Properties();
	readRSSProperties(kprops);
	return getKernel(kprops);
    }

    public synchronized static QuoKernel getKernel(Properties kprops) 
    {
	kprops.put("quoKernel.EvaluatorThread", "false");
	String nodeName = System.getProperty("org.cougaar.node.name");
  	kprops.put("quoKernel.Title",  "QuO Kernel: " +  nodeName);

	QuoKernel kernel = KernelImpl.getKernelReference(kprops);


	if (Boolean.getBoolean("org.cougaar.lib.quo.kernel.gui")) {
	    try {
		kernel.newFrame();
		if (Debug.debug(QUO)) {
		    kernel.setDebug(com.bbn.quo.corba.QuoKernel.DEBUG_ALL);
		}
	    } catch (java.rmi.RemoteException ex) {
		ex.printStackTrace();
	    }
	}

	return kernel;
    }


}
