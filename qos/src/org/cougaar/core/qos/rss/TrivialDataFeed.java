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
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.CircularQueue;

import com.bbn.quo.data.AbstractDataFeed;
import com.bbn.quo.data.DataFeed;
import com.bbn.quo.data.DataFeedListener;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TrivialDataFeed 
    extends AbstractDataFeed
    implements Constants
{
    private HashMap listeners;
    private HashMap data;
    private ThreadService threadService;
    private CircularQueue queue;
    private Schedulable thread;


    private class Notifier implements Runnable {
	public void run() { 
	    String key = null;
	    DataValue value = null;
	    while (true) {
		synchronized (queue) {
		    if (queue.isEmpty()) break;
		    key = (String) queue.next();
		}
		if (key == null) continue;
		value = lookup(key);
		if (value == null) continue;
		notifyListeners(key, value); 
	    }
	}
    }

    TrivialDataFeed(ServiceBroker sb) {
	listeners = new HashMap();
	data = new HashMap();
	threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	queue = new CircularQueue();
	Runnable notifier = new Notifier();
	thread = threadService.getThread(this, notifier, "TrivialDataFeed");
    }

    public synchronized void removeListenerForKey(DataFeedListener listener, 
						  String key) 
    {
	ArrayList key_listeners = (ArrayList) listeners.get(key);
	if (key_listeners != null) key_listeners.remove(listener);
    }

    public synchronized void addListenerForKey(DataFeedListener listener,
					       String key) 
    {
	ArrayList key_listeners = (ArrayList) listeners.get(key);
	if (key_listeners == null) {
	    key_listeners = new ArrayList();
	    listeners.put(key, key_listeners);
	}
	if (!key_listeners.contains(listener)) key_listeners.add(listener);
    }





    private synchronized void notifyListeners(String key,
					      DataValue value)
    {
	ArrayList key_listeners = (ArrayList) listeners.get(key);
	if (key_listeners != null) {
	    Iterator i = key_listeners.iterator();
	    while (i.hasNext()) {
		DataFeedListener listener = (DataFeedListener) i.next();
		listener.newData(this, key, value);
	    }
	}
    }


    public DataValue lookup(String key) {
	return (DataValue) data.get(key);
    }



    public synchronized void newData(String key, Metric metric) {
	DataValue old_value = lookup(key);
	double credibility = metric.getCredibility();
	if (old_value == null || old_value.getCredibility() <= credibility) {
	    String prov = metric.getProvenance();
	    String units = metric.getUnits();
	    Object val = metric.getRawValue();
	    DataValue value = new DataValue(val, credibility, units, prov);
	    data.put(key, value);
	    boolean new_queue_entry = false;
	    synchronized (queue) {
		if (!queue.contains(key)) {
		    queue.add(key);
		    new_queue_entry = true;
		}
	    }
	    if (new_queue_entry) thread.start();
	}
    }

}
