/* =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import org.cougaar.core.mts.DestinationLink;
import org.cougaar.core.mts.SSLRMILinkProtocol;
import org.cougaar.core.society.Message;
import org.cougaar.core.society.MessageAddress;
import org.cougaar.core.qos.monitor.ResourceMonitorService;
import org.cougaar.core.qos.monitor.QosMonitorService;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.ValueSC;
import com.bbn.quo.rmi.SysCond;
import com.bbn.quo.rmi.ExpectedBandwidthSC;
import com.bbn.quo.rmi.ExpectedCapacitySC;

import java.util.Timer;
import java.util.TimerTask;

public class RemoteSSLQosketImpl
    extends RemoteSSLQosketSkel
{
	    
    private static ValueSC USE_SSL;

    private static synchronized ValueSC Get_USE_SSL (QuoKernel kernel) 
	throws java.rmi.RemoteException

    {
	if (USE_SSL == null) {
	    SysCond syscond =  kernel.bindSysCond("UseSSL",
						  "com.bbn.quo.rmi.ValueSC",
						  "com.bbn.quo.ValueSCImpl");
	    System.out.println("Created UseSSL syscond");
	    USE_SSL = (ValueSC) syscond;

	    boolean useSSL = Boolean.getBoolean("org.cougaar.lib.quo.UseSSL");
	    USE_SSL.booleanValue(useSSL);
	}
	return USE_SSL;
    }


    private DestinationLink link;
    private ResourceMonitorService rms;


    public void setDestinationLink(DestinationLink link) {
	this.link = link;
    }

    public void setServices(ResourceMonitorService rms,
			    ValueSC trust) 
    {
	this.rms = rms;
	this.trust = trust; // 'trust' defined in RemoteSSL.cdl
    }

    public int computeCost(Message message) 
    {
	if (link.getProtocolClass() == SSLRMILinkProtocol.class) {
	    return 1;
	} else {
	    return link.cost(message);
	}
    }


	    

    public void initSysconds(QuoKernel kernel) 
	throws java.rmi.RemoteException
    {

	MessageAddress destination = link.getDestination();
	UseSSL = Get_USE_SSL(kernel);
	Bandwidth = 
	    (ExpectedCapacitySC) rms.getExpectedCapacityForAgentSyscond(destination);
    }

}
