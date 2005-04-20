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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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


/**
 * Currently the only implementation of FrameSet, this class enforces
 * single inheritance in both the prototype hierarchy and the
 * containment hierarchy.
 */
public class SingleInheritanceFrameSet
    implements FrameSet
{
    private final String name;
    private final String pkg;
    private final LoggingService log;
    private final UIDService uids;
    private final BlackboardService bbs;
    private final Object change_queue_lock;
    private ArrayList change_queue;
    private HashMap kb;
    private HashMap cached_classes;
    private HashMap prototypes;

    // Containment hackery
    private HashSet pending_containment;
    private HashMap containers;
    private String 
	container_relation,
	parent_proto_slot,
	parent_slot_slot,
	parent_value_slot,
	child_proto_slot,
	child_slot_slot,
	child_value_slot;
    

    public SingleInheritanceFrameSet(String pkg,
				     ServiceBroker sb,
				     BlackboardService bbs,
				     String name,
				     String container_relation,
				     String parent_proto_slot,
				     String parent_slot_slot,
				     String parent_value_slot,
				     String child_proto_slot,
				     String child_slot_slot,
				     String child_value_slot)
    {
	this.name = name;
	this.cached_classes = new HashMap();
	this.pkg = pkg;
	this.bbs = bbs;
	this.change_queue = new ArrayList();
	this.change_queue_lock = new Object();
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);

 	this.kb = new HashMap();
	this.prototypes = new HashMap();

	// The kind tag of Frames representing the containment
	// relationship
	this.container_relation = container_relation;

	this.pending_containment = new HashSet();
	this.containers = new HashMap();

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
	if (object instanceof DataFrame) checkForPendingContainment(); // yuch
    }

    private void checkForPendingContainment()
    {
	synchronized (pending_containment) {
	    Iterator itr = pending_containment.iterator();
	    while (itr.hasNext()) {
		Frame frame = (Frame) itr.next();
		boolean success = establishContainment(frame);
		if (success) {
		    itr.remove();
		    return;
		}
	    }
	}
    }

    private DataFrame getRelate(Frame relationship,
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
	    DataFrame result = (DataFrame) findFrame(proto, slot, value);
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

    public DataFrame getRelationshipParent(DataFrame relationship)
    {
	return getRelate(relationship, 
			 parent_proto_slot,
			 parent_slot_slot,
			 parent_value_slot);
    }

    public DataFrame getRelationshipChild(DataFrame relationship)
    {
	return getRelate(relationship,
			 child_proto_slot,
			 child_slot_slot,
			 child_value_slot);
    }




    private boolean establishContainment(Frame relationship)
    {
	synchronized (containers) {
	    // cache a containment relationship
		    
	    DataFrame parent = getRelate(relationship,
					 parent_proto_slot, 
					 parent_slot_slot,
					 parent_value_slot);

	    DataFrame child = getRelate(relationship,
					child_proto_slot, 
					child_slot_slot,
					child_value_slot);

	    
	    if (parent == null || child == null) {
		// Queue for later
		synchronized (pending_containment) {
		    pending_containment.add(relationship);
		}
		return false;
	    } else {
		DataFrame old = (DataFrame) containers.get(child);
		child.containerChange(old, parent);
		containers.put(child, parent);
		if (log.isInfoEnabled())
		    log.info("Parent of " +child+ " is " +parent);
		return true;
	    }
	}
    }

    private void disestablishContainment(Frame relationship)
    {
	synchronized (containers) {
	    // decache a containment relationship
	    String child_proto = (String)
		relationship.getValue(child_proto_slot);
	    String child_slot = (String)
		relationship.getValue(child_slot_slot);
	    Object child_value = relationship.getValue(child_value_slot);
	    Frame child = findFrame(child_proto, child_slot, child_value);
	    if (child != null) containers.remove(child);
	}
	synchronized (pending_containment) {
	    pending_containment.remove(relationship);
	}
    }

    private boolean isContainmentRelation(DataFrame frame)
    {
	return descendsFrom(frame, container_relation);
    }



    public String getName()
    {
	return name;
    }



    // XML dumping
    void dumpDataFrames(PrintWriter writer, int indentation, int offset)
    {
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (raw instanceof DataFrame) {
		    DataFrame frame = (DataFrame) raw;
		    frame.dump(writer, indentation, offset);
		}
	    }
	}
    }

    void dumpData(File file, int indentation, int offset)
	throws java.io.IOException
    {
	FileWriter fwriter = new FileWriter(file);
	PrintWriter writer = new PrintWriter(fwriter);

	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<frameset>");
	indentation += offset;
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<frames>");
	indentation += offset;

	dumpDataFrames(writer, indentation, offset);

	indentation -= offset;
	writer.println("</frames>");
	indentation -= offset;
	writer.println("</frameset>");


	writer.close();
    }



    void dumpProtoFrames(PrintWriter writer, int indentation, int offset)
    {
	synchronized (prototypes) {
	    Iterator itr = prototypes.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (raw instanceof PrototypeFrame) {
		    PrototypeFrame frame = (PrototypeFrame) raw;
		    frame.dump(writer, indentation, offset);
		}
	    }
	}
    }

    void dumpPaths(PrintWriter writer, int indentation, int offset)
    {
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (raw instanceof Path) {
		    Path path = (Path) raw;
		    path.dump(writer, indentation, offset);
		}
	    }
	}
    }

    void dumpPrototypes(File file, int indentation, int offset)
	throws java.io.IOException
    {
	FileWriter fwriter = new FileWriter(file);
	PrintWriter writer = new PrintWriter(fwriter);

	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<frameset"); 
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  frame-inheritance=\"single\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  package=\"" +pkg+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  frame-inheritance-relation=\"" +container_relation+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  parent-prototype=\"" +parent_proto_slot+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  parent-slot=\"" +parent_slot_slot+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  parent-value=\"" +parent_value_slot+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  child-prototype=\"" +child_proto_slot+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  child-slot=\"" +child_slot_slot+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("  child-value=\"" +child_value_slot+ "\"");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println(">");


	indentation += offset;
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<prototypes>");
	indentation += offset;
	dumpPaths(writer, indentation, offset);
	dumpProtoFrames(writer, indentation, offset);
	indentation -= offset;
	writer.println("</prototypes>");
	indentation -= offset;
	writer.println("</frameset>");

	writer.close();
    }


    public  void dump(File proto_file, File data_file)
	throws java.io.IOException
    {
	dumpPrototypes(proto_file, 0, 2);
	dumpData(data_file, 0, 2);
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

    public DataFrame findFrame(String proto, String slot, Object value)
    {
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (!(raw instanceof DataFrame)) continue;
		DataFrame frame = (DataFrame) raw;
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



    public Set findFrames(String proto, Properties slot_value_pairs)
    {
	HashSet results = new HashSet();
	synchronized (kb) {
	    Iterator itr = kb.values().iterator();
	    while (itr.hasNext()) {
		Object raw = itr.next();
		if (!(raw instanceof DataFrame)) continue;
		DataFrame frame = (DataFrame) raw;
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

		DataFrame relationship = (DataFrame) raw;
		

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

		DataFrame relationship = (DataFrame) raw;


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


    public void valueUpdated(DataFrame frame, String slot, Object value)
    {
	// handle the modification of container relationship frames
	if (isContainmentRelation(frame))  establishContainment(frame);

	// Publish the frame itself as the change, or just a change
	// record for the specific slot?
	ArrayList changes = new ArrayList(1);
	Frame.Change change = new Frame.Change(frame.getUID(), slot, value);
	changes.add(change);
	publishChange(frame, changes);
    }

    public DataFrame makeFrame(String proto, Properties values)
    {
	UID uid = uids.nextUID();
	return makeFrame(proto, values, uid);
    }

    public DataFrame makeFrame(String proto, Properties values, UID uid)
    {
	DataFrame frame = DataFrame.newFrame(this, proto, uid, values);

	if (isContainmentRelation(frame)) establishContainment(frame);

	addObject(frame);
	publishAdd(frame);
	return frame;
    }

    public DataFrame makeFrame(DataFrame frame)
    {
	if (isContainmentRelation(frame)) establishContainment(frame);

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

    // Replaced by reflection
    public boolean descendsFromOld(Frame frame, String prototype)
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
	    proto_frame != null &&  descendsFromOld(proto_frame, prototype);
	return result;
    }

    private static final Object CNF = new Object();
    public Class classForPrototype(String prototype)
    {
	// cache these!
	synchronized (cached_classes) {
	    Object klass = cached_classes.get(prototype);
	    if (klass == CNF) return null;
	    if (klass != null) return (Class) klass;

	    
	    String classname = pkg +"."+ FrameGen.fixName(prototype, true);
	    try {
		Class klass2 =  Class.forName(classname);
		cached_classes.put(prototype, klass2);
		return klass2;
	    } catch (Exception ex) {
		if (log.isWarnEnabled())
		    log.warn("Couldn't find class for prototype " +prototype);
		cached_classes.put(prototype, CNF);
		return null;
	    }
	}
    }


    public boolean descendsFrom(DataFrame frame, String prototype)
    {
	boolean result;
	Class klass = classForPrototype(prototype);
	if (klass != null) {
	    result = klass.isInstance(frame);
	} else {
	    result = false;
	}
	if (log.isDebugEnabled())
	    log.debug(frame+ 
		      (result ? " descends from " : " does not descend from ") 
		      +prototype);
	return result;
    }

    public boolean descendsFrom(PrototypeFrame frame, String prototype)
    {
	boolean result;
	Class klass1 = classForPrototype(prototype);
	Class klass2 = classForPrototype(frame.getName());
	if (klass1 != null && klass2 != null)
	    result = klass1.isAssignableFrom(klass2);
	else
	    result = false;

	if (log.isDebugEnabled())
	    log.debug(frame+ 
		      (result ? " descends from " : " does not descend from ") 
		      +prototype);
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
		frame = new PrototypeFrame(this, proto, parent, uid, values);
		if (log.isDebugEnabled())
		    log.debug("Adding prototype " +frame+
			      " for " +proto);
		prototypes.put(proto, frame); 
	    }
	}
	addObject(frame);
	publishAdd(frame);
	return frame;
    }

    public Collection getPrototypes()
    {
	synchronized (prototypes) {
	    return new ArrayList(prototypes.values());
	}
    }

    public void removeFrame(DataFrame frame)
    {
	synchronized (kb) { kb.remove(frame.getUID()); }

	String name = (String) frame.getValue("name");
	synchronized (prototypes) { 
	    prototypes.remove(name); 
	}

	// Handle the removal of containment relationship frames
	if (isContainmentRelation(frame)) disestablishContainment(frame);

	publishRemove(frame);
    }

    public DataFrame getContainer(DataFrame frame)
    {
	synchronized (containers) {
	    return (DataFrame) containers.get(frame);
	}
    }

    public PrototypeFrame getPrototype(Frame frame)
    {
	String proto = frame.getKind();
	if (proto == null) return null;
	synchronized (prototypes) {
	    return (PrototypeFrame) prototypes.get(proto);
	}
    }


}
