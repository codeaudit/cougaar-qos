/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;


import org.apache.log4j.Logger;
import org.cougaar.qos.qrs.CorbaUtils;
import org.cougaar.qos.qrs.DataInterpreter;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;
import org.cougaar.qos.qrs.RSSUtils;
import org.cougaar.qos.qrs.SimpleQueueingDataFeed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Supply syststat data directly to an existing DataFeed.  This has
 * nothing to do with TypedEventChannels at all but it shares the
 * systat code...
 */
public class DirectSysStatSupplier
{
    // "Jips" is no longer a default
    static final String[] DefaultKinds = 
    { "Memory", "CPU", "LoadAverage", "Sockets",  "CPUCount"};

    private ArrayList handlers;
    private Task task;
    private SimpleQueueingDataFeed feed;
    private Interpreter interpreter = new Interpreter();
    private HashMap map = new HashMap();

    private static class Interpreter implements DataInterpreter {
	public double getCredibility(Object x) {
	    return ((DataValue) x).getCredibility();
	}

	public DataValue getDataValue(Object x) {
	    return (DataValue) x;
	}
    }

    public DirectSysStatSupplier(String kinds[], SimpleQueueingDataFeed feed) {
	this.feed = feed;
	
	String host = CorbaUtils.hostaddr();
	if (kinds == null) kinds = DefaultKinds;
	handlers = new ArrayList();
	for (int i=0; i<kinds.length; i++) {
	    try {
		handlers.add(SysStatHandler.getHandler(kinds[i], host, 0));
	    }
	    catch (SysStatHandler.NoSysStatHandler err) {
		Logger logger = Logging.getLogger(DirectSysStatSupplier.class);
		logger.warn(err.getMessage());
	    }
	}
    }

    public void addProcessSuppliers(int pid)
    {
	String host = CorbaUtils.hostaddr();
	
	synchronized (handlers) {
	    try {
		handlers.add(SysStatHandler.getHandler("ProcessStats", host, pid));
	    }
	    catch (SysStatHandler.NoSysStatHandler err) {
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
		for (int i=0; i<handlers.size(); i++) {
		    SysStatHandler handler = (SysStatHandler) handlers.get(i);
		    if (handler == null) continue;
		    handler.getData(map);
		}
	    }

	    Iterator itr = map.entrySet().iterator();
	    while (itr.hasNext()) {
		Map.Entry entry = (Map.Entry) itr.next();
		String key = (String) entry.getKey();
		DataValue value = (DataValue) entry.getValue();
		if (logger.isInfoEnabled())
		    logger.info(key +"="+ value);
		feed.newData(key, value, interpreter);
	    }
	}
    }
    
    public synchronized void schedule(int interval) {
	if (task != null) return;
	task = new Task();
	RSSUtils.schedule(task, 0, interval);
    }


}
