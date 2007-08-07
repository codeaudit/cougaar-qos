/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is provides listener support and a simple caching scheme. Despite
 * the name, it's not particularly 'standard', in fact it's really only suitable
 * for feeds which want to avoid calculating values on the fly, because of the
 * expense involved. The only uses so far seem to be Remos and Erni.
 */
abstract public class StandardDataFeed extends AbstractDataFeed {

    /**
     * Indexed by key, each entry is a Vector of listeners for that key.
     */
    private final Hashtable<String, Vector<DataFeedListener>> listenerMap = 
        new Hashtable<String, Vector<DataFeedListener>>();

    /**
     * Indexed by key, each entry is a the cached value from last poll
     */
    private final Hashtable<String, DataValue> valueMap = new Hashtable<String, DataValue>();

    /**
     * Return the cached value if it exists, otherwise NO_VALUE.
     */
    private DataValue getValueForKey(String key) {
        DataValue value = valueMap.get(key);
        if (value == null) {
            return DataValue.NO_VALUE;
        } else {
            return value;
        }
    }

    private void notifyListeners(DataValue value, String key) {
        Vector<DataFeedListener> listeners = listenersForKey(key);
        if (listeners != null) {
            for (DataFeedListener listener : listeners) {
                listener.newData(this, key, value);
            }
        }
    }

    private Vector<DataFeedListener> listenersForKey(String key) {
        return listenerMap.get(key);
    }

    /**
     * Subclass method should return true here iff the key is relevant to the
     * subclass. If the key is not valid, the subclass will never be asked to
     * compute a value for it.
     */
    abstract protected boolean validateKey(String key);

    /**
     * This method does the real work of finding the current value associated
     * with the given key. This should not usually be called as part of a query
     * (ie from lookup, either directly or indirectly).
     */
    abstract protected DataValue updateValue(String key);

    /**
     * Subclasses should call this to force an update of the cache. The second
     * arg indicates whether or not the listeners should be notified.
     */
    protected void updateValueForKey(String key, boolean notify) {
        DataValue value = updateValue(key);
        valueMap.put(key, value);
        if (notify) {
            notifyListeners(value, key);
        }
    }

    /**
     * Forces and caches an update of all listener keys.
     */
    protected void updateAll() {
        for (String key : listenerMap.keySet()) {
            updateValueForKey(key, true);
        }
    }

    /**
     * The query interface. A real value is returned iff the key is meaningful
     * for this feed and a value is currently cached. No computation or updating
     * happens here.
     */
    public DataValue lookup(String key) {
        if (validateKey(key)) {
            return getValueForKey(key);
        } else {
            return DataValue.NO_VALUE;
        }
    }

    /**
     * Add a listener for the given key. Whenever a new value for this key
     * enters the store, all listeners will be notified. This method is part of
     * the DataFeed interface.
     */
    public void addListenerForKey(DataFeedListener listener, String key) {
        if (!validateKey(key)) {
            return;
        }

        Vector<DataFeedListener> listeners = listenersForKey(key);
        if (listeners == null) {
            listeners = new Vector<DataFeedListener>(10);
            listenerMap.put(key, listeners);
        }
        listeners.addElement(listener);
    }

    /**
     * Remove a listener for the given key. This method is part of the DataFeed
     * interface.
     */
    public void removeListenerForKey(DataFeedListener listener, String key) {
        Vector<DataFeedListener> listeners = listenersForKey(key);
        if (listeners != null) {
            listeners.removeElement(listener);
        }
    }

}
