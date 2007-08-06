/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * The data-supplier interface: listeners, naming, query. */
public interface DataFeed
{
    public void removeListenerForKey(DataFeedListener listener, String key);
    public void addListenerForKey(DataFeedListener listener, String key);
    public DataValue lookup(String key);
    public String getName();
    public void setName(String name);
}
