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
 * If a child frame is added or removed, redo all relevant calculations on
 * related parents. Similary if childSlot was given and that slot was changed in
 * a child frame.
 * 
 * For now ignore the relation frames, though if they're changing that will have
 * to be taken into account.
 * 
 */
public class SlotDependency {
    private final String parentSlot;
    private final String childSlot;
    private final String relation;
    private final SlotUpdater updater;
    private final FrameSet frameset;
    private final Class childClass;
    private IncrementalSubscription sub;
    
    public SlotDependency(FrameSet frameset, String slot, 
	    	String childProto, String childSlot, 
	    	String relation,
	    	SlotUpdater updater) {
	this.updater = updater;
	this.parentSlot = slot;
	this.childSlot = childSlot;
	this.relation = relation;
	this.frameset = frameset;
	this.childClass = frameset.classForPrototype(childProto); 
    }
    
    private void addToUpdateSet(Object x, Set<DataFrame> framesToUpdate) {
	DataFrame frame = (DataFrame) x;
	Set<DataFrame> parents = frame.findRelations("parent", relation);
	if (parents != null) {
	    framesToUpdate.addAll(parents);
	}
    }

    /**
     * Collect the set of changed parent frames and invoke
     * the updater on each.
     */ 
    public void execute(BlackboardService bbs) {
	if (sub == null) {
	    setupSubscriptions(bbs);
	}
	Set<DataFrame> framesToUpdate = new HashSet<DataFrame>();
	if (sub.hasChanged()) {
	    Collection addedFrames = sub.getAddedCollection();
	    for (Object x : addedFrames) {
		addToUpdateSet(x, framesToUpdate);
	    }

	    Collection removedFrames = sub.getRemovedCollection();
	    for (Object x : removedFrames) {
		addToUpdateSet(x, framesToUpdate);
	    }

	    if (childSlot != null) {
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
		    if (relevant) addToUpdateSet(x, framesToUpdate);
		}
	    }
	}
	for (DataFrame frame : framesToUpdate) {
	    Set<DataFrame> children = frame.findRelations("child", relation);
	    updater.updateSlotValue(frame, parentSlot, children);
	}
    }
    
    public void setupSubscriptions(BlackboardService bbs) {
	sub = (IncrementalSubscription) bbs.subscribe(new FramePredicate());
    }
    
    private class FramePredicate implements UnaryPredicate {
	public boolean execute(Object o) {
	    if (!(o instanceof DataFrame)) return false;
	    DataFrame frame = (DataFrame) o;
	    return frameset == frame.getFrameSet() && childClass.isAssignableFrom(frame.getClass());
	}
    }
}
