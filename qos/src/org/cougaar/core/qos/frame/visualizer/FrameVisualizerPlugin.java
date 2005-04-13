/*
 * <copyright>
 *  
 *  Copyright 1997-2005 BBNT Solutions, LLC
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

package org.cougaar.core.qos.frame.visualizer;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.*;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;


public class FrameVisualizerPlugin
    extends ParameterizedPlugin
{
    private UnaryPredicate framePred = new UnaryPredicate() {
	    public boolean execute(Object o) {
	       return (o instanceof DataFrame) &&
		    ((DataFrame) o).getFrameSet().getName().equals(frameSetName);
	    }
	};
    private IncrementalSubscription sub;
    private LoggingService log;
    private String frameSetName;

    public void load()
    {
	super.load();

	ServiceBroker sb = getServiceBroker();

	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
    }

    public void start()
    {

	frameSetName = (String) getParameter("frame-set");
	super.start();
    }

    private void do_execute(BlackboardService bbs)
    {
	if (!sub.hasChanged()) {
	    if (log.isDebugEnabled())
		log.debug("No Frame changes");
	    return;
	}

	Enumeration en;
		
	// New Frames
	en = sub.getAddedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed added "+frame);
	    }
	    // Handle new Frame
	}
		
		
	// Changed Frames
	en = sub.getChangedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed changed "+frame);
	    }
	    Collection changes = sub.getChangeReports(frame);
	    // A collection of Frame.Change instances.
	    if (changes != null) {
		Iterator itr = changes.iterator();
		while (itr.hasNext()) {
		    Frame.Change change = (Frame.Change) itr.next();
		    // Handle change to existing frame
		}
	    }
	}
		
	// Remove Frames.  Won't happen.
	en = sub.getRemovedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {			
		log.debug("Observed removed "+frame);
	    }
	}
    }

    protected void execute()
    {
	BlackboardService bbs = getBlackboardService();
	do_execute(bbs);
    }

    protected void setupSubscriptions() 
    {
	BlackboardService bbs = getBlackboardService();
	if (log.isDebugEnabled())
	    log.debug("FrameSet name is " + frameSetName);

	sub = (IncrementalSubscription)
	    bbs.subscribe(framePred);
	
	if (!sub.getAddedCollection().isEmpty() && log.isDebugEnabled())
	    log.debug("Subscription has initial contents");
	do_execute(bbs);

    }
    


}
