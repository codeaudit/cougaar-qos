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

package org.cougaar.core.qos.rss;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.node.NodeControlService;
import org.cougaar.core.qos.metrics.DataFeedRegistrationService;
import org.cougaar.core.qos.metrics.GossipKeyDistributionService;
import org.cougaar.core.qos.metrics.GossipUpdateService;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;

import com.bbn.quo.data.DataFeedListener;
import com.bbn.quo.data.SimpleQueueingDataFeed;

public class GossipFeedComponent
    extends QosComponent
{
    public static final int PROPAGATION = 1;

    private GossipFeed feed;
    private ServiceBroker sb;
    private LoggingService loggingService;
    private GossipUpdateService updateService;
    private GossipKeyDistributionService keyService;
    private MetricInterpreter interpreter = new MetricInterpreter();
    private int propagation;

    public void load() {
	super.load();
	sb = getServiceBroker();
	loggingService = (LoggingService)
	    sb.getService(this, LoggingService.class, null);

	propagation = (int) getParameter("propagation", PROPAGATION);

	feed = new GossipFeed(sb);


	DataFeedRegistrationService svc = (DataFeedRegistrationService)
	    sb.getService(this, DataFeedRegistrationService.class, null);
	svc.registerFeed(feed, "GossipFeed");

	updateService = new GossipUpdateServiceImpl();
	NodeControlService ncs = (NodeControlService)
	    sb.getService(this, NodeControlService.class, null);
	ServiceBroker rootsb = ncs.getRootServiceBroker();
	ServiceProvider sp = new GossipServices();
	rootsb.addService(GossipUpdateService.class, sp);
    }

    private synchronized void ensureKeyService() {
	if (keyService == null) {
	    keyService = (GossipKeyDistributionService)
		sb.getService(this, GossipKeyDistributionService.class, null);
	}
    }

    private class GossipServices implements ServiceProvider {
	public Object getService(ServiceBroker sb, 
				 Object requestor, 
				 Class serviceClass) 
	{
	    if (serviceClass == GossipUpdateService.class) {
		return updateService;
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


    private class GossipUpdateServiceImpl
	implements GossipUpdateService
    {
	public void updateValue(String key, Metric metric) {
	    feed.newData(key, metric, interpreter);
	}
    }


    private class GossipFeed extends SimpleQueueingDataFeed {
	private Schedulable thread;


	GossipFeed(ServiceBroker sb) {
	    super();
	    ThreadService threadService = (ThreadService)
		sb.getService(this, ThreadService.class, null);
	    Runnable notifier = getNotifier();
	    thread = threadService.getThread(this, notifier, "GossipFeed");
	}

	protected void dispatch() {
	    thread.start();
	}


	public void removeListenerForKey(DataFeedListener listener, String key)
	{
	    super.removeListenerForKey(listener, key);
	    ensureKeyService();
	    if (keyService != null) keyService.removeKey(key);
	}

	public void addListenerForKey(DataFeedListener listener, String key) {
	    super.addListenerForKey(listener, key);
	    if (listener instanceof GossipIntegraterDS.GossipFormula) return;
	    ensureKeyService();
	    if (keyService != null) keyService.addKey(key, propagation);
	}
    }


}
