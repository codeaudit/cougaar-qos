/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
import org.cougaar.core.thread.Schedulable;

import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.SimpleQueueingDataFeed;


public class TrivialDataFeed 
    extends SimpleQueueingDataFeed
{
    private static final long HOLD_TIME = 500;
    private Schedulable thread;

    private class Notifier implements Runnable {
	public void run() { 
	    long endTime= System.currentTimeMillis() + HOLD_TIME;
	    // String key = nextKey();
	    // DataValue value = null;
	    do {
		String key = nextKey();
		if (key == null) break;
		DataValue value = lookup(key);
		if (value == null) continue;
		notifyListeners(key, value); 
	    } while (System.currentTimeMillis() <= endTime) ;
// 	    if (key != null) {
// 		value = lookup(key);
// 		if (value != null) notifyListeners(key, value); 
// 	    }
	    if (!isEmpty()) thread.start();
	}
    }


    protected Runnable makeNotifier() {
	return new Notifier();
    }

    TrivialDataFeed(ServiceBroker sb) {
	super();
	ThreadService threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	Runnable notifier = getNotifier();
	thread = threadService.getThread(this, notifier, "TrivialDataFeed",
					 ThreadService.WELL_BEHAVED_LANE);
	sb.releaseService(this, ThreadService.class, threadService);
    }

    protected void dispatch() {
	thread.start();
    }

}
