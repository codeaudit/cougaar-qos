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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cougaar.qos.ResourceStatus.RSSSubscriberPOA;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.qos.ResourceStatus.ResourceStatusService;

/**
 * RelayDataFeeds are designed to move data from one RSS to another. This first
 * one is simple but inefficient (the same formulas will be calculated at
 * multiple levels) and not all particular about what it passes along
 * (everything).
 */

public class PromiscuousRelayDataFeed extends AbstractDataFeed implements Constants {
    private final Map<String, Set<DataFeedListener>> listeners;
    private final Map<String, DataValue> data;
    private final Set<String> keys;
    private final List<ResourceStatusService> services;

    public PromiscuousRelayDataFeed() {
        super();
        listeners = new HashMap<String, Set<DataFeedListener>>();
        data = new HashMap<String, DataValue>();
        keys = new HashSet<String>();
        services = new ArrayList<ResourceStatusService>();
    }

    public void addService(ResourceStatusService rss) {
        synchronized (services) {
            services.add(rss);
        }
        synchronized (keys) {
            for (String key : keys) {
                Subscriber subscriber = new Subscriber(key, rss);
                try {
                    CorbaUtils.poa.activate_object(subscriber);
                    subscriber.connect();
                } catch (Exception ex) {
                    Logger logger = Logging.getLogger(PromiscuousRelayDataFeed.class);
                    logger.error(null, ex);
                }
            }
        }
    }

    public void removeService(ResourceStatusService rss) {
        synchronized (services) {
            services.remove(rss);
            // much more to do here.
        }
    }

    // Caller should synchronize on keys
    private void makeNewSubscribersForKey(String key) {
        Subscriber subscriber;
        synchronized (services) {
            for (ResourceStatusService rss : services) {
                subscriber = new Subscriber(key, rss);
                try {
                    CorbaUtils.poa.activate_object(subscriber);
                    subscriber.connect();
                } catch (Exception ex) {
                    Logger logger = Logging.getLogger(PromiscuousRelayDataFeed.class);
                    logger.error(null, ex);
                }
            }
        }
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

    private void notifyListeners(String key, DataValue value) {
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
        synchronized (keys) {
            if (!keys.contains(key)) {
                makeNewSubscribersForKey(key);
                keys.add(key);
            }
        }
        return data.get(key);
    }

    private void newData(final String key, org.cougaar.qos.ResourceStatus.DataValue corba_value) {
        synchronized (data) {
            DataValue old_value = lookup(key);
            double credibility = corba_value.credibility;
            if (old_value == null || old_value.getCredibility() <= credibility) {
                DataValue new_value = new DataValue(corba_value);
                data.put(key, new_value);
                notifyListeners(key, new_value);
            }
        }

    }
    
    private class Subscriber extends RSSSubscriberPOA {
        String key;
        ResourceStatusService rss;

        Subscriber(String key, ResourceStatusService rss) {
            this.key = key;
            this.rss = rss;
        }

        void connect() {
            ResourceNode scope_ref = new ResourceNode();
            String[] parameters = {key};
            scope_ref.kind = "Integrater";
            scope_ref.parameters = parameters;
            ResourceNode formula_ref = new ResourceNode();
            formula_ref.kind = "Formula";
            formula_ref.parameters = new String[0];
            ResourceNode[] description = {scope_ref, formula_ref};
            rss.unqualifiedSubscribe(_this(), description, 0);
        }

        public void dataUpdate(int callback_id, final org.cougaar.qos.ResourceStatus.DataValue value) {
            // run this in a different thread?
            Runnable task = new Runnable() {
                public void run() {
                    newData(key, value);
                }
            };
            RSSUtils.schedule(task, 0);
        }
    }
}
