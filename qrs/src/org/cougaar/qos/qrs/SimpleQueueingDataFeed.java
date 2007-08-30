/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cougaar.util.CircularQueue;

abstract public class SimpleQueueingDataFeed extends AbstractDataFeed {
    private final Map<String, Set<DataFeedListener>> listeners;
    private final Map<String, DataValue> data;
    private final CircularQueue<String> queue;
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
        listeners = new HashMap<String, Set<DataFeedListener>>();
        data = new HashMap<String, DataValue>();
        queue = new CircularQueue<String>();
        notifier = makeNotifier();
    }

    protected String nextKey() {
        String next = null;
        synchronized (queue) {
            if (!queue.isEmpty()) {
                next = queue.next();
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
        Set<DataFeedListener> key_listeners = listeners.get(key);
        if (key_listeners != null) {
            synchronized (key_listeners) {
                key_listeners.remove(listener);
            }
        }
    }

    public void addListenerForKey(DataFeedListener listener, String key) {
        Set<DataFeedListener> key_listeners = null;
        synchronized (listeners) {
            key_listeners = listeners.get(key);
            if (key_listeners == null) {
                key_listeners = new HashSet<DataFeedListener>();
                listeners.put(key, key_listeners);
            }
        }
        synchronized (key_listeners) {
            key_listeners.add(listener);
        }
    }

    protected void notifyListeners(String key, DataValue value) {
        Set<DataFeedListener> key_listeners = listeners.get(key);
        if (key_listeners != null) {
            synchronized (key_listeners) {
                for (DataFeedListener listener : key_listeners) {
                    listener.newData(this, key, value);
                }
            }
        }
    }

    public DataValue lookup(String key) {
        return data.get(key);
    }

    public <T> void newData(String key, T raw, DataInterpreter<T> interpreter) {
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
                // XXX: Should the dispatch really keep the queue locked?
                dispatch();
            }
        }
    }

}
