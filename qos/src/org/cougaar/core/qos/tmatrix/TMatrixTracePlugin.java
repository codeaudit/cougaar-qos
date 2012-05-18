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

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;

/* 
 * Simple TrafficMatrix example client, meant to show usage. 
 * Loaded on the Robustness Manager Agent, which snapshots the matrix at intervals 
 * and logs them at LEVEL=DEBUG
 */
public class TMatrixTracePlugin
    extends ComponentPlugin
    implements Runnable, Constants
{
    private static final int BASE_PERIOD = 10; //10SecAVG
    private LoggingService logging;
    private CommunityTrafficMatrixService agentFlowService;
    private Schedulable schedulable;
  
    public TMatrixTracePlugin() {
    }
  
    // Component
    @Override
   public void load() {
	super.load();
	
	ServiceBroker sb = getServiceBroker();
	
	logging = sb.getService(this, LoggingService.class, null);
	
	// TrafficMatrix accessor service
	agentFlowService = sb.getService(this, CommunityTrafficMatrixService.class, null);
	if (agentFlowService == null) {
	    if (logging.isErrorEnabled()) {
		logging.error("Can't find CommunityTrafficMatrixService. This plugin must be loaded at Low priority");
	    }
	    return;
	}
	
	ThreadService threadService = sb.getService(this, ThreadService.class, null);
	schedulable = threadService.getThread(this, this, "TMatrixTracePlugin");
	schedulable.schedule(5000, BASE_PERIOD*1000);
	sb.releaseService(this, ThreadService.class, threadService);
    }
  
  
    // Examples - Get the TrafficMatrix & print out at an interval
    public void run() {
	TrafficMatrix agentFlowSnapshot = agentFlowService.snapshotMatrix();
	/*
	// Ex.#1: print out matrix using TrafficIterator
	if(agentFlowSnapshot != null) {
	    if(logging.isDebugEnabled()) {
		logging.debug("Printing out traffic matrix using the TrafficIterator: ");	   	    
	    }
	    TrafficMatrix.TrafficIterator iter = agentFlowSnapshot.getIterator();
	    while(iter.hasNext()) {
		TrafficMatrix.TrafficRecord newRecord = (TrafficMatrix.TrafficRecord) iter.next();
		MessageAddress orig = iter.getOrig();
		MessageAddress target = iter.getTarget();
		if(logging.isDebugEnabled()) {
		    logging.debug("Orig="+orig+", Target="+target+": "+newRecord);
		}
	    }
	    
	    
	    // Ex.#2: get data between two individual & print out
	    double msgCt = agentFlowSnapshot.getMsgCount(MessageAddress.getMessageAddress("src1"), MessageAddress.getMessageAddress("sink1"));
	    double byteCt = agentFlowSnapshot.getByteCount(MessageAddress.getMessageAddress("src1"), MessageAddress.getMessageAddress("sink1"));
	    if(logging.isDebugEnabled()) {
		logging.debug("msgCount & byteCount bewteen src1 -> sink1: "+ msgCt + ", "+ byteCt);
	    } 
	}
	*/

	if(logging.isDebugEnabled()) {
	    logging.debug(agentFlowSnapshot.toPrettyString());
	}
    }
    
    // Cougaar Plugin requirement methods
    @Override
   protected void setupSubscriptions() {
    }
    
    @Override
   protected void execute() {
    }
}
