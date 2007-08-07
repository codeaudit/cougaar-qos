/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import org.cougaar.util.CircularQueue;

abstract public class SimpleQueueingDataFeed extends AbstractDataFeed {
    private final HashMap listeners;
    private final HashMap data;
    private final CircularQueue queue;
    private final Runnable notifier;

    private class Notifier implements Runnable {
        public void run() {
            String key = null;
            DataValue value = null;
            while (true) {
                key = nextKey();
                if (key == null) {
                    break;
                }
                value = lookup(key);
                if (value == null) {
                    continue;
                }

                notifyListeners(key, value);
            }
        }
    }

    protected Runnable makeNotifier() {
        return new Notifier();
    }

    protected SimpleQueueingDataFeed() {
        listeners = new HashMap();
        data = new HashMap();
        queue = new CircularQueue();
        notifier = makeNotifier();
    }

    protected String nextKey() {
        String next = null;
        synchronized (queue) {
            if (!queue.isEmpty()) {
                next = (String) queue.next();
            }
        }
        return next;
    }

    protected boolean isEmpty() {
        synchronized (queue) {
            return queue.isEmpty();
        }
    }

    protected void dispatch() {
        notifier.run();
    }

    protected Runnable getNotifier() {
        return notifier;
    }

    public void removeListenerForKey(DataFeedListener listener, String key) {
        HashSet key_listeners = (HashSet) listeners.get(key);
        if (key_listeners != null) {
            synchronized (key_listeners) {
                key_listeners.remove(listener);
            }
        }
    }

    public void addListenerForKey(DataFeedListener listener, String key) {
        HashSet key_listeners = null;
        synchronized (listeners) {
            key_listeners = (HashSet) listeners.get(key);
            if (key_listeners == null) {
                key_listeners = new HashSet();
                listeners.put(key, key_listeners);
            }
        }
        synchronized (key_listeners) {
            key_listeners.add(listener);
        }
    }

    protected void notifyListeners(String key, DataValue value) {
        HashSet key_listeners = (HashSet) listeners.get(key);
        if (key_listeners != null) {
            synchronized (key_listeners) {
                Iterator i = key_listeners.iterator();
                while (i.hasNext()) {
                    DataFeedListener listener = (DataFeedListener) i.next();
                    listener.newData(this, key, value);
                }
            }
        }
    }

    public DataValue lookup(String key) {
        return (DataValue) data.get(key);
    }

    public void newData(String key, Object raw, DataInterpreter interpreter) {
        synchronized (data) {
            DataValue old_value = lookup(key);
            DataValue new_value;
            double credibility;
            if (interpreter != null) {
                credibility = interpreter.getCredibility(raw);
            } else {
                credibility = ((DataValue) raw).getCredibility();
            }
            if (old_value == null || old_value.getCredibility() <= credibility) {
                if (interpreter != null) {
                    new_value = interpreter.getDataValue(raw);
                } else {
                    new_value = (DataValue) raw;
                }
                data.put(key, new_value);
            } else {
                return;
            }
        }

        synchronized (queue) {
            if (!queue.contains(key)) {
                queue.add(key);
                dispatch();
            }
        }
    }

}
