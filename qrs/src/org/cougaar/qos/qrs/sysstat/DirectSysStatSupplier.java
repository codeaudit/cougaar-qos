/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cougaar.qos.qrs.CorbaUtils;
import org.cougaar.qos.qrs.DataInterpreter;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;

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
                    handler.getData(map);
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
