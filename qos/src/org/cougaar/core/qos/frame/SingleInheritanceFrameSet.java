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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UID;


public class SingleInheritanceFrameSet
    implements FrameSet
{
    private final static String PROTOTYPE = "frame::prototype";

    private LoggingService log;
    private UIDService uids;
    private BlackboardService bbs;
    private HashSet pendingParentage;
    private HashMap kbs;
    private HashMap prototypes, parents;
    private String 
	parent_relation,
	parent_kind_slot,
	parent_slot_slot,
	parent_value_slot,
	child_kind_slot,
	child_slot_slot,
	child_value_slot;
    

    public SingleInheritanceFrameSet(ServiceBroker sb,
				     BlackboardService bbs,
				     String parent_relation,
				     String parent_kind_slot,
				     String parent_slot_slot,
				     String parent_value_slot,
				     String child_kind_slot,
				     String child_slot_slot,
				     String child_value_slot)
    {
	this.bbs = bbs;
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);

	this.pendingParentage = new HashSet();
	this.kbs = new HashMap();
	this.prototypes = new HashMap();
	this.parents = new HashMap();

	// The kind tag of Frames representing a parent-child relationship
	this.parent_relation = parent_relation;

	// Any given Frame of this kind will have three slots each,
	// for the parent and child respectively: a kind, a slot, and
	// value.  The names of these six slots in the relation Frame
	// are given here

	this.parent_kind_slot = parent_kind_slot;
	this.parent_slot_slot = parent_slot_slot;
	this.parent_value_slot = parent_value_slot;

	this.child_kind_slot = child_kind_slot;
	this.child_slot_slot = child_slot_slot;
	this.child_value_slot = child_value_slot;
    }

    private HashMap frameKB(Frame frame)
    {
	String kind = frame.getKind();
	return frameKB(kind);
    }

    private HashMap frameKB(String kind)
    {
	HashMap kb = null;
	synchronized (kbs) {
	    kb = (HashMap) kbs.get(kind);
	    if (kb == null) {
		kb = new HashMap();
		kbs.put(kind, kb);
	    }
	}
	return kb;
    }

    private void addFrame(Frame frame)
    {
	if (log.isInfoEnabled())
	    log.info("Adding frame " +frame);
	HashMap kb = frameKB(frame);
	synchronized (kb) {
	    kb.put(frame.getUID(), frame);
	}
	checkForPendingParentage(); // yuch
    }

    private void checkForPendingParentage()
    {
	synchronized (pendingParentage) {
	    Iterator itr = pendingParentage.iterator();
	    while (itr.hasNext()) {
		Frame frame = (Frame) itr.next();
		boolean success = establishParentage(frame);
		if (success) {
		    itr.remove();
		    return;
		}
	    }
	}
    }

    private boolean establishParentage(Frame relationship)
    {
	synchronized (parents) {
	    // cache a parent-child relationship
	    String parent_kind = (String)
		relationship.getValue(parent_kind_slot);
	    String parent_slot = (String)
		relationship.getValue(parent_slot_slot);
	    Object parent_value = relationship.getValue(parent_value_slot);

	    String child_kind = (String)
		relationship.getValue(child_kind_slot);
	    String child_slot = (String)
		relationship.getValue(child_slot_slot);
	    Object child_value = relationship.getValue(child_value_slot);

	    Frame parent = findFrame(parent_kind, parent_slot, parent_value);
	    Frame child = findFrame(child_kind, child_slot, child_value);
	    if (parent == null || child == null) {
		// Queue for later? 
		if (log.isInfoEnabled())
		    log.info("Parent or child is missing");
		synchronized (pendingParentage) {
		    pendingParentage.add(relationship);
		}
		return false;
	    } else {
		parents.put(child, parent);
		if (log.isInfoEnabled())
		    log.info("Parent of " +child+ " is " +parent);
		return true;
	    }

	}
    }

    private void disestablishParentage(Frame relationship)
    {
	synchronized (parents) {
	    // decache a parent-child relationship
	    String child_kind = (String)
		relationship.getValue(child_kind_slot);
	    String child_slot = (String)
		relationship.getValue(child_slot_slot);
	    Object child_value = relationship.getValue(child_value_slot);
	    Frame child = findFrame(child_kind, child_slot, child_value);
	    if (child != null) parents.remove(child);
	}
	synchronized (pendingParentage) {
	    pendingParentage.remove(relationship);
	}
    }


    public Frame findFrame(UID uid)
    {
	Frame frame = null;
	HashMap kb = null;
	synchronized (kbs) {
	    Iterator itr = kbs.values().iterator();
	    while (itr.hasNext()) {
		kb = (HashMap) itr.next();
		synchronized (kb) {
		    frame = (Frame) kb.get(uid);
		    if (frame != null) return frame;
		}
	    }
	}
	return null;
    }

    public Frame findFrame(String kind, String slot, Object value)
    {
	HashMap kb = frameKB(kind);
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Frame frame = (Frame) itr.next();
		// Check only local value [?]
		Object candidate = frame.getValue(slot);
		if (candidate != null && candidate.equals(value)) return frame;
	    }
	}
	return null;
    }

    public void valueUpdated(Frame frame, String attribute, Object value)
    {
	// handle the modification of parent-child relationship frames
	if (frame.getKind().equals(parent_relation)) establishParentage(frame);

	// Publish the frame itself as the change, or just a change
	// record for the specific attribute?
	ArrayList changes = new ArrayList(1);
	Frame.Change change = new Frame.Change(attribute, value);
	changes.add(change);
	if (bbs != null) bbs.publishChange(frame, changes);
    }

    public Frame makeFrame(String kind, Properties values)
    {
	UID uid = uids.nextUID();
	return makeFrame(kind, values, uid);
    }

    public Frame makeFrame(String kind, Properties values, UID uid)
    {
	Frame frame = new Frame(this, kind, uid, values);

	if (kind.equals(parent_relation)) establishParentage(frame);

	addFrame(frame);
	if (bbs != null) bbs.publishAdd(frame);
	return frame;
    }

    // In this case the kind argument refers to what the prototype
    // should be a prototype of.  The prototype Frame itself always
    // has a kind of PROTOTYPE.
    public Frame makePrototype(String kind, Properties values)
    {
	UID uid = uids.nextUID();
	Frame frame = new Frame(this, PROTOTYPE, uid, values);
	synchronized (prototypes) { prototypes.put(kind, frame); }
	addFrame(frame);
	if (bbs != null) bbs.publishAdd(frame);
	return frame;
    }

    public void removeFrame(Frame frame)
    {
	HashMap kb = frameKB(frame);
	synchronized (kb) {
	    kb.remove(frame.getUID());
	}
	if (frame.getKind().equals(PROTOTYPE)) {
	    String name = (String) frame.getValue("name");
	    synchronized (prototypes) { prototypes.remove(name); }
	}

	// Handle the removal of parent-child relationship frames
	if (frame.getKind().equals(parent_relation))
	    disestablishParentage(frame);

	if (bbs != null) bbs.publishRemove(frame);
    }

    public Frame getParent(Frame frame)
    {
	synchronized (parents) {
	    return (Frame) parents.get(frame);
	}
    }

    public Frame getPrototype(Frame frame)
    {
	synchronized (prototypes) {
	    return (Frame) prototypes.get(frame.getKind());
	}
    }


}
