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

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;

import com.bbn.quo.data.TaskScheduler;

public class CougaarTimer implements TaskScheduler
{
    private ThreadService threadService;

    CougaarTimer(ServiceBroker sb) 
    {
	threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
    }

    public Object schedule(Runnable body, long delay) 
    {
	Schedulable sched = threadService.getThread(this, body);
	sched.schedule(delay);
	return sched;
    }

    public Object schedule(Runnable body, long delay, long period) 
    {
	Schedulable sched = threadService.getThread(this, body);
	sched.schedule(delay, period);
	return sched;
    }

    public void unschedule(Object task)
    {
	if (task != null) ((Schedulable) task).cancelTimer();
    }


}
