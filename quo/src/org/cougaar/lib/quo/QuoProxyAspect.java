/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ULTRALOG (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.quo;


import org.cougaar.core.qos.quo.Utils;
import org.cougaar.core.mts.MT;
import org.cougaar.core.mts.MTImpl;
import org.cougaar.core.mts.SocketFactory;
import org.cougaar.core.mts.StandardAspect;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;

import java.rmi.server.RMISocketFactory;

public class QuoProxyAspect extends StandardAspect
{

    /**
     * Bundle up an MT and an MTInstrumented into a QuoProxy instance,
     * and use that instance as an aspect delegate at the RemoteImpl
     * cutpoint.  */
    private static class QuoProxy 
	implements java.io.Serializable, MT
    {
	public MT mt;
	public MTInstrumented mti;
	public QuoProxy(MT mt, MTInstrumented mti) 
	{
	    this.mt = mt; 
	    this.mti = mti;
	}
	public String toString() {
	    return "<QuoProxy "+mt+", "+mti+">";
	}

	// MT compatibility.  Not used but required.
	public void rerouteMessage(Message m) 
	    throws java.rmi.RemoteException
	{
	    mt.rerouteMessage(m);
	}

	public MessageAddress getMessageAddress() 
	    throws java.rmi.RemoteException
	{
	    return mt.getMessageAddress();
	}
    }



    private CougaarWrapper makeClientAdapter() {
	String wrapper_classname = 
	    System.getProperty("org.cougaar.lib.quo.wrapper");
	if (wrapper_classname ==  null || wrapper_classname.equals(""))
	    return null;

	Class wrapper_class = null;

	Object raw_instance = null;
	CougaarWrapper wrapper = null;

	try {
	    wrapper_class = Class.forName(wrapper_classname);
	}
	catch (ClassNotFoundException class_not_found) {
	    System.err.println(class_not_found);
	    return null;
	}

	try {
	    raw_instance = wrapper_class.newInstance();
	}
	catch (InstantiationException instantiation) {
	    System.err.println(instantiation);
	    return null;
	}
	catch (IllegalAccessException illegal_access) {
	    System.err.println(illegal_access);
	    return null;
	}

	if (raw_instance instanceof CougaarWrapper) {
	    wrapper = (CougaarWrapper) raw_instance;
	} else {
	    System.err.println(raw_instance + " is not an CougaarWrapper");
	    return null;
	}

	return wrapper;
    }

    /**
     * RemoteProxy cutpoint: if the remote object is a QuoProxy, make
     * a CougaarWrapper from its components; if it's an ordinary
     * MT, return it unchanged.  */
    private MT makeClientSideQuoProxy(Object remote) {
	try {
	    if (remote instanceof QuoProxy) {
		QuoProxy quo_proxy = (QuoProxy) remote;
		CougaarWrapper wrapper = makeClientAdapter();
		if (wrapper != null) {
		    wrapper.connect(quo_proxy.mt, quo_proxy.mti);
		}
		return (MT) wrapper;
	    } else if (remote instanceof MT) {
		return (MT) remote;
	    } else {
		System.err.println("Object is neither an MT nor a QuoProxy: "
				   +  remote);
		return null;
	    }
	} catch (java.rmi.RemoteException re) {
	    re.printStackTrace();
	    return null;
	}
    }



    /**
     * RemoteImpl cutpoint: make a QuoProxy for the MT and its
     * corresponding MTInstrumented wrapper.  */
    protected Object makeServerSideQuoProxy(MT mt)
    {
	ServerWrapper wrapper = null;
	RMISocketFactory socfac =
	    (mt instanceof MTImpl) ?
	    ((MTImpl) mt).getSocketFactory() :
	    RMISocketFactory.getDefaultSocketFactory();
	com.bbn.quo.rmi.QuoKernel kernel = Utils.getKernel();
	try {
	    wrapper = new ServerWrapper(0, socfac, socfac);
	    wrapper.connect(kernel, mt);
	    return new QuoProxy(mt, wrapper);
	} catch (java.rmi.RemoteException ex) {
	    ex.printStackTrace();
	    return mt;
	}
    }


    public Object getDelegate(Object delegate, Class type) 
    {
	if (type == MT.class) {
	    return makeClientSideQuoProxy(delegate);
	} else if (type == MTImpl.class) {
	    return makeServerSideQuoProxy((MT) delegate);
	} else {
	    return null;
	}
    }

}
