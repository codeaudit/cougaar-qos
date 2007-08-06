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
import org.cougaar.core.node.NodeControlService;
import org.cougaar.core.qos.metrics.DataFeedRegistrationService;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.qos.rss.MetricInterpreter;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.qos.qrs.DataFeedListener;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;


/** 
 * QuO RSS DataFeed used to extract a list of Metrics to request and
 * to update their values. 
 */
public class GossipFeedComponent
    extends QosComponent
{
    public static final int PROPAGATION = 1;

    private GossipFeed feed;
    private ServiceBroker sb;
    private GossipUpdateService updateService;
    private GossipKeyDistributionService keyService;
    private MetricInterpreter interpreter = new MetricInterpreter();
    private int propagation;

    public void load() {
	super.load();
	sb = getServiceBroker();
	
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
