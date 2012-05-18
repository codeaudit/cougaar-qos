/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.core.qos.gossip;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.node.NodeControlService;
import org.cougaar.mts.base.AttributedMessage;
import org.cougaar.mts.base.CommFailureException;
import org.cougaar.mts.base.DestinationLink;
import org.cougaar.mts.base.DestinationLinkDelegateImplBase;
import org.cougaar.mts.base.MessageDeliverer;
import org.cougaar.mts.base.MessageDelivererDelegateImplBase;
import org.cougaar.mts.base.MisdeliveredMessageException;
import org.cougaar.mts.base.NameLookupException;
import org.cougaar.mts.base.RPCLinkProtocol;
import org.cougaar.mts.base.StandardAspect;
import org.cougaar.mts.base.UnregisteredNameException;

/** 
 * Aspect to collect Gossip overhead statistics
 */

final public class GossipStatisticsServiceAspect
    extends StandardAspect
    implements ServiceProvider
{
    private GossipTrafficRecord stats;
    private GossipStatisticsService impl;

    public GossipStatisticsServiceAspect()
    {
    }

    @Override
   public void load() {
	super.load();

	stats = new GossipTrafficRecord();
	this.impl = new Impl();

	ServiceBroker sb = getServiceBroker();

	NodeControlService ncs = sb.getService(this, NodeControlService.class, null);

	ServiceBroker rootsb = ncs.getRootServiceBroker();
	sb.releaseService(this, NodeControlService.class, null);

	rootsb.addService(GossipStatisticsService.class, this);

	
    }

    @Override
   public Object getReverseDelegate(Object delegatee, Class type) 
    {
	if (type == MessageDeliverer.class) {
	    return new DelivererDelegate((MessageDeliverer) delegatee);
	} else {
	    return null;
	}
    }

    @Override
   public Object getDelegate(Object delegatee, Class type) 
    {
	if (type == DestinationLink.class) {
	    // RPC Links only!
	    DestinationLink link = (DestinationLink) delegatee;
	    Class cl = link.getProtocolClass();
	    if (RPCLinkProtocol.class.isAssignableFrom(cl)) {
		return new DestinationLinkDelegate(link);
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
    }


    public Object getService(ServiceBroker sb, 
			     Object requestor, 
			     Class serviceClass) 
    {
	if (serviceClass == GossipStatisticsService.class) {
	    return impl;
	} else {
	    return null;
	}
    }

    public void releaseService(ServiceBroker sb, 
			       Object requestor, 
			       Class serviceClass, 
			       Object service)
    {
    }


    void requestsSent(KeyGossip gossip) {
	stats.requests_sent += gossip.size();
    }

    void requestsReceived(KeyGossip gossip) {
	stats.requests_rcvd += gossip.size();
    }

    void valuesSent(ValueGossip gossip) {
	stats.values_sent += gossip.size();
    }

    void valuesReceived(ValueGossip gossip) {
	stats.values_rcvd += gossip.size();
    }

    private boolean hasGossip(Gossip gossip)
    {
	return gossip != null && !gossip.isEmpty();
    }


    private class Impl implements GossipStatisticsService {

	Impl() {
	}

	public GossipTrafficRecord getStatistics() {
	    return new GossipTrafficRecord(stats);
	}

    }

    private class DestinationLinkDelegate 
	extends DestinationLinkDelegateImplBase
    {
	DestinationLinkDelegate(DestinationLink delegatee) {
	    super(delegatee);
	}

	@Override
   public MessageAttributes forwardMessage(AttributedMessage message) 
	    throws UnregisteredNameException, 
		   NameLookupException, 
		   CommFailureException,
		   MisdeliveredMessageException
	{
	    MessageAttributes result = super.forwardMessage(message);
	    // statistics
	    KeyGossip keyGossip = (KeyGossip) 
		message.getAttribute(GossipAspect.REQUEST_GOSSIP_ATTR);
	    ValueGossip valueGossip = (ValueGossip) 
		message.getAttribute(GossipAspect.VALUE_GOSSIP_ATTR);
	    if (hasGossip(keyGossip)) requestsSent(keyGossip);
	    if (hasGossip(valueGossip)) valuesSent(valueGossip);
	    ++stats.msg_sent;
	    if (hasGossip(keyGossip) || hasGossip(valueGossip))
		++stats.msg_with_gossip_sent;
	    return result;
	}

    }

    private class DelivererDelegate 
	extends MessageDelivererDelegateImplBase
    {
	DelivererDelegate(MessageDeliverer delegatee) {
	    super(delegatee);
	}


	@Override
   public MessageAttributes deliverMessage(AttributedMessage message,
						MessageAddress dest)
	    throws MisdeliveredMessageException
	{
	    // statistics
	    KeyGossip keyGossip = (KeyGossip) 
		message.getAttribute(GossipAspect.REQUEST_GOSSIP_ATTR);
	    ValueGossip valueGossip = (ValueGossip) 
		message.getAttribute(GossipAspect.VALUE_GOSSIP_ATTR);
	    if (hasGossip(keyGossip)) requestsReceived(keyGossip);
	    if (hasGossip(valueGossip)) valuesReceived(valueGossip);
	    ++stats.msg_rcvd;
	    if (hasGossip(keyGossip) || hasGossip(valueGossip))
		++stats.msg_with_gossip_rcvd;
	    return super.deliverMessage(message, dest);
	}
	}

}

