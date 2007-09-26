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

package org.cougaar.qos.qrs.sysstat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cougaar.qos.qrs.CorbaUtils;
import org.cougaar.qos.qrs.DataInterpreter;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;
import org.cougaar.util.log.Logger;

/**
 * Supply syststat data directly to an existing DataFeed. This has nothing to do
 * with TypedEventChannels at all but it shares the systat code...
 */
public class DirectSysStatSupplier {
    // "Jips" is no longer a default
    static final String[] DefaultKinds = {"Memory", "CPU", "LoadAverage", "Sockets", "CPUCount"};

    private final List<SysStatHandler> handlers;
    private Task task;
    private final SimpleQueueingDataFeed feed;
    private final Interpreter interpreter = new Interpreter();
    private final Map<String, DataValue> map = new HashMap<String, DataValue>();

    private static class Interpreter implements DataInterpreter<DataValue> {
        public double getCredibility(DataValue x) {
            return x.getCredibility();
        }

        public DataValue getDataValue(DataValue x) {
            return x;
        }
    }

    public DirectSysStatSupplier(String kinds[], SimpleQueueingDataFeed feed) {
        this.feed = feed;

        String host = CorbaUtils.hostaddr();
        if (kinds == null) {
            kinds = DefaultKinds;
        }
        handlers = new ArrayList<SysStatHandler>();
        for (String element : kinds) {
            try {
                handlers.add(SysStatHandler.getHandler(element, host, 0));
            } catch (SysStatHandler.NoSysStatHandler err) {
                Logger logger = Logging.getLogger(DirectSysStatSupplier.class);
                logger.warn(err.getMessage());
            }
        }
    }

    public void addProcessSuppliers(int pid) {
        String host = CorbaUtils.hostaddr();

        synchronized (handlers) {
            try {
                handlers.add(SysStatHandler.getHandler("ProcessStats", host, pid));
            } catch (SysStatHandler.NoSysStatHandler err) {
                Logger logger = Logging.getLogger(DirectSysStatSupplier.class);
                logger.warn(err.getMessage());
            }
        }
    }

    private class Task implements Runnable {
        public void run() {
            map.clear();
            Logger logger = Logging.getLogger(DirectSysStatSupplier.class);
            synchronized (handlers) {
                for (SysStatHandler handler : handlers) {
                    if (handler == null) {
                        continue;
                    }
                    try {
			handler.getData(map);
		    } catch (RuntimeException e) {
			logger.warn("Handler " + handler+ " failed " +
				e.getMessage());
		    }
                }
            }

            for (Map.Entry<String,DataValue> entry : map.entrySet()) {
                String key = entry.getKey();
                DataValue value = entry.getValue();
                if (logger.isInfoEnabled()) {
                    logger.info(key + "=" + value);
                }
                feed.newData(key, value, interpreter);
            }
        }
    }

    public synchronized void schedule(int interval) {
        if (task != null) {
            return;
        }
        task = new Task();
        RSSUtils.schedule(task, 0, interval);
    }

}
