/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;
import org.cougaar.qos.ResourceStatus.ResourceNode;

public class RSSUtils implements Constants {

    private static TaskScheduler scheduler;
    private static final Logger logger = Logging.getLogger(RSSUtils.class);

    // Pretty-Printers
    public static void nodeToString(StringBuffer buf, ResourceNode node) {
        buf.append("<Resource ");
        buf.append(node.kind);
        buf.append("(");
        for (int i = 0; i < node.parameters.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(node.parameters[i]);
        }
        buf.append(")>");
    }

    public static String nodeToString(ResourceNode node) {
        StringBuffer buf = new StringBuffer();
        nodeToString(buf, node);
        return buf.toString();
    }

    public static void pathToString(StringBuffer buf, ResourceNode[] path) {
        buf.append("[");
        for (int i = 0; i < path.length; i++) {
            if (i > 0) {
                buf.append(" ");
            }
            nodeToString(buf, path[i]);
        }
        buf.append("]");
    }

    public static String pathToString(ResourceNode[] path) {
        StringBuffer buf = new StringBuffer();
        pathToString(buf, path);
        return buf.toString();
    }

    // Semi-abstract scheduling
    public static synchronized void setScheduler(TaskScheduler scheduler) {
        RSSUtils.scheduler = scheduler;
    }

    private static synchronized TaskScheduler getScheduler() {
        if (scheduler == null) {
            scheduler = new TaskSchedulerImpl();
        }
        return scheduler;
    }

    public static Object schedule(Runnable body, long delay, long period) {
        return getScheduler().schedule(body, delay, period);
    }

    public static Object schedule(Runnable body, long delay) {
        return getScheduler().schedule(body, delay);
    }

    public static void unschedule(Object task) {
        getScheduler().unschedule(task);
    }

}
