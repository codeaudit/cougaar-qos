/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public interface EventSubscriber {
    public void rssEvent(ResourceContext context, RSS.Event event_type);
}