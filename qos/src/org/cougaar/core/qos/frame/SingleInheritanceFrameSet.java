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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;


public class SingleInheritanceFrameSet
    implements FrameSet
{
    private LoggingService log;
    private UIDService uids;
    private BlackboardService bbs;
    private ArrayList change_queue;
    private Object change_queue_lock;
    private String name;
    private HashSet pending_parentage;
    private HashMap kb;
    private HashMap prototypes, parents;
    private HashSet parent_relations;
    private String pkg;
    private String 
	parent_relation,
	parent_proto_slot,
	parent_slot_slot,
	parent_value_slot,
	child_proto_slot,
	child_slot_slot,
	child_value_slot;
    

    public SingleInheritanceFrameSet(String pkg_prefix,
				     ServiceBroker sb,
				     BlackboardService bbs,
				     String name,
				     String parent_relation,
				     String parent_proto_slot,
				     String parent_slot_slot,
				     String parent_value_slot,
				     String child_proto_slot,
				     String child_slot_slot,
				     String child_value_slot)
    {
	this.name = name;
	this.pkg = pkg_prefix +"."+ name;
	this.bbs = bbs;
	this.change_queue = new ArrayList();
	this.change_queue_lock = new Object();
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);

	this.pending_parentage = new HashSet();
 	this.kb = new HashMap();
	this.prototypes = new HashMap();
	this.parents = new HashMap();

	// The kind tag of Frames representing a parent-child relationship
	this.parent_relation = parent_relation;
	this.parent_relations = new HashSet();
	this.parent_relations.add(parent_relation);

	// Any given Frame of this kind will have three slots each,
	// for the parent and child respectively: a proto, a slot, and
	// value.  The names of these six slots in the relation Frame
	// are given here

	this.parent_proto_slot = parent_proto_slot;
	this.parent_slot_slot = parent_slot_slot;
	this.parent_value_slot = parent_value_slot;

	this.child_proto_slot = child_proto_slot;
	this.child_slot_slot = child_slot_slot;
	this.child_value_slot = child_value_slot;
    }


    private void addObject(UniqueObject object)
    {
	if (log.isInfoEnabled())
	    log.info("Adding  " +object);
	synchronized (kb) {
	    kb.put(object.getUID(), object);
	}
	if (object instanceof DataFrame) checkForPendingParentage(); // yuch
    }

    private void checkForPendingParentage()
    {
	synchronized (pending_parentage) {
	    Iterator itr = pending_parentage.iterator();
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

    private Frame getRelate(Frame relationship,
			    String proto_slot,
			    String slot_slot,
			    String value_slot)
    {
	String proto = (String)
	    relationship.getValue(proto_slot);
	String slot = (String)
	    relationship.getValue(slot_slot);
	Object value = relationship.getValue(value_slot);

	if (slot == null || proto == null || value == null) {
	    if (log.isWarnEnabled()) {
		if (slot == null) {
		    log.warn("Relationship " +relationship+
			     " has no value for " +slot_slot);
		}
		if (proto == null) {
		    log.warn("Relationship " +relationship+
			     " has no value for " +proto_slot);
		}
		if (value == null) {
		    log.warn("Relationship " +relationship+
			     " has no value for " +value_slot);
		}
	    }
	    
	    return null;
	} else {
	    Frame result = findFrame(proto, slot, value);
	    if (result == null && log.isWarnEnabled())
		if (result == null)
		    log.warn(" Proto = " +proto+
			     " Slot = " +slot+
			     " Value = " +value+
			     " matches nothing");
	    if (result != null && log.isInfoEnabled())
		log.info(" Proto = " +proto+
			 " Slot = " +slot+
			 " Value = " +value+
			 " Result = " +result);
		    
	    return result;
	}
    }


    public String getPackageName()
    {
	return pkg;
    }

    public Frame getRelationshipParent(Frame relationship)
    {
	return getRelate(relationship, 
			 parent_proto_slot,
			 parent_slot_slot,
			 parent_value_slot);
    }

    public Frame getRelationshipChild(Frame relationship)
    {
	return getRelate(relationship,
			 child_proto_slot,
			 child_slot_slot,
			 child_value_slot);
    }




    private boolean establishParentage(Frame relationship)
    {
	synchronized (parents) {
	    // cache a parent-child relationship
		    
	    Frame parent = getRelate(relationship,
				     parent_proto_slot, 
				     parent_slot_slot,
				     parent_value_slot);

	    Frame child = getRelate(relationship,
				    child_proto_slot, 
				    child_slot_slot,
				    child_value_slot);

	    
	    if (parent == null || child == null) {
		// Queue for later
		synchronized (pending_parentage) {
		    pending_parentage.add(relationship);
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
	    String child_proto = (String)
		relationship.getValue(child_proto_slot);
	    String child_slot = (String)
		relationship.getValue(child_slot_slot);
	    Object child_value = relationship.getValue(child_value_slot);
	    Frame child = findFrame(child_proto, child_slot, child_value);
	    if (child != null) parents.remove(child);
	}
	synchronized (pending_parentage) {
	    pending_parentage.remove(relationship);
	}
    }

    private boolean isParentageRelation(Frame frame)
    {
	String proto = frame.getKind();
	synchronized (parent_relations) {
	    return parent_relations.contains(proto);
	}
    }



    public String getName()
    {
	return name;
    }


    public Path findPath(UID uid)
    {
	synchronized (kb) {
	    Object raw = kb.get(uid);
	    if (raw instanceof Path)
		return (Path) raw;
	    else
		return null;
	}
    }

    public Path findPath(String name)
    {
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (!(raw instanceof Path)) continue;
		Path path = (Path) raw;
		if (path.getName().equals(name)) return path;
	    }
	}
	return null;
    }


		
    public Frame findFrame(UID uid)
    {
	synchronized (kb) {
	    Object raw = kb.get(uid);
	    if (raw instanceof Frame)
		return (Frame) raw;
	    else
		return null;
	}
    }

    public Frame findFrame(String proto, String slot, Object value)
    {
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (!(raw instanceof DataFrame)) continue;
		Frame frame = (Frame) raw;
		// Check only local value [?]
		if (descendsFrom(frame, proto)) {
		    Object candidate = frame.getValue(slot);
		    if (candidate != null && candidate.equals(value)) 
			return frame;
		}
	    }
	}
	return null;
    }



    public Set findFrames(String proto, 
			  Properties slot_value_pairs,
			  boolean includePrototypes)
    {
	HashSet results = new HashSet();
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (!(raw instanceof Frame)) continue;
		if (!includePrototypes && raw instanceof PrototypeFrame)
		    continue;
		Frame frame = (Frame) raw;
		// Check only local value [?]
		if (descendsFrom(frame, proto) &&
		    frame.matchesSlots(slot_value_pairs))
		    results.add(frame);
	    }
	}
	return results;
      }

    Set findChildren(Frame parent, String relation_prototype)
    {
	HashSet results = new HashSet();
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (!(raw instanceof DataFrame)) continue;

		Frame relationship = (Frame) raw;
		

		if (descendsFrom(relationship, relation_prototype)) {
		    Frame p = getRelate(relationship,
					parent_proto_slot, 
					parent_slot_slot,
					parent_value_slot);
		    if ( p != null && p.equals(parent)) {
			Frame child = getRelate(relationship,
						child_proto_slot, 
						child_slot_slot,
						child_value_slot);
			if (child != null) results.add(child);
		    }		    
		}
	    }
	}
	return results;
    }

    Set findParents(Frame child, String relation_prototype)
    {
	HashSet results = new HashSet();
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (!(raw instanceof DataFrame)) continue;

		Frame relationship = (Frame) raw;


		if (descendsFrom(relationship, relation_prototype)) {
		    Frame c = getRelate(relationship,
					child_proto_slot, 
					child_slot_slot,
					child_value_slot);
		    if (log.isDebugEnabled())
			log.debug("Candidate = " +c+
				  " child = " +child);

		    if ( c != null && c.equals(child)) {
			Frame parent = getRelate(relationship,
						parent_proto_slot, 
						parent_slot_slot,
						parent_value_slot);
			if (log.isDebugEnabled())
			    log.debug("Adding parent " + parent);
			if (parent != null) results.add(parent);
		    }		    
		}
	    }
	}
	return results;
    }

    public Set findRelations(Frame root, String role, String relation)
    {
	if (role.equals("parent")) {
	    return findParents(root, relation);
	} else if (role.equals("child")) {
	    return findChildren(root, relation);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Role " +role+ " should be \"parent\" or \"child\"");
	    return null;
	}
				
    }

    private static class Add {
	UniqueObject object;
	Add(UniqueObject object)
	{
	    this.object = object;
	}
    }

    private static class Change {
	UniqueObject object;
	Collection changes;
	Change(UniqueObject object, Collection changes)
	{
	    this.object = object;
	    this.changes = changes;
	}
    }

    private static class Remove {
	UniqueObject object;
	Remove(UniqueObject object)
	{
	    this.object = object;
	}
    }


    // Synchronized for a shorter time but doesn't work reliably.
    // Sometimes items are added while this is in progress and
    // execute doesn't run again.
    public void processQueue()
    {
	ArrayList changes = null;
	synchronized (change_queue_lock) {
	    changes = new ArrayList(change_queue);
	    change_queue = new ArrayList();
	}
	int count = changes.size();
	for (int i=0; i<count; i++) {
	    Object change = changes.get(i);
	    if (log.isDebugEnabled())
		log.debug("about to publish " + change);
	    if (change instanceof Change) {
		Change chng = (Change) change;
		bbs.publishChange(chng.object, chng.changes);
	    } else  if (change instanceof Add) {
		Add add = (Add) change;
		bbs.publishAdd(add.object);
	    } else  if (change instanceof Remove) {
		Remove rem = (Remove) change;
		bbs.publishRemove(rem.object);
	    }
	}
    }

    public void processQueueSlow()
    {
	synchronized (change_queue_lock) {
	    int count = change_queue.size();
	    for (int i=0; i<count; i++) {
		Object change = change_queue.get(i);
		if (change instanceof Change) {
		    Change chng = (Change) change;
		    bbs.publishChange(chng.object, chng.changes);
		} else  if (change instanceof Add) {
		    Add add = (Add) change;
		    bbs.publishAdd(add.object);
		} else  if (change instanceof Remove) {
		    Remove rem = (Remove) change;
		    bbs.publishRemove(rem.object);
		}
	    }
	    change_queue.clear();
	}
    }
	
    void publishAdd(UniqueObject object)
    {
	synchronized (change_queue_lock) {
	    change_queue.add(new Add(object));
	    bbs.signalClientActivity();
	}
    }

    void publishChange(UniqueObject object, ArrayList changes)
    {
	synchronized (change_queue_lock) {
	    change_queue.add(new Change(object, changes));
	    bbs.signalClientActivity();
	}
    }

    void publishRemove(UniqueObject object)
    {
	synchronized (change_queue_lock) {
	    change_queue.add(new Remove(object));
	    bbs.signalClientActivity();
	}
    }


    public void valueUpdated(Frame frame, String slot, Object value)
    {
	// handle the modification of parent-child relationship frames
	if (isParentageRelation(frame))  establishParentage(frame);

	// Publish the frame itself as the change, or just a change
	// record for the specific slot?
	ArrayList changes = new ArrayList(1);
	Frame.Change change = new Frame.Change(frame.getUID(), slot, value);
	changes.add(change);
	publishChange(frame, changes);
    }

    public Frame makeFrame(String proto, Properties values)
    {
	UID uid = uids.nextUID();
	return makeFrame(proto, values, uid);
    }

    public Frame makeFrame(String proto, Properties values, UID uid)
    {
	Frame frame = DataFrame.newFrame(this, proto, uid, values);

	if (isParentageRelation(frame)) establishParentage(frame);

	addObject(frame);
	publishAdd(frame);
	return frame;
    }

    public Frame makeFrame(Frame frame)
    {
	if (isParentageRelation(frame)) establishParentage(frame);

	addObject(frame);
	publishAdd(frame);
	return frame;
    }


    public Path makePath(String name, Path.Fork[] forks, String slot)
    {
	UID uid = uids.nextUID();
	Path path = new Path(uid, name, forks, slot);
	addObject(path);
	publishAdd(path);
	return path;
    }

    public boolean descendsFrom(Frame frame, String prototype)
    {
	String proto = frame.getKind();
	if (proto == null) return false;
	if (proto.equals(prototype)) {
	    return true;
	}
	Frame proto_frame = null;
	synchronized (prototypes) {
	    proto_frame = (Frame) prototypes.get(proto);
	}
	boolean result =
	    proto_frame != null &&  descendsFrom(proto_frame, prototype);
	return result;
    }

    // In this case the proto argument refers to what the prototype
    // should be a prototype of.  
    public PrototypeFrame makePrototype(String proto, 
					String parent, 
					Properties values)
    {
	UID uid = uids.nextUID();
	return makePrototype(proto, parent, values, uid);
    }

    public PrototypeFrame makePrototype(String proto, 
					String parent, 
					Properties values,
					UID uid)
    {
	PrototypeFrame frame = null;
	synchronized (prototypes) { 
	    
	    if (prototypes.containsKey(proto)) {
		if (log.isWarnEnabled())
		    log.warn("Ignoring prototype " +proto);
		return null;
	    } else {
		frame = new PrototypeFrame(this, proto, parent, uid);
		if (log.isDebugEnabled())
		    log.debug("Adding prototype " +frame+
			      " for " +proto);
		prototypes.put(proto, frame); 
	    }
	}
	synchronized (parent_relations) { 
	    if (descendsFrom(frame, parent_relation))
		parent_relations.add(proto);
	}
	addObject(frame);
	publishAdd(frame);
	return frame;
    }

    public Set getPrototypes()
    {
	synchronized (prototypes) {
	    return new HashSet(prototypes.keySet());
	}
    }

    public void removeFrame(Frame frame)
    {
	synchronized (kb) { kb.remove(frame.getUID()); }

	String name = (String) frame.getValue("name");
	synchronized (prototypes) { 
	    prototypes.remove(name); 
	}

	// Handle the removal of parent-child relationship frames
	if (isParentageRelation(frame)) disestablishParentage(frame);

	publishRemove(frame);
    }

    public Frame getParent(Frame frame)
    {
	synchronized (parents) {
	    return (Frame) parents.get(frame);
	}
    }

    public Frame getPrototype(Frame frame)
    {
	String proto = frame.getKind();
	if (proto == null) return null;
	synchronized (prototypes) {
	    return (Frame) prototypes.get(proto);
	}
    }


}
