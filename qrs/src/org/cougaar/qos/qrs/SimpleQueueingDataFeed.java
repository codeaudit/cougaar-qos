/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>

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
