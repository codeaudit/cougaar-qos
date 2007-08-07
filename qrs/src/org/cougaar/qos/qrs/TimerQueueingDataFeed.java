/* =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public class TimerQueueingDataFeed extends SimpleQueueingDataFeed {

    public TimerQueueingDataFeed() {
    }

    protected void dispatch() {
        Runnable task = new Runnable() {
            public void run() {
                getNotifier().run();
            }
        };
        RSSUtils.schedule(task, 0);
    }

}
