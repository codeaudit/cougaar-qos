/* =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;


import org.cougaar.core.mts.DestinationLink;
import org.cougaar.core.mts.SSLRMILinkProtocol;
import org.cougaar.core.mts.AttributedMessage;
import org.cougaar.core.mts.MessageAddress;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.ValueSC;
import com.bbn.quo.rmi.SysCond;
import com.bbn.quo.rmi.ExpectedBandwidthSC;


public class RemoteSSLQosketImpl
    extends RemoteSSLQosketSkel
{
	    

    private DestinationLink link;


    public void setDestinationLink(DestinationLink link) {
	this.link = link;
    }

    public void setServices(ValueSC trust, ValueSC useSSL)
    {

	this.trust = trust;     // These two are from RemoteSSL.cdl
	this.UseSSL = useSSL;
    }

    public int computeCost(AttributedMessage message) 
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
	SyscondFactory factory = SyscondFactory.getFactory();
	Bandwidth = factory.getExpectedBandwidthForAgentSyscond(destination);
    }

}
