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

public class PollUrlDataFeed extends PropertiesDataFeed {
    
    private Map<String, Set<DataFeedListener>> listeners;
    private int pollPeriodMillis;
    
    public PollUrlDataFeed(String[] args) {
        super(args);
    }
    
    @Override
   protected void parseArgs(String[] args) {
        super.parseArgs(args);
        listeners = new HashMap<String, Set<DataFeedListener>>();
        pollPeriodMillis = 10000;
        int i = 0;
        for (i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-poll")) {
                pollPeriodMillis = Integer.parseInt(args[++i]);
                break;
            }
        }
    }
    
    @Override
   protected void initialize() {
        if (pollPeriodMillis <= 0) {
            log.error("Poll period must be greater than 0 but is " +pollPeriodMillis);
            return;
        }
        RSSUtils.schedule(new Poller(), 0, pollPeriodMillis);
    }
    
    @Override
   public void removeListenerForKey(DataFeedListener listener, String key) {
        Set<DataFeedListener> keyListeners = null;
        synchronized (listeners) {
            keyListeners = listeners.get(key);
        }
        if (keyListeners != null) {
            synchronized (keyListeners) {
                keyListeners.remove(listener);
            }
        }
    }

    @Override
   public void addListenerForKey(DataFeedListener listener, String key) {
        Set<DataFeedListener> keyListeners = null;
        synchronized (listeners) {
            keyListeners = listeners.get(key);
            if (keyListeners == null) {
                keyListeners = new HashSet<DataFeedListener>();
                listeners.put(key, keyListeners);
            }
        }
        synchronized (keyListeners) {
            keyListeners.add(listener);
        }
    }
    
    private void updateListeners(String listenerKey, DataValue value) {
        // Is anyone interested in key
        Set<DataFeedListener> snapshot = null;
        synchronized (listeners) {
            Set<DataFeedListener> keyListeners = listeners.get(listenerKey);
            if (keyListeners != null) {
                snapshot = new HashSet<DataFeedListener>(keyListeners);
            }
        }
        
        if (snapshot != null) {
            for (DataFeedListener listener : snapshot) {
                listener.newData(this, listenerKey, value);
            }
        }
    }
    
    private final class Poller implements Runnable {
        public void run() {
            capture();
            Map<String, DataValue> values = collectValues();
            for (Map.Entry<String, DataValue> entry : values.entrySet()) {
                updateListeners(entry.getKey(), entry.getValue());
            }
        }
    }
}
