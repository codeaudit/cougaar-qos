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

import org.cougaar.core.mts.Debug;
import org.cougaar.core.mts.DebugFlags;
import org.cougaar.core.mts.MessageTransportRegistry;

public class Utils implements DebugFlags
{


    public synchronized static QuoKernel getKernel() {
	Properties kprops = new Properties();
	MessageTransportRegistry registry = 
	    MessageTransportRegistry.getRegistry();
	String name = "QuO Kernel: " + registry.getLocalAddress().toString();
	kprops.put("quoKernel.Title", name);
	return getKernel(kprops);
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
