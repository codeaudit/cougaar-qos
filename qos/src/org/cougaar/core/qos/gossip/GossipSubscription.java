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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricNotificationQualifier;
import org.cougaar.core.qos.metrics.MetricsService;

/**
 * Metrics callback class for holding the subscription to a Key
 * requested by a neighbor.
 */
class GossipSubscription
{
    private class Callback implements Observer {
	String key;
	Metric current;
	Object subscription_uid;

	Callback(String key) {
	    this.key = key;
	    String path = "GossipIntegrater(" +key+ "):GossipFormula";
	    MetricNotificationQualifier qualifier = null;
	    if (qualifier_svc != null) {
		qualifier = qualifier_svc.getNotificationQualifier(key);
	    }
	    subscription_uid = svc.subscribeToValue(path, this,
						    qualifier);
	}

	public void update(Observable ignore, Object value) {
	    this.current = (Metric) value;
	    addChange(key, this.current);
	}

	void unsubscribe() {
	    svc.unsubscribeToValue(subscription_uid);
	}
    }

    private GossipQualifierService qualifier_svc;
    private MessageAddress neighbor;
    private MetricsService svc;
    private HashMap callbacks;
    private ValueGossip changes;

    GossipSubscription(MessageAddress neighbor, 
		       MetricsService svc,
		       GossipQualifierService qualifierService)
    {
	this.svc = svc;
	this. qualifier_svc = qualifierService;
	this.neighbor = neighbor;
	callbacks = new HashMap();
	changes = null;
    }


    private synchronized void addChange(String key, Metric metric) {
	if (changes == null) changes = new ValueGossip();
	changes.add(key, metric);
    }

    synchronized ValueGossip getChanges() {
	if (changes == null || changes.size() == 0) return null;
	ValueGossip result = new ValueGossip();
	Iterator itr = changes.iterator();
	Map.Entry entry = null;
	while (itr.hasNext()) {
	    entry = (Map.Entry) itr.next();
	    result.addEntry(entry.getKey(), entry.getValue());
	}
	return result;
    }

    synchronized void commitChanges(ValueGossip uncommitted_changes) {
	if (uncommitted_changes == null || uncommitted_changes.size() == 0) return;
	Iterator itr = uncommitted_changes.iterator();
	Map.Entry entry = null;
	Object key = null;
	Object data = null;
	while(itr.hasNext()) {
	    entry = (Map.Entry) itr.next();
	    key = entry.getKey();
	    data = entry.getValue();
	    Object old_data = changes.lookupValue(key);
	    if (old_data != null && data.equals(old_data)) 
		changes.removeEntry(key);
	}
    }

    private void addKey(String key) {
	Callback cb = (Callback) callbacks.get(key);
	if (cb == null) {
	    cb = new Callback(key);
	    callbacks.put(key, cb);
	}
    }

    synchronized void add (KeyGossip gossip) {
	Iterator  itr = gossip.iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String key = (String) entry.getKey();
	    addKey(key);
	}
    }

}

    
