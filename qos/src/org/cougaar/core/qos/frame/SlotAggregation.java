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
import org.cougaar.core.qos.frame.aggregator.SlotAggregator;
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
public class SlotAggregation {
    private final String slot;
    private final String relatedSlot;
    private final String relation;
    private final SlotAggregator aggregator;
    private final FrameSet frameset;
    private final String role;
    private final String otherRole;
    private IncrementalSubscription frameSub, relationshipSub;
    
    public SlotAggregation(FrameSet frameset, String slot, String relatedSlot, String relation,
	    String role, String className) 
    throws Exception {
	// Assume "parent" role for now
	this.role = role == null ? "parent" : role;
	this.otherRole = this.role.equals("parent") ? "child" : "parent";
	
	Class aggregatorClass = null;
	if (className.indexOf('.') <0) {
	    // No package, check the frameset's
	    String cname = frameset.getPackageName() +"."+ className;
	    try {
		aggregatorClass = Class.forName(cname);
	    } catch (ClassNotFoundException ex) {
		// Not in the frameset's package, try our package
		cname = getClass().getPackage().getName() +".aggregator."+ className;
		try {
		    aggregatorClass = Class.forName(cname);
		} catch (ClassNotFoundException ex2) {
		    // ignore
		}
	    }
	}
	if (aggregatorClass == null) {
	    // fully qualified name: allow this one to throw an Exception out of the call
	    aggregatorClass = Class.forName(className);
	}
	this.aggregator = (SlotAggregator) aggregatorClass.newInstance();
	this.slot = slot;
	this.relatedSlot = relatedSlot;
	this.relation = relation;
	this.frameset = frameset;
    }

    public String getRelatedSlot() {
        return relatedSlot;
    }

    public String getSlot() {
        return slot;
    }

    public String getRelation() {
        return relation;
    }
    
    public String getRole() {
	return role;
    }

    private void addToUpdateSet(Object x, Set<DataFrame> framesToUpdate) {
	DataFrame frame = (DataFrame) x;
	Set<DataFrame> parents = frame.findRelations(role, relation);
	if (parents != null) {
	    framesToUpdate.addAll(parents);
	}
    }

    /**
     * Collect the set of changed parent frames and invoke
     * the updater on each.
     */ 
    public void execute(BlackboardService bbs) {
	if (frameSub == null) {
	    setupSubscriptions(bbs);
	}
	Set<DataFrame> framesToUpdate = new HashSet<DataFrame>();
	if (frameSub.hasChanged()) {
	    Collection addedFrames = frameSub.getAddedCollection();
	    for (Object x : addedFrames) {
		addToUpdateSet(x, framesToUpdate);
	    }
	    
	    Collection removedFrames = frameSub.getRemovedCollection();
	    for (Object x : removedFrames) {
		addToUpdateSet(x, framesToUpdate);
	    }
	    
	    if (relatedSlot != null) {
		Collection changedFrames = frameSub.getChangedCollection();
		for (Object x : changedFrames) {
		    Set changeReports = frameSub.getChangeReports(x);
		    boolean relevant = false;
		    for (Frame.Change change : (Set<Frame.Change>) changeReports) {
			if (change.getSlotName().equals(relatedSlot)) {
			    relevant = true;
			    break;
			}
		    }
		    if (relevant) addToUpdateSet(x, framesToUpdate);
		}
	    }
	}
	
	if (relationshipSub.hasChanged()) {
	    Collection addedFrames = relationshipSub.getAddedCollection();
	    for (Object x : addedFrames) {
		RelationFrame rframe = (RelationFrame) x;
		if (role.equals("parent")) {
		    framesToUpdate.add(frameset.getRelationshipParent(rframe));
		} else {
		    framesToUpdate.add(frameset.getRelationshipChild(rframe)); 
		}
	    }
	    
	    Collection removedFrames = relationshipSub.getRemovedCollection();
	    for (Object x : removedFrames) {
		RelationFrame rframe = (RelationFrame) x;
		if (role.equals(FrameSet.PARENT)) {
		    framesToUpdate.add(frameset.getRelationshipParent(rframe));
		} else {
		    framesToUpdate.add(frameset.getRelationshipChild(rframe)); 
		}
	    } 
	}
	
	for (DataFrame frame : framesToUpdate) {
            if (frame == null) continue;
	    Set<DataFrame> children = frame.findRelations(otherRole, relation);
	    aggregator.updateSlotValue(frame, children, this);
	}
    }
    
    public void setupSubscriptions(BlackboardService bbs) {
	if (frameSub == null) {
	    frameSub = (IncrementalSubscription) bbs.subscribe(new FramePredicate());
	}
	if (relationshipSub == null) {
	    relationshipSub = (IncrementalSubscription) bbs.subscribe(new RelationPredicate());
	}
    }
    
    private class FramePredicate implements UnaryPredicate {
	/**
       * 
       */
      private static final long serialVersionUID = 1L;
   private final String attribute = otherRole + "-prototype";
	private Class relatedFrameClass;
	
	public boolean execute(Object o) {
	    if (!(o instanceof DataFrame)) return false;
	    DataFrame frame = (DataFrame) o;
	    if (frameset != frame.getFrameSet()) return false;
	    
	    if (relatedFrameClass == null) {
		PrototypeFrame relationProto = frameset.findPrototypeFrame(relation);
		String type = relationProto.getAttribute(attribute);
		relatedFrameClass = frameset.classForPrototype(type);
	    }
	    Class frameClass = frame.getClass();
	    return relatedFrameClass.isAssignableFrom(frameClass);
	}
    }
    
    private class RelationPredicate implements UnaryPredicate {
	/**
       * 
       */
      private static final long serialVersionUID = 1L;
   private Class relationshipClass;
	
	public boolean execute(Object o) {
	    if (!(o instanceof DataFrame)) return false;
	    DataFrame frame = (DataFrame) o;
	    if (frameset != frame.getFrameSet()) return false;
	    
	    if (relationshipClass == null) {
		PrototypeFrame relationProto = frameset.findPrototypeFrame(relation);
		relationshipClass = frameset.classForPrototype(relationProto);
	    }
	    Class frameClass = frame.getClass();
	    return relationshipClass.isAssignableFrom(frameClass);
	}
    }
}
