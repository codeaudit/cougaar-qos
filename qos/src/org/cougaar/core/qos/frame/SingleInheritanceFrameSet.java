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
    private final Object change_queue_lock, relation_lock;
    private ArrayList change_queue;
    private HashMap kb; // UID -> object
    private HashMap cached_classes; // proto name -> Class
    private HashMap prototypes; // proto name -> PrototypeFrame
    private HashMap parent_cache, child_cache; // RelationFrame -> DataFrame
    private HashMap paths; // path name -> Path
    private HashSet frames; // all DataFrames

    // Containment hackery
    private HashSet pending_relations;
    private HashMap containers;
    private String container_relation;

    public SingleInheritanceFrameSet(String pkg,
				     ServiceBroker sb,
				     BlackboardService bbs,
				     String name,
				     String container_relation)
    {
	this.name = name;
	this.container_relation = container_relation;
	this.cached_classes = new HashMap();
	this.parent_cache = new HashMap();
	this.child_cache = new HashMap();
	this.paths = new HashMap();
	this.frames = new HashSet();
	this.pkg = pkg;
	this.bbs = bbs;
	this.change_queue = new ArrayList();
	this.change_queue_lock = new Object();
	this.relation_lock = new Object();
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);

 	this.kb = new HashMap();
	this.prototypes = new HashMap();

	this.pending_relations = new HashSet();
	this.containers = new HashMap();
    }


    // Object creation
    private void addObject(UniqueObject object)
    {
	if (log.isInfoEnabled())
	    log.info("Adding  " +object);
	synchronized (kb) {
	    kb.put(object.getUID(), object);
	}
	if (object instanceof DataFrame) {
	    synchronized (frames) {
		frames.add(object);
	    }
	    if (object instanceof RelationFrame) {
		cacheRelation((RelationFrame) object);
	    } else {
		// Any new DataFrame could potentially resolve as yet
		// unfilled values in relations.
		int resolved = 0;
		synchronized (pending_relations) {
		    Iterator itr = pending_relations.iterator();
		    while (itr.hasNext()) {
			RelationFrame frame = (RelationFrame) itr.next();
			boolean success = cacheRelation(frame);
			if (success) itr.remove();
		    }
		}
	    }
	}
    }

    public DataFrame makeFrame(String proto, Properties values)
    {
	UID uid = uids.nextUID();
	return makeFrame(proto, values, uid);
    }

    public DataFrame makeFrame(String proto, Properties values, UID uid)
    {
	DataFrame frame = DataFrame.newFrame(this, proto, uid, values);
	addObject(frame);
	publishAdd(frame);
	return frame;
    }

    public DataFrame makeFrame(DataFrame frame)
    {
	addObject(frame);
	publishAdd(frame);
	return frame;
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

    public Path makePath(String name, Path.Fork[] forks, String slot)
    {
	UID uid = uids.nextUID();
	Path path = new Path(uid, name, forks, slot);
	synchronized (paths) {
	    paths.put(name, path);
	}
	addObject(path);
	publishAdd(path);
	return path;
    }

    // Removing and modifying frames
    public void valueUpdated(DataFrame frame, String slot, Object value)
    {
	if (frame instanceof RelationFrame) {
	    RelationFrame rframe = (RelationFrame) frame;
	    synchronized (relation_lock) {
		if (slot.startsWith("child")) {
		    child_cache.remove(frame);
		} else if (slot.startsWith("parent")) {
		    parent_cache.remove(frame);
		}
	    }
	    cacheRelation(rframe);
	}
	    

	// Publish the frame itself as the change, or just a change
	// record for the specific slot?
	ArrayList changes = new ArrayList(1);
	Frame.Change change = new Frame.Change(frame.getUID(), slot, value);
	changes.add(change);
	publishChange(frame, changes);
    }


    public void removeFrame(DataFrame frame)
    {
	synchronized (kb) { kb.remove(frame.getUID()); }

	// Handle the removal of containment relationship frames
	if (frame instanceof RelationFrame) {
	    RelationFrame rframe = (RelationFrame) frame;
	    decacheRelation(rframe);
	}

	publishRemove(frame);
    }



    public String getName()
    {
	return name;
    }

    public String getPackageName()
    {
	return pkg;
    }

    public Collection getPrototypes()
    {
	synchronized (prototypes) {
	    return new ArrayList(prototypes.values());
	}
    }



    // Hierarchy

    public PrototypeFrame getPrototype(Frame frame)
    {
	String proto = frame.getKind();
	if (proto == null) return null;
	synchronized (prototypes) {
	    return (PrototypeFrame) prototypes.get(proto);
	}
    }

    // Old version, replaced by reflection
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
	Class klass = classForPrototype(prototype);
	return klass != null ? descendsFrom(frame, klass, prototype) : false;
    }

    boolean descendsFrom(DataFrame frame, Class klass, String prototype)
    {
	boolean result = klass.isInstance(frame);
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




    // Relationships

    private boolean isContainmentRelation(DataFrame frame)
    {
	return descendsFrom(frame, container_relation);
    }

    public DataFrame getContainer(DataFrame frame)
    {
	synchronized (containers) {
	    return (DataFrame) containers.get(frame);
	}
    }

    // Caller should synchronize on relation_lock
    private DataFrame getRelate(RelationFrame relationship,
				HashMap cache,
				String proto,
				String slot,
				Object value)
    {
	DataFrame result = (DataFrame) findFrame(proto, slot, value);
	if (result == null) {
	    if (log.isWarnEnabled())
		log.warn(" Proto = " +proto+
			 " Slot = " +slot+
			 " Value = " +value+
			 " matches nothing");
	} else {
	    if (log.isInfoEnabled())
		log.info(" Caching: Proto = " +proto+
			 " Slot = " +slot+
			 " Value = " +value+
			 " Result = " +result);
	    cache.put(relationship, result);
	}
	return result;
    }


    public DataFrame getRelationshipParent(RelationFrame relationship)
    {
	synchronized (relation_lock) {
	    DataFrame result = (DataFrame) parent_cache.get(relationship);
	    if (result != null) {
		if (log.isInfoEnabled())
		    log.info(" Found cached relation value " +result);
		return result;
	    }

	    String proto = (String) relationship.getParentPrototype();
	    String slot = (String) relationship.getParentSlot();
	    Object value = relationship.getParentValue();
	    return getRelate(relationship, parent_cache, proto, slot, value);
	}
    }

    public DataFrame getRelationshipChild(RelationFrame relationship)
    {
	synchronized (relation_lock) {
	    DataFrame result = (DataFrame) child_cache.get(relationship);
	    if (result != null) {
		if (log.isInfoEnabled())
		    log.info(" Found cached relation value " +result);
		return result;
	    }

	    String proto = (String) relationship.getChildPrototype();
	    String slot = (String) relationship.getChildSlot();
	    Object value = relationship.getChildValue();
	    return getRelate(relationship, child_cache, proto, slot, value);
	}
    }

    private boolean cacheRelation(RelationFrame relationship)
    {
	// cache a containment relationship
		    
	DataFrame parent = getRelationshipParent(relationship);
	DataFrame child = getRelationshipChild(relationship);

	    
	if (parent == null || child == null) {
	    // Queue for later
	    synchronized (pending_relations) {
		pending_relations.add(relationship);
	    }
	    return false;
	} else {
	    if (isContainmentRelation(relationship)) {
		synchronized (containers) {
		    DataFrame old = (DataFrame) containers.get(child);
		    child.containerChange(old, parent);
		    containers.put(child, parent);
		}
		if (log.isInfoEnabled())
		    log.info("Parent of " +child+ " is " +parent);
	    }
	    return true;
	}
    }

    private void decacheRelation(RelationFrame relationship)
    {
	synchronized (relation_lock) {
	    child_cache.remove(relationship);
	    parent_cache.remove(relationship);
	}
	synchronized (pending_relations) {
	    pending_relations.remove(relationship);
	}
	if (isContainmentRelation(relationship)) {
	    synchronized (containers) {
		Frame child = getRelationshipChild(relationship);
		if (child != null) containers.remove(child);
	    }
	}
    }








    // Query

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
	if (slot == null || proto == null || value == null) return null;

	    
	Class klass = classForPrototype(proto);
	if (klass == null) return null;

	synchronized (frames) {
	    Iterator itr = frames.iterator();
	    while (itr.hasNext()) {
		DataFrame frame = (DataFrame) itr.next();
		if (descendsFrom(frame, klass, proto)) {
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
	Class klass = classForPrototype(proto);
	if (klass == null) return null;

	HashSet results = new HashSet();
	synchronized (frames) {
	    Iterator itr = frames.iterator();
	    while (itr.hasNext()) {
		DataFrame frame = (DataFrame) itr.next();
		if (descendsFrom(frame, klass, proto) &&
		    frame.matchesSlots(slot_value_pairs))
		    results.add(frame);
	    }
	}
	return results;
      }

    Set findChildren(Frame parent, String relation_prototype)
    {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return null;
	HashSet results = new HashSet();
	synchronized (relation_lock) {
	    Iterator itr = parent_cache.entrySet().iterator();
	    while (itr.hasNext()) {
		Map.Entry entry = (Map.Entry) itr.next();
		if (entry.getValue().equals(parent)) {
		    RelationFrame relation = (RelationFrame) entry.getKey();
		    Object child = child_cache.get(relation);
		    if (child != null &&
			descendsFrom(relation, klass, relation_prototype))
			results.add(child);
		}
	    }
	}
	return results;
    }

    Set findParents(Frame child, String relation_prototype)
    {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return null;

	HashSet results = new HashSet();
	synchronized (relation_lock) {
	    Iterator itr = child_cache.entrySet().iterator();
	    while (itr.hasNext()) {
		Map.Entry entry = (Map.Entry) itr.next();
		if (entry.getValue().equals(child)) {
		    RelationFrame relation = (RelationFrame) entry.getKey();
		    Object parent = parent_cache.get(relation);
		    if (parent != null &&
			descendsFrom(relation, klass, relation_prototype))
			results.add(parent);
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
	synchronized (paths) {
	    return (Path) paths.get(name);
	}
    }




		
    // BBS queue

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

    // Old version
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

	indentation += offset;
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<frames>");
	indentation += offset;

	dumpDataFrames(writer, indentation, offset);

	indentation -= offset;
	writer.println("</frames>");


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
	writer.println("  container-relation=\"" +container_relation+ "\"");
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


}
