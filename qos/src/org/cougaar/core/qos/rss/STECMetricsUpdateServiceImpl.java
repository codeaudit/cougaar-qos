/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import org.cougaar.core.service.ThreadService;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.component.ServiceBroker;

import com.bbn.quo.event.Connector;
import com.bbn.quo.event.status.StatusTEC;
import com.bbn.quo.event.topology.SimpleTopologyManager;

import org.omg.CosTypedEventChannelAdmin.TypedEventChannel;
import org.omg.CORBA.ORB;

import java.util.TimerTask;

public class STECMetricsUpdateServiceImpl
    implements MetricsUpdateService
{
    private static final String STEC_URL_PROPERTY =
	"org.cougaar.metrics.stec.url";
    
    private ServiceBroker sb;
    private STECSender sender;



    public STECMetricsUpdateServiceImpl(ServiceBroker sb) {
	this.sb = sb;

	// Make ORB
	String[] args = new String[0];
	Connector.orb = ORB.init(args, null);

	TypedEventChannel channel = getChannel();
	sender = new STECSender(channel, Connector.poa());

	Heartbeater beater = new Heartbeater(sender);
	ThreadService threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	TimerTask task = threadService.getTimerTask(this, beater, "Beater");
	threadService.schedule(task, 0, 1000);
    }


    private TypedEventChannel getChannel() {
  	String url = System.getProperty(STEC_URL_PROPERTY);
	String args[] = 
	    { "-queue",
	      "com.bbn.quo.event.status.PrioritizedStatusChunkingQ",
	      "-policy",
	      "com.bbn.quo.event.status.StatusValidatingCachingPolicy",
	      "-url",
	      url,
	      "-id",
	      "foo"};
	return new StatusTEC(args)._this();
    }

    public void updateValue(String key, String type, Metric value) {
	sender.send(key, type, value);
    }


}
