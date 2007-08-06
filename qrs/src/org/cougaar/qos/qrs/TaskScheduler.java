/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public interface TaskScheduler
{
    // Returns a generic task object that can be passed to unschedule
    public Object schedule(Runnable body, long delay, long period);
    public Object schedule(Runnable body, long delay);

    public void unschedule(Object task);
}
