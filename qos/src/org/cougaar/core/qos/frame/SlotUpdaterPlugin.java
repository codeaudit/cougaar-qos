/*
 * <copyright>
 *  
 *  Copyright 1997-2006 BBNT Solutions, LLC
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

package org.cougaar.core.qos.frame;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;

/**
 * This class implements a quasi-generalizaion of complex frame relationship. An
 * example is as follows: the 'site' prototype has a slot called
 * 'percentageHostsUp', the value of which is found by computing the percentage
 * of hosts at that site whose status is "up". The percentageHostsUp slot value
 * must update automatically whenever the status of any host at that site
 * changes. Probably it should also update automatically when hosts move in to
 * or out of the site, but we're ignoring that case for now.
 * 
 * The generalization would have to involve adding dependencies on some subset
 * of entities of another kind of entity (eg sites need to know about changes to
 * hosts) as well as on some relationship between the two kinds (eg hostAtSite).
 * On any dependent change, the relationship is then used to construct a
 * collection of frames of the other kind (eg a collection of hosts). Finally,
 * some piece of Java code is run on that collection.
 * 
 * Ideally this would be implemented by the FrameSet. But it's not clear to me
 * that we can find the right place to run in that context. Instead I'm trying
 * it in a plugin which for now implements one dependency, as specified by
 * parameters. It could just as easily implement a set of dependencies defined
 * in xml.
 * 
 * 
 */
public class SlotUpdaterPlugin extends ParameterizedPlugin implements FrameSetService.Callback {
    private String relation;
    private String childSlot;
    private Class childClass;
    private SlotUpdater updater;
    private LoggingService log;
    private FrameSet frameset;
    private IncrementalSubscription sub;

    public void start() {
	ServiceBroker sb = getServiceBroker();
	log = (LoggingService) sb.getService(this, LoggingService.class, null);
	
	this.relation = getParameter("relation");
	this.childSlot = getParameter("child-slot");
	String childClassName = getParameter("child-class");
	try {
	    childClass = Class.forName(childClassName);
	} catch (ClassNotFoundException e) {
	    log.error("Class not found: " + childClassName);
	    return;
	}
	String updaterName = getParameter("updater");
	try {
	    Class updateClass = Class.forName(updaterName);
	    updater = (SlotUpdater) updateClass.newInstance();
	} catch (ClassNotFoundException e) {
	    log.error("Class not found: " + childClassName);
	    return;
	} catch (InstantiationException e) {
	    log.error("Couldn't instantiate " + childClassName);
	    return;
	} catch (IllegalAccessException e) {
	    log.error("Couldn't instantiate " + childClassName);
	    return;
	}
	
	String frameSetName = getParameter("frame-set");
	
	FrameSetService fss = (FrameSetService) 
	sb.getService(this, FrameSetService.class, null);
	if (fss != null) {
	    frameset = fss.findFrameSet(frameSetName, this);
	} else {
	    log.error("Couldn't find FrameSetService");
	}
	// Defer initial execute
	super.start();

    }

    // Generic piece: decide which frames to update
    protected void execute() {
	Set<DataFrame> framesToUpdate = null;
	// For now we're only looking at changes to hosts
	if (sub.hasChanged()) {
	    Collection changedFrames = sub.getChangedCollection();
	    for (Object x : changedFrames) {
		Set changeReports = sub.getChangeReports(x);
		boolean relevant = false;
		for (Frame.Change change : (Set<Frame.Change>) changeReports) {
		    if (change.getSlotName().equals(childSlot)) {
			relevant = true;
			break;
		    }
		}
		if (!relevant) continue;
		
		DataFrame frame = (DataFrame) x;
		Set<DataFrame> parents = frame.findRelations("parent", relation);
		if (parents != null) {
		    if (framesToUpdate == null) framesToUpdate = new HashSet<DataFrame>();
		    framesToUpdate.addAll(parents);
		}
	    }
	}
	if (framesToUpdate != null) {
	    for (DataFrame frame : framesToUpdate) {
		Set<DataFrame> children = frame.findRelations("child", relation);
		updater.updateSlotValue(frame, children);
	    }
	}
    }

    protected void setupSubscriptions() {
	BlackboardService bbs = getBlackboardService();
	sub = (IncrementalSubscription) bbs.subscribe(new FramePredicate());
    }

    public void frameSetAvailable(String name, FrameSet set) {
	frameset = set;
    }


    private class FramePredicate implements UnaryPredicate {
	public boolean execute(Object o) {
	    // Boilerplate
	    if (frameset == null || !(o instanceof DataFrame)) return false;
	    DataFrame frame = (DataFrame) o;
	    if (frameset != frame.getFrameSet()) return false;
	    
	    // For now we only care about one kind of frame
	    if (childClass.isAssignableFrom(frame.getClass())) return true;
	    
	    return false;
	}
    }
    

}
