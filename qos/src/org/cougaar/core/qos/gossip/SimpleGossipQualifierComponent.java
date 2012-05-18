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
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.core.qos.metrics.CredibilityQualifier;
import org.cougaar.core.qos.metrics.DeltaValueQualifier;
import org.cougaar.core.qos.metrics.MetricNotificationQualifier;
import org.cougaar.mts.base.BoundComponent;

/**
 * Default Gossip Qualifier implementation which specifies the
 * restrictions on keys and values being sent to neighbors
 */
public class SimpleGossipQualifierComponent
    extends BoundComponent 
{
    @Override
   public void load() {
	super.load();

	ServiceBroker sb = getServiceBroker();
	NodeIdentificationService nis = sb.getService(this, NodeIdentificationService.class, null);
	String node = nis.getMessageAddress().getAddress();

	ServiceProvider sp = new Provider(node);
	sb.addService(GossipQualifierService.class, sp);
    }


    private static class Provider implements ServiceProvider {
	private GossipQualifierService impl;

	Provider(String node) {
	    impl = new Impl(node);
	}

	public Object getService(ServiceBroker sb, 
				 Object requestor, 
				 Class serviceClass) 
	{
	    if (serviceClass == GossipQualifierService.class) {
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
    }

    private static class Impl implements GossipQualifierService, Constants
    {
	String my_addr;
	String host_key;
	String ipflow_key;
	String node;
	String node_key;

	Impl(String node) {
	    this.node = node;
	    try {
		my_addr = java.net.InetAddress.getLocalHost().getHostAddress();
	    } catch (java.net.UnknownHostException ex) {
		my_addr = "127.0.0.1";
	    }
	    host_key = "Host_" + my_addr;
	    ipflow_key = "Ip_Flow_" + my_addr;
	    node_key = "Node_" + node;
	}

	public MetricNotificationQualifier getNotificationQualifier(String key)
	{
	    if (key.matches("^Agent_.*_((Spoke)|(Heard))Time$")) {
		// These values are milliseconds.
		return new DeltaValueQualifier(5000.0);
	    } else {
		return new CredibilityQualifier(SYS_DEFAULT_CREDIBILITY);
	    }
	}


	public boolean shouldForwardRequest(String key) {
	    return 
		!key.startsWith(host_key) &&
		!key.startsWith("Site_Flow") &&
		!key.startsWith("Service") &&
		!key.startsWith("AgentFlow") &&
		!key.startsWith(node_key) &&
		key.indexOf("169.0.0.1") == -1;
	}

    }

}

