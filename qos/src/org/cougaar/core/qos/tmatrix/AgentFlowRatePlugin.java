/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.core.qos.tmatrix;

import java.util.HashMap;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.core.qos.metrics.DecayingHistory;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricImpl;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;



/* Load this Plugin at LOW priority since it needs another plugin's service
 */
public class AgentFlowRatePlugin
  extends ComponentPlugin
  implements Runnable, Constants
{
    private static final int BASE_PERIOD = 10; //10SecAVG
    private LoggingService logging;

    private class AgentFlowHistory extends DecayingHistory {
	private static final double CREDIBILITY = SECOND_MEAS_CREDIBILITY;
	String msgKey;
	String byteKey;
	MessageAddress orig = null;
	MessageAddress target = null;
    
	AgentFlowHistory(MessageAddress orig, MessageAddress target) {
	    super(10, 3, BASE_PERIOD);
	    this.orig = orig;
	    this.target = target;
	    String flowKey=
		"AgentFlow" +KEY_SEPR+ orig +KEY_SEPR+ target +KEY_SEPR;
	    msgKey=(flowKey+ MSG_RATE).intern();
	    addKey(msgKey);
	    byteKey=(flowKey+ BYTE_RATE).intern() ;
	    addKey(byteKey);
	}
    
	// done on records, not the whole matrix
	public void newAddition(KeyMap keys,
				DecayingHistory.SnapShot now_raw,
				DecayingHistory.SnapShot last_raw) 
	{
	    TrafficMatrix.TrafficRecord now = (TrafficMatrix.TrafficRecord) 
		now_raw;
	    TrafficMatrix.TrafficRecord last = (TrafficMatrix.TrafficRecord)
		last_raw;
	    double deltaT = (now.timestamp -last.timestamp) / 1000.0;
	    double deltaMsgs = now.msgCount -last.msgCount;
	    double deltaBytes = now.byteCount -last.byteCount;
      
	    String msgAvgKey =keys.getKey(msgKey);
	    String byteAvgKey =keys.getKey( byteKey);
      
	    Metric msgAvg = new MetricImpl(new Double( deltaMsgs/deltaT),
					   CREDIBILITY,
					   "msg/sec",
					   "AgentFlowRate");
	    metricsUpdateService.updateValue(msgAvgKey, msgAvg);
      
	    Metric byteAvg = new MetricImpl(new Double( deltaBytes/deltaT),
					    CREDIBILITY,
					    "bytes/sec",
					    "AgentFlowRate");
	    metricsUpdateService.updateValue(byteAvgKey, byteAvg);
	    if (logging.isDebugEnabled())
		logging.debug("key="+msgAvgKey+" Value="+msgAvg);
	}
    }

    private TrafficMatrixStatisticsService agentFlowService;
    private MetricsUpdateService metricsUpdateService;
    private Schedulable schedulable;
    private HashMap histories;
  
    public AgentFlowRatePlugin() {
	histories = new HashMap();
    }
  
    // Local
    AgentFlowHistory findOrMakeHistory(MessageAddress orig, MessageAddress target) {
	HashMap submap = (HashMap) histories.get(orig.getPrimary());
	if (submap == null) {
	    submap = new HashMap();
	    histories.put(orig.getPrimary(), submap);
	}
	AgentFlowHistory history = (AgentFlowHistory) submap.get(target.getPrimary());
	if (history == null) {
	    history = new AgentFlowHistory(orig, target);
	    submap.put(target.getPrimary(), history);
	}
	return history;
    }
  
    // Component
    public void load() {
	super.load();
    
	ServiceBroker sb = getServiceBroker();

	logging = (LoggingService)
            sb.getService(this, LoggingService.class, null);

	agentFlowService = (TrafficMatrixStatisticsService)
	    sb.getService(this, TrafficMatrixStatisticsService.class, null);
	if (agentFlowService == null) {
	    logging.error("Can't find TrafficMatrixStatsisticsService. This plugin must be loaded at Low priority");
	    return;
	}
    
	metricsUpdateService = (MetricsUpdateService)
	    sb.getService(this, MetricsUpdateService.class, null);


	ThreadService threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	schedulable = threadService.getThread(this, this, "AgentFlowRatePlugin");
	schedulable.schedule(5000, BASE_PERIOD*1000);
	sb.releaseService(this, ThreadService.class, threadService);
    }
  
  
    // Schedulable body
    public void run() {
	int count =0;
	TrafficMatrix agentFlowSnapshot = agentFlowService.snapshotMatrix();
	// print out for sanity's sake
	if( agentFlowSnapshot!=null) {
	    logging.debug("AgentFlowRatePlugin.agentFlowSnapshot Looks like: " + agentFlowSnapshot);
	}
	TrafficMatrix.TrafficIterator itr = agentFlowSnapshot.getIterator();
	while (itr.hasNext()) {
	    count++;  
	    TrafficMatrix.TrafficRecord record = (TrafficMatrix.TrafficRecord)
		itr.next();
	    MessageAddress orig = itr.getOrig();
	    MessageAddress target = itr.getTarget();
	    AgentFlowHistory history = findOrMakeHistory(orig, target);
	    history.add(record);
	    logging.debug("AgentFlowRatePlugin processed TrafficRecord: " + record);
	}
	if (logging.isDebugEnabled())
	    logging.debug("Processed Traffic Records="+count);
    }
    
    // Plugin
    protected void setupSubscriptions() {
    }
  
    protected void execute() {
    }
}
