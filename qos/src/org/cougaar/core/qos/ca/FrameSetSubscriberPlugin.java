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

package org.cougaar.core.qos.ca;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;

/**
 * Simple tester for FrameSets
 */
public class FrameSetSubscriberPlugin
    extends ParameterizedPlugin
{
    private UnaryPredicate framePred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		return (o instanceof Frame);
	    }
	};
    private IncrementalSubscription sub;
    private LoggingService log;

    public void load()
    {
	super.load();

	ServiceBroker sb = getServiceBroker();

	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
    }



    // plugin
    protected void execute()
    {
	if (sub == null || !sub.hasChanged()) {
	    if (log.isDebugEnabled())
		log.debug("No Frame changes");
	    return;
	}

	java.util.Enumeration en;
		
	// observe added relays
	en = sub.getAddedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed added b"+frame);
	    }		    
	}
		
		
	// observe changed relays
	en = sub.getChangedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed changed "+frame);
	    }
	}
		
	// removed relays
	en = sub.getRemovedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {			
		log.debug("Observed removed "+frame);
	    }
	}
    }

    

    protected void setupSubscriptions() 
    {
	BlackboardService bbs = getBlackboardService();
	sub = (IncrementalSubscription)
	    blackboard.subscribe(framePred);
    }



}

