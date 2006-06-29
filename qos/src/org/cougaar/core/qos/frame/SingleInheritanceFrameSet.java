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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsService;
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
    implements FrameSet {
    private static final long LOOKUP_WARN_TIME = 10000;

    private final String name;
    private final String pkg;
    private final LoggingService log;
    private final UIDService uids;
    private final BlackboardService bbs;
    private final MetricsService metrics;
    private final Object change_queue_lock, relation_lock;
    private List<ChangeQueueEntry> change_queue;
    private Map<UID,Object> kb; // values can be either Frames or Paths
    private Map<String,Class> cached_classes;
    private Map<String,PrototypeFrame> prototypes;
    private Map<RelationFrame,DataFrame> parent_cache, child_cache;
    private Map<String,Path> paths;
    private Set<DataFrame> frames;

    // Containment hackery
    private Set<RelationFrame> pending_relations;
    private Map<DataFrame,DataFrame> containers;
    private String container_relation;

    public SingleInheritanceFrameSet(String pkg,
				     ServiceBroker sb,
				     BlackboardService bbs,
				     String name,
				     String container_relation) {
	this.name = name;
	this.container_relation = container_relation;
	this.cached_classes = new HashMap<String,Class>();
	this.parent_cache = new HashMap<RelationFrame,DataFrame>();
	this.child_cache = new HashMap<RelationFrame,DataFrame>();
	this.paths = new HashMap<String,Path>();
	this.frames = new HashSet<DataFrame>();
	this.pkg = pkg;
	this.bbs = bbs;
	this.change_queue = new ArrayList();
	this.change_queue_lock = new Object();
	this.relation_lock = new Object();
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);
	metrics = (MetricsService)
	    sb.getService(this, MetricsService.class, null);

 	this.kb = new HashMap<UID,Object>();
	this.prototypes = new HashMap<String,PrototypeFrame>();

	this.pending_relations = new HashSet<RelationFrame>();
	this.containers = new HashMap<DataFrame,DataFrame>();
    }


    // Object creation
    private void addObject(UniqueObject object) {
	if (log.isInfoEnabled())
	    log.info("Adding  " +object);
	synchronized (kb) {
	    kb.put(object.getUID(), object);
	}
	if (object instanceof DataFrame) {
	    synchronized (frames) {
		frames.add((DataFrame) object);
	    }
	    if (object instanceof RelationFrame) {
		cacheRelation((RelationFrame) object);
	    } else {
		// Any new DataFrame could potentially resolve as yet
		// unfilled values in relations.
		synchronized (pending_relations) {
		    Iterator<RelationFrame> itr = pending_relations.iterator();
		    while (itr.hasNext()) {
			boolean success = cacheRelation(itr.next());
			if (success) itr.remove();
		    }
		}
	    }
	}
    }

    public DataFrame makeFrame(String proto, Properties values) {
	UID uid = uids.nextUID();
	return makeFrame(proto, values, uid);
    }

    public DataFrame makeFrame(String proto, Properties values, UID uid) {
	DataFrame frame = DataFrame.newFrame(this, proto, uid, values);
	addObject(frame);
	publishAdd(frame);
	return frame;
    }

    public DataFrame makeFrame(DataFrame frame) {
	addObject(frame);
	publishAdd(frame);
	return frame;
    }

    // In this case the proto argument refers to what the prototype
    // should be a prototype of.  
    public PrototypeFrame makePrototype(String proto, 
					String parent, 
					Properties values) {
	UID uid = uids.nextUID();
	return makePrototype(proto, parent, values, uid);
    }

    public PrototypeFrame makePrototype(String proto, 
					String parent, 
					Properties values,
					UID uid) {
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

    public Path makePath(String name, Path.Fork[] forks, String slot) {
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
    public void valueUpdated(DataFrame frame, String slot, Object value) {
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
	Frame.Change change = new Frame.Change(frame.getUID(), slot, value);
	publishChange(frame, change);
    }


    public void removeFrame(DataFrame frame) {
	synchronized (kb) { 
	    kb.remove(frame.getUID());
	}

	// Handle the removal of containment relationship frames
	if (frame instanceof RelationFrame) {
	    RelationFrame rframe = (RelationFrame) frame;
	    decacheRelation(rframe);
	}

	publishRemove(frame);
    }



    public String getName() {
	return name;
    }

    public String getPackageName() {
	return pkg;
    }

    public Collection<PrototypeFrame> getPrototypes() {
	synchronized (prototypes) {
	    return Collections.unmodifiableCollection(prototypes.values());
	}
    }



    // Hierarchy

    public PrototypeFrame getPrototype(Frame frame) {
	String proto = frame.getKind();
	if (proto == null) return null;
	synchronized (prototypes) {
	    return prototypes.get(proto);
	}
    }

    // Old version, replaced by reflection
    public boolean descendsFromOld(Frame frame, String prototype) {
	String proto = frame.getKind();
	if (proto == null) return false;
	if (proto.equals(prototype)) {
	    return true;
	}
	Frame proto_frame = null;
	synchronized (prototypes) {
	    proto_frame = prototypes.get(proto);
	}
	boolean result =
	    proto_frame != null &&  descendsFromOld(proto_frame, prototype);
	return result;
    }

    private static final Class CNF = Object.class;
    public Class classForPrototype(String prototype) {
	// cache these!
	synchronized (cached_classes) {
	    Class klass = cached_classes.get(prototype);
	    if (klass == CNF) return null;
	    if (klass != null) return klass;

	    
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
    
    public Class classForPrototype(PrototypeFrame pframe) {
	return classForPrototype(pframe.getName());
    }


    public boolean descendsFrom(DataFrame frame, String prototype) {
	Class klass = classForPrototype(prototype);
	return klass != null ? descendsFrom(frame, klass, prototype) : false;
    }

    boolean descendsFrom(DataFrame frame, Class klass, String prototype) {
	boolean result = klass.isInstance(frame);
	if (log.isDebugEnabled())
	    log.debug(frame+ 
		      (result ? " descends from " : " does not descend from ") 
		      +prototype);
	return result;
    }

    public boolean descendsFrom(PrototypeFrame frame, String prototype) {
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

    private boolean isContainmentRelation(DataFrame frame) {
	return descendsFrom(frame, container_relation);
    }

    public DataFrame getContainer(DataFrame frame) {
	synchronized (containers) {
	    return containers.get(frame);
	}
    }

    // Caller should synchronize on relation_lock
    private DataFrame getRelate(RelationFrame relationship,
				Map<RelationFrame,DataFrame> cache,
				String proto,
				String slot,
				Object value) {
	DataFrame result = findFrame(proto, slot, value);
	if (result == null) {
	    long time =  relationship.failed_lookup_time();
	    if (time > LOOKUP_WARN_TIME) {
		// reset the timer
		relationship.clear_failed_lookup_time();
		if (log.isWarnEnabled())
		    log.warn(" Proto = " +proto+
			     " Slot = " +slot+
			     " Value = " +value+
			     " matches nothing in " +name);
	    } else if (log.isDebugEnabled()) {
		log.debug(" Proto = " +proto+
			     " Slot = " +slot+
			     " Value = " +value+
			     " matches nothing in " +name);
	    }
	} else {
	    if (log.isDebugEnabled())
		log.debug(" Caching: Proto = " +proto+
			  " Slot = " +slot+
			  " Value = " +value+
			  " Result = " +result+ 
			  "in " +name);
	    relationship.clear_failed_lookup_time();
	    cache.put(relationship, result);
	}
	return result;
    }


    public DataFrame getRelationshipParent(RelationFrame relationship) {
	synchronized (relation_lock) {
	    DataFrame result = parent_cache.get(relationship);
	    if (result != null) {
		if (log.isInfoEnabled())
		    log.info(" Found cached relation value " +result);
		return result;
	    }

	    String proto = relationship.getParentPrototype();
	    String slot = relationship.getParentSlot();
	    Object value = relationship.getParentValue();
	    return getRelate(relationship, parent_cache, proto, slot, value);
	}
    }

    public DataFrame getRelationshipChild(RelationFrame relationship) {
	synchronized (relation_lock) {
	    DataFrame result = child_cache.get(relationship);
	    if (result != null) {
		if (log.isInfoEnabled())
		    log.info(" Found cached relation value " +result);
		return result;
	    }

	    String proto = relationship.getChildPrototype();
	    String slot = relationship.getChildSlot();
	    Object value = relationship.getChildValue();
	    return getRelate(relationship, child_cache, proto, slot, value);
	}
    }

    private boolean cacheRelation(RelationFrame relationship) {
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
                DataFrame old;
		synchronized (containers) {
		    old = containers.get(child);
		    containers.put(child, parent);
		}
                child.containerChange(old, parent);
		if (log.isInfoEnabled())
		    log.info("Parent of " +child+ " is " +parent);
	    }
	    return true;
	}
    }

    private void decacheRelation(RelationFrame relationship) {
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



    // Metrics
    public void subscribeToMetric(DataFrame frame, 
				  Observer observer, 
				  String path) {
	metrics.subscribeToValue(path, observer, frame);
    }

    public Metric getMetricValue(DataFrame frame, String path) {
	return metrics.getValue(path, frame);
    }




    // Query

    public PrototypeFrame findPrototypeFrame(String name) {
	return prototypes.get(name);
    }
    
    public Frame findFrame(UID uid) {
	synchronized (kb) {
	    Object raw = kb.get(uid);
	    if (raw instanceof Frame)
		return (Frame) raw;
	    else
		return null;
	}
    }

    public DataFrame findFrame(String proto, String slot, Object value) {
	if (slot == null || proto == null || value == null) return null;

	    
	Class klass = classForPrototype(proto);
	if (klass == null) return null;

	synchronized (frames) {
	    for (DataFrame frame : frames) {
		if (descendsFrom(frame, klass, proto)) {
		    Object candidate = frame.getValue(slot);
		    if (candidate != null && candidate.equals(value)) 
			return frame;
		}
	    }
	}
	return null;
    }

    public Set<DataFrame> findFrames(String proto, Properties slot_value_pairs) {
	Class klass = classForPrototype(proto);
	if (klass == null) return null;

	Set<DataFrame> results = new HashSet<DataFrame>();
	synchronized (frames) {
	    for (DataFrame frame : frames) {
		if (descendsFrom(frame, klass, proto) &&
		    frame.matchesSlots(slot_value_pairs))
		    results.add(frame);
	    }
	}
	return results;
      }

    Set<DataFrame> findChildren(DataFrame parent, 
	    String relation_prototype,
	    Map<RelationFrame,DataFrame> map) {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return null;
	Set<DataFrame> results = map != null ? null : new HashSet<DataFrame>();
	synchronized (relation_lock) {
	    for (Map.Entry<RelationFrame,DataFrame> entry : parent_cache.entrySet()) {
		if (entry.getValue().equals(parent)) {
		    RelationFrame relation = entry.getKey();
		    DataFrame child = child_cache.get(relation);
		    if (child != null &&
			descendsFrom(relation, klass, relation_prototype)) {
			if (map != null)
			    map.put(relation, child);
			else
			    results.add(child);
		    }
		}
	    }
	}
	return results;
    }

    Set<DataFrame> findParents(DataFrame child, 
	    String relation_prototype,
	    Map<RelationFrame,DataFrame> map) {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return null;

	Set<DataFrame> results = map != null ? null : new HashSet<DataFrame>();
	synchronized (relation_lock) {
	    for (Map.Entry<RelationFrame,DataFrame> entry : child_cache.entrySet()) {
		if (entry.getValue().equals(child)) {
		    RelationFrame relation = entry.getKey();
		    DataFrame parent = parent_cache.get(relation);
		    if (parent != null &&
			descendsFrom(relation, klass, relation_prototype)) {
			if (map != null)
			    map.put(relation, parent);
			else
			    results.add(parent);
		    }
		}
	    }
	}
	return results;
    }

    public Set<DataFrame> findRelations(Frame frame, // should be DataFrame
	    String role,
	    String relation_proto) {
	if (role.equals("parent")) {
	    return findParents((DataFrame) frame, relation_proto, null);
	} else if (role.equals("child")) {
	    return findChildren((DataFrame) frame, relation_proto, null);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Role " +role+ " should be \"parent\" or \"child\"");
	    return null;
	}
				
    }

    public Map<RelationFrame,DataFrame> findRelationshipFrames(DataFrame frame, 
	    String role, 
	    String relation_proto) {
	Map<RelationFrame,DataFrame> map = new HashMap<RelationFrame,DataFrame>();
	if (role.equals("parent")) {
	    findParents(frame, relation_proto, map);
	} else if (role.equals("child")) {
	    findChildren(frame, relation_proto, map);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Role " +role+ " should be \"parent\" or \"child\"");
	}
	return map;
				
    }

    public Path findPath(UID uid) {
	synchronized (kb) {
	    Object raw = kb.get(uid);
	    if (raw instanceof Path)
		return (Path) raw;
	    else
		return null;
	}
    }

    public Path findPath(String name) {
	synchronized (paths) {
	    return paths.get(name);
	}
    }




		
    // BBS queue
    
    private static class ChangeQueueEntry {
	final UniqueObject object;
	public ChangeQueueEntry(UniqueObject object) {
	    this.object = object;
	}
    }

    private static class Add extends ChangeQueueEntry {
	Add(UniqueObject object) {
	    super(object);
	}
    }

    private static class Change extends ChangeQueueEntry {
	Object change;
	Change(UniqueObject object, Object change) {
	    super(object);
	    this.change = change;
	}
    }

    private static class Remove extends ChangeQueueEntry {
	Remove(UniqueObject object) {
	    super(object);
	}
    }


    // Synchronized for a shorter time but doesn't work reliably.
    // Sometimes items are added while this is in progress and
    // execute doesn't run again.
    public void processQueue() {
	List<ChangeQueueEntry> changes = null;
	synchronized (change_queue_lock) {
	    changes = new ArrayList<ChangeQueueEntry>(change_queue);
	    change_queue = new ArrayList<ChangeQueueEntry>();
	}
	int count = changes.size();
	for (int i=0; i<count; i++) {
	    ChangeQueueEntry change = changes.get(i);
	    if (log.isDebugEnabled())
		log.debug("about to publish " + change);
	    if (change instanceof Change) {
		Change chng = (Change) change;
		List<Object> changes_list = new ArrayList<Object>(1);
		changes_list.add(chng.change);
		if (log.isDebugEnabled())
		    log.debug("Publish change " + chng.change);
		bbs.publishChange(chng.object, changes_list);
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
    public void processQueueSlow() {
	synchronized (change_queue_lock) {
	    int count = change_queue.size();
	    for (int i=0; i<count; i++) {
		ChangeQueueEntry change = change_queue.get(i);
		if (change instanceof Change) {
		    Change chng = (Change) change;
		    List<Object> changes_list = new ArrayList<Object>(1);
		    changes_list.add(chng.change);
		    bbs.publishChange(chng.object, changes_list);
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
	
    void publishAdd(UniqueObject object) {
	synchronized (change_queue_lock) {
	    change_queue.add(new Add(object));
	    bbs.signalClientActivity();
	}
    }

    void publishChange(UniqueObject object, Object change) {
	synchronized (change_queue_lock) {
	    change_queue.add(new Change(object, change));
	    bbs.signalClientActivity();
	}
    }

    void publishRemove(UniqueObject object) {
	synchronized (change_queue_lock) {
	    change_queue.add(new Remove(object));
	    bbs.signalClientActivity();
	}
    }






    // XML dumping

    void dumpDataFrames(PrintWriter writer, int indentation, int offset) {
	synchronized (kb) {
	    for (Object raw : kb.values()) {
		if (raw instanceof DataFrame) {
		    DataFrame frame = (DataFrame) raw;
		    frame.dump(writer, indentation, offset);
		}
	    }
	}
    }

    void dumpData(File file, int indentation, int offset)
    throws java.io.IOException {
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



    void dumpProtoFrames(PrintWriter writer, int indentation, int offset) {
	synchronized (prototypes) {
	    for (PrototypeFrame frame : prototypes.values()) {
		frame.dump(writer, indentation, offset);
	    }
	}
    }

    void dumpPaths(PrintWriter writer, int indentation, int offset) {
	synchronized (kb) {
	    for (Object raw : kb.values()) {
		if (raw instanceof Path) {
		    Path path = (Path) raw;
		    path.dump(writer, indentation, offset);
		}
	    }
	}
    }

    void dumpPrototypes(File file, int indentation, int offset)
	throws java.io.IOException {
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
	throws java.io.IOException {
	dumpPrototypes(proto_file, 0, 2);
	dumpData(data_file, 0, 2);
    }


}
