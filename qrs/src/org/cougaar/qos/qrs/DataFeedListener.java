/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * The interface for objects which want to listen for new data on a given data
 * feed. DataFeedListeners register with the feed via addListenerForKey.
 */
public interface DataFeedListener {
    public void newData(DataFeed store, String key, DataValue data);
}
