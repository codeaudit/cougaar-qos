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
package org.cougaar.core.qos.frame;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.util.UnaryPredicate;

/**
 * @author rshapiro
 *
 */
public class SlotDependency {
    private final String childProto;
    private final String childSlot;
    private final String relation;
    private final SlotUpdater updater;
    private FrameSet frameset;
    private Class childClass;
    private IncrementalSubscription sub;
    
    public SlotDependency(FrameSet frameset, 
	    	String childProto, String childSlot, 
	    	String relation,
	    	SlotUpdater updater) {
	this.updater = updater;
	this.childProto = childProto;
	this.childSlot = childSlot;
	this.relation = relation;
	setFrameSet(frameset);
    }
    
    public void setFrameSet(FrameSet frameset) {
	this.frameset = frameset;
	if (frameset != null) {
	    this.childClass = frameset.classForPrototype(childProto); 
	}
    }
    
    public void execute() {
	if (childClass == null) {
	    // too early
	    return;
	}
	Set<DataFrame> framesToUpdate = null;
	// For now we're only looking at changes
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
    
    public void setupSubscriptions(BlackboardService bbs) {
	sub = (IncrementalSubscription) bbs.subscribe(new FramePredicate());
    }
    
    private class FramePredicate implements UnaryPredicate {
	public boolean execute(Object o) {
	    // Boilerplate
	    if (childClass == null) return false;
	    if (frameset == null || !(o instanceof DataFrame)) return false;
	    DataFrame frame = (DataFrame) o;
	    if (frameset != frame.getFrameSet()) return false;
	    
	    // For now we only care about one kind of frame
	    if (childClass.isAssignableFrom(frame.getClass())) return true;
	    
	    return false;
	}
    }
}
