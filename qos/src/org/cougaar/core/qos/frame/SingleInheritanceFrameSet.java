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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
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
import org.cougaar.util.UnaryPredicate;
import org.xml.sax.Attributes;


/**
 * Currently the only implementation of FrameSet, this class enforces
 * single inheritance in both the prototype hierarchy and the
 * containment hierarchy.
 */
public class SingleInheritanceFrameSet
    implements FrameSet {
    
    private static final long LOOKUP_WARN_TIME = 10000;

    private final String domain;
    private final String name;
    private final String pkg;
    private final LoggingService log;
    private final UIDService uids;
    private final BlackboardService bbs;
    private final MetricsService metrics;
    private final Object change_queue_lock, relation_lock;
    //  This slot is assumed to be immutable
    private final String primaryIndexSlot;
    
    private List<ChangeQueueEntry> change_queue;
    private Map<UID,Object> kb; // values can be either Frames or Paths
    private Map<String,Class> cached_classes;
    private Map<String,PrototypeFrame> prototypes;
    private Map<RelationFrame,DataFrame> parent_cache, child_cache;
    private Map<DataFrame,Set<RelationFrame>> inverse_parent_cache, inverse_child_cache;
    private Map<String,Path> paths;
    private Set<DataFrame> frames;

    // Containment hackery
    private Set<RelationFrame> pending_relations;
    private Map<DataFrame,DataFrame> containers;
    private String container_relation;
    
    private Map<PrototypeFrame,Map<Object,DataFrame>> primaryIndexCache;
    
    private final Object transactionLock = new TransactionLock();
    
    
    
    private Set<SlotAggregation> aggregations;
    
    //eclispe friendly lock names
    public class TransactionLock extends Object {};
    public class ChangeQueueLock extends Object {};
    public class RelationLock extends Object {};  
    public class PendingRelationships extends HashSet<RelationFrame> {};
    public class Containers extends  HashMap<DataFrame,DataFrame> {};
    public class KB extends  HashMap<UID,Object>{};
    public class Frames extends  HashSet<DataFrame> {};
    public class CachedClasses extends  HashMap<String,Class> {};
    public class Paths extends  HashMap<String,Path> {};
    public class Prototypes extends  HashMap<String,PrototypeFrame> {};


    public SingleInheritanceFrameSet(String pkg,
				     ServiceBroker sb,
				     BlackboardService bbs,
				     String domain,
				     String name,
				     String container_relation,
				     String primaryIndexSlot) {
	this.domain = domain;
	this.name = name;
	this.primaryIndexSlot = primaryIndexSlot;
	this.container_relation = container_relation;
	this.cached_classes = new CachedClasses();
	this.parent_cache = new HashMap<RelationFrame,DataFrame>();
	this.child_cache = new HashMap<RelationFrame,DataFrame>();
	this.inverse_child_cache = new HashMap<DataFrame,Set<RelationFrame>>();
	this.inverse_parent_cache = new HashMap<DataFrame,Set<RelationFrame>>();
	this.paths = new Paths();
	this.frames = new Frames();
	this.primaryIndexCache = new HashMap<PrototypeFrame,Map<Object, DataFrame>>();
	this.pkg = pkg;
	this.bbs = bbs;
	this.change_queue = new ArrayList();
	this.change_queue_lock = new ChangeQueueLock();
	this.relation_lock = new RelationLock();
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);
	metrics = (MetricsService)
	    sb.getService(this, MetricsService.class, null);

 	this.kb = new KB();
	this.prototypes = new Prototypes();

	this.pending_relations = new PendingRelationships();
	this.containers = new Containers();
	this.aggregations = new HashSet<SlotAggregation>();
    }


    // Slot aggregation
    
    public void addAggregator(String slot, String relatedSlot, String relation, 
	    String role, String aggregator) {
	try {
	    SlotAggregation agg = new SlotAggregation(this, slot, relatedSlot, relation, role, aggregator);
	    aggregations.add(agg);
	} catch (Exception e) {
	    log.error("Couldn't instantiate " + aggregator);
	}
    }
    
    public void initializeAggregators() {
	for (SlotAggregation aggregation : aggregations) {
	    aggregation.setupSubscriptions(bbs);
	}
    }
    
    private void executeAggregators() {
	for (SlotAggregation aggregation : aggregations) {
	    aggregation.execute(bbs);
	}
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
		synchronized (relation_lock) {
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
	Object key = null;
	if (primaryIndexSlot != null) {
	    // Look for an existing frame with the same key
	    key = values.get(primaryIndexSlot);
	    if (key != null) {
		DataFrame oldFrame = getIndexedFrame(proto, key);
		if (oldFrame != null) {
		    log.error("A " +proto+ " frame with " +primaryIndexSlot+ 
			    " " +key+ " already exists");
		    return oldFrame;
		}
	    }
	}
	DataFrame frame = DataFrame.newFrame(this, proto, uid, values);
	if (key != null) {
	    PrototypeFrame pframe = frame.getPrototype();
	    Map<Object,DataFrame> cache = primaryIndexCache.get(pframe);
	    if (cache == null) {
		cache = new HashMap<Object,DataFrame>();
		primaryIndexCache.put(pframe, cache);
	    }
	    cache.put(key, frame);
	}
	addObject(frame);
	publishAdd(frame);
	return frame;

    }

    public DataFrame makeFrame(DataFrame frame) {
	if (primaryIndexSlot != null) {
	    Object key = frame.getValue(primaryIndexSlot);
	    if (key != null) {
		PrototypeFrame pframe = frame.getPrototype();
		
		String proto = pframe.getName();
		DataFrame oldFrame = getIndexedFrame(proto, key);
		if (oldFrame != null) {
		    log.error("A " +proto+ " frame with " +primaryIndexSlot+ 
			    " " +key+ " already exists");
		    return oldFrame;
		}
		
		Map<Object,DataFrame> cache = primaryIndexCache.get(pframe);
		if (cache == null) {
		    cache = new HashMap<Object,DataFrame>();
		    primaryIndexCache.put(pframe, cache);
		}
		cache.put(key, frame);
	    }
	}
	addObject(frame);
	publishAdd(frame);
	return frame;
    }
    
    public boolean isResolved(RelationFrame frame) {
	synchronized(pending_relations) {
	    return pending_relations.contains(frame);
	}
    }
    
    public RelationFrame makeRelationship(String kind, Properties values, DataFrame parent, DataFrame child) {
	UID uid = uids.nextUID();
	RelationFrame rel = (RelationFrame) DataFrame.newFrame(this, kind, uid, values);
	synchronized (relation_lock) {
	    cacheRelation(rel, parent, child);
	}	
	synchronized (kb) {
	    kb.put(uid, rel);
	}
	synchronized (frames) {
	    frames.add(rel);
	}
	publishAdd(rel);
	return rel;
    }

    // In this case the proto argument refers to what the prototype
    // should be a prototype of.  
    public PrototypeFrame makePrototype(String proto, 
					String parent, 
					Attributes attrs, 
					Map<String,Attributes> slots) {
	UID uid = uids.nextUID();
	return makePrototype(proto, parent, attrs, slots, uid);
    }

    public PrototypeFrame makePrototype(String proto, 
					String parent, 
					Attributes attrs,
					Map<String,Attributes> slots,
					UID uid) {
	PrototypeFrame frame = null;
	synchronized (prototypes) { 
	    if (prototypes.containsKey(proto)) {
		if (log.isWarnEnabled())
		    log.warn("Ignoring prototype " +proto);
		return null;
	    } else {
		frame = new PrototypeFrame(this, proto, parent, uid, attrs, slots);
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
		if (slot.startsWith(CHILD)) {
		    DataFrame child = child_cache.get(rframe);
		    child_cache.remove(rframe);
		    Set<RelationFrame> rframes = inverse_child_cache.get(child);
		    rframes.remove(rframe);
		} else if (slot.startsWith(PARENT)) {
		    DataFrame parent = parent_cache.get(rframe);
		    parent_cache.remove(frame);
		    Set<RelationFrame> rframes = inverse_parent_cache.get(parent);
		    rframes.remove(rframe);
		}
	    }
	    cacheRelation(rframe);
	}
	    

	// Publish the frame itself as the change, or just a change
	// record for the specific slot?
	Frame.Change change = new Frame.Change(frame.getUID(), slot, value);
	publishChange(frame, change);
    }

    // Caller should synchronize on kb
    private void removeFromKB(DataFrame frame) {
	synchronized (kb) { 
	    if (primaryIndexSlot != null) {
		PrototypeFrame pframe = frame.getPrototype();
		Map<Object,DataFrame> cache = primaryIndexCache.get(pframe);
		if (cache != null) {
		    Object key = frame.getValue(primaryIndexSlot);
		    cache.remove(key);
		}
	    }
	    kb.remove(frame.getUID());
	}
    }

    public void removeFrame(DataFrame frame) {
	synchronized (kb) { 
	    removeFromKB(frame);
	}
	synchronized (frames) {
	    frames.remove(frame);
	}

	// Handle the removal of containment relationship frames
	if (frame instanceof RelationFrame) {
	    RelationFrame rframe = (RelationFrame) frame;
	    decacheRelation(rframe);
	} else {
	    // TODO: If frame is some other frame's container, that other frame needs to be told!
	    synchronized(relation_lock) {
		Set<RelationFrame> rframes = inverse_child_cache.get(frame);
		inverse_child_cache.remove(frame);
		if (rframes != null) {
		    for (RelationFrame rframe : rframes) {
			child_cache.remove(rframe);
			pending_relations.add(rframe);
		    }
		}
		rframes = inverse_parent_cache.get(frame);
		inverse_parent_cache.remove(frame);
		if (rframes != null) {
		    for (RelationFrame rframe : rframes) {
			parent_cache.remove(rframe);
			pending_relations.add(rframe);
		    } 
		}
	    }
	}

	publishRemove(frame);
    }

    public void removeFrameAndRelations(DataFrame frame) {
	Set<DataFrame> removedFrames = new HashSet<DataFrame>();
	removedFrames.add(frame);
	synchronized (kb) { 
	    removeFromKB(frame);
	    synchronized(relation_lock) {   
		containers.remove(frame);
		Set<RelationFrame> rframes = inverse_child_cache.get(frame);
		inverse_child_cache.remove(frame);
		if (rframes != null) {
		    for (RelationFrame rframe : rframes) {
			child_cache.remove(rframe);
			parent_cache.remove(rframe);
			pending_relations.remove(rframe);
			removeFromKB(rframe);
		    }
		    removedFrames.addAll(rframes);
		}
		rframes = inverse_parent_cache.get(frame);
		inverse_parent_cache.remove(frame);
		if (rframes != null) {
		    for (RelationFrame rframe : rframes) {
			child_cache.remove(rframe);
			parent_cache.remove(rframe);
			pending_relations.remove(rframe);
			removeFromKB(rframe);
		    }
		    removedFrames.addAll(rframes);
		}
	    }
	}
	synchronized (frames) {
	    frames.removeAll(removedFrames);
	}
	publishRemove(removedFrames);
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
				Map<DataFrame,Set<RelationFrame>> inverseCache,
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
	    Set<RelationFrame> rframes = inverseCache.get(result);
	    if (rframes == null) {
		rframes = new HashSet<RelationFrame>();
		inverseCache.put(result, rframes);
	    }
	    rframes.add(relationship);
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
	    return getRelate(relationship, parent_cache, inverse_parent_cache, proto, slot, value);
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
	    return getRelate(relationship, child_cache, inverse_child_cache, proto, slot, value);
	}
    }

    private boolean cacheRelation(RelationFrame relationship) {
	// cache a containment relationship
		    
	DataFrame parent = getRelationshipParent(relationship);
	DataFrame child = getRelationshipChild(relationship);
	return cacheRelation(relationship, parent, child);
    }
    
    private boolean cacheRelation(RelationFrame relationship, DataFrame parent, DataFrame child) {
	if (parent == null || child == null) {
	    synchronized (relation_lock) {    
		// Queue for later
		synchronized (pending_relations) {
		    pending_relations.add(relationship);
		    if (log.isDetailEnabled())
			log.detail("Relation of type " +relationship.getPrototype().getName() +
				" between " +relationship.getParentValue() +
				" and "+relationship.getChildValue()+ " is unresolved");
		}
		return false;
	    }
	} else {
	    if (isContainmentRelation(relationship)) {
                DataFrame old;
                synchronized (relation_lock) {
                    synchronized (containers) {
                	old = containers.get(child);
                	containers.put(child, parent);
                    }
                }
                // JAZ container Change potentially walks all child paths so can't hold relation_lock
                child.containerChange(old, parent);
		if (log.isInfoEnabled())
		    log.info("Parent of " +child+ " is " +parent);
	    }
	    return true;
	}
    }

    private void decacheRelation(RelationFrame relationship) {
	synchronized (relation_lock) {
	    DataFrame child = child_cache.get(relationship);
	    if (child != null) {
		Set<RelationFrame> rframes = inverse_child_cache.get(child);
		if (rframes != null) {
		    rframes.remove(relationship);
		}
	    }
	    DataFrame parent = parent_cache.get(relationship);
	    if (parent != null) {
		Set<RelationFrame> rframes = inverse_parent_cache.get(parent);
		if (rframes != null) {
		    rframes.remove(relationship);
		}
	    }
	    child_cache.remove(relationship);
	    parent_cache.remove(relationship);
	
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
    
    private DataFrame getIndexedFrame(String proto, Object key) {
	PrototypeFrame pframe = findPrototypeFrame(proto);
	Map<Object,DataFrame> cache = primaryIndexCache.get(pframe);
	if (cache != null) {
	    DataFrame value = cache.get(key);
	    if (value != null) return value;
	}
	// walk up the proto hierarchy
	String parent = pframe.getKind();
	if (parent != null) {
	    return getIndexedFrame(parent, key);
	} else {
	    return null;
	}
    }

    public DataFrame findFrame(String proto, String slot, Object value) {
	if (slot == null || proto == null || value == null) return null;
	if (primaryIndexSlot != null && slot.equals(primaryIndexSlot)) {
	    DataFrame frame = getIndexedFrame(proto, value);
	    if (frame != null) {
		return frame;
	    }
	}
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
		if (descendsFrom(frame, klass, proto)) {
		    if (slot_value_pairs == null || frame.matchesSlots(slot_value_pairs)) {
			results.add(frame);
		    }
		}
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
	    Set<RelationFrame> rframes = inverse_parent_cache.get(parent);
	    if (rframes != null) {
		for (RelationFrame rframe : rframes) {
		    if (descendsFrom(rframe, klass, relation_prototype)) {
			DataFrame child = child_cache.get(rframe);
			if (child != null) {
			    if (map != null)
				map.put(rframe, child);
			    else
				results.add(child);
			}
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
	    Set<RelationFrame> rframes = inverse_child_cache.get(child);
	    if (rframes != null) {
		for (RelationFrame rframe : rframes) {
		    if (descendsFrom(rframe, klass, relation_prototype)) {
			DataFrame parent = parent_cache.get(rframe);
			if (parent != null) {
			    if (map != null)
				map.put(rframe, parent);
			    else
				results.add(parent);
			}
		    }
		}
	    }
	}
	return results;
    }


    int countChildren(DataFrame parent, String relation_prototype) {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return 0;
	int count = 0;
	synchronized (relation_lock) {
	    Set<RelationFrame> rframes = inverse_parent_cache.get(parent);
	    if (rframes != null) {
		for (RelationFrame rframe : rframes) {
		    if (descendsFrom(rframe, klass, relation_prototype)) {
			DataFrame child = child_cache.get(rframe);
			if (child != null) {
			    ++count;
			}
		    }
		}
	    }
	}
	return count;
    }

    int countParents(DataFrame child, String relation_prototype) {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return 0;
	int count = 0;
	synchronized (relation_lock) {
	    Set<RelationFrame> rframes = inverse_child_cache.get(child);
	    if (rframes != null) {
		for (RelationFrame rframe : rframes) {
		    if (descendsFrom(rframe, klass, relation_prototype)) {
			DataFrame parent = parent_cache.get(rframe);
			if (parent != null) {
			    ++count;
			}
		    }
		}
	    }
	}
	return count;
    }
    
    DataFrame findFirstChild(DataFrame parent, String relation_prototype) {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return null;
	synchronized (relation_lock) {
	    Set<RelationFrame> rframes = inverse_parent_cache.get(parent);
	    if (rframes != null) {
		for (RelationFrame rframe : rframes) {
		    if (descendsFrom(rframe, klass, relation_prototype)) {
			DataFrame child = child_cache.get(rframe);
			if (child != null) {
			    return child;
			}
		    }
		}
	    }
	}
	return null;
    }

    DataFrame findFirstParent(DataFrame child, String relation_prototype) {
	Class klass = classForPrototype(relation_prototype);
	if (klass == null) return null;
	synchronized (relation_lock) {
	    Set<RelationFrame> rframes = inverse_child_cache.get(child);
	    if (rframes != null) {
		for (RelationFrame rframe : rframes) {
		    if (descendsFrom(rframe, klass, relation_prototype)) {
			DataFrame parent = parent_cache.get(rframe);
			if (parent != null) {
			    return parent;
			}
		    }
		}
	    }
	}
	return null;
    }
    
   

    
    public Set<DataFrame> findRelations(Frame frame, // should be DataFrame
	    String role,
	    String relation_proto) {
	if (role.equals(PARENT)) {
	    return findParents((DataFrame) frame, relation_proto, null);
	} else if (role.equals(CHILD)) {
	    return findChildren((DataFrame) frame, relation_proto, null);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Role " +role+ " should be " +PARENT+ " or " + CHILD);
	    return null;
	}
    }
    
    public DataFrame findFirstRelation(Frame frame, // should be DataFrame
	    String role,
	    String relation_proto) {
	if (role.equals(PARENT)) {
	    return findFirstParent((DataFrame) frame, relation_proto);
	} else if (role.equals(CHILD)) {
	    return findFirstChild((DataFrame) frame, relation_proto);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Role " +role+ " should be " +PARENT+ " or " + CHILD);
	    return null;
	}
    }
    
    public int countRelations(Frame frame, // should be DataFrame
	    String role,
	    String relation_proto) {
	if (role.equals(PARENT)) {
	    return countParents((DataFrame) frame, relation_proto);
	} else if (role.equals(CHILD)) {
	    return countChildren((DataFrame) frame, relation_proto);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Role " +role+ " should be " +PARENT+ " or " + CHILD);
	    return 0;
	}
    }

    public Map<RelationFrame,DataFrame> findRelationshipFrames(DataFrame frame, 
	    String role, 
	    String relation_proto) {
	Map<RelationFrame,DataFrame> map = new HashMap<RelationFrame,DataFrame>();
	if (role.equals(PARENT)) {
	    findParents(frame, relation_proto, map);
	} else if (role.equals(CHILD)) {
	    findChildren(frame, relation_proto, map);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Role " +role+ " should be " +PARENT+ " or " + CHILD);
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

    public void runInTransaction(Runnable r) {
	synchronized (transactionLock) {
	    r.run();
	}	
    }

    // Synchronized for a shorter time but doesn't work reliably.
    // Sometimes items are added while this is in progress and
    // execute doesn't run again.
    public void processQueue() {
	
	synchronized (transactionLock) {
	    executeAggregators();
	    List<ChangeQueueEntry> changes = null;
	    synchronized (change_queue_lock) {
		changes = new ArrayList<ChangeQueueEntry>(change_queue);
		change_queue = new ArrayList<ChangeQueueEntry>();
	    }
	    int count = changes.size();
	    for (int i = 0; i < count; i++) {
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
		} else if (change instanceof Add) {
		    Add add = (Add) change;
		    bbs.publishAdd(add.object);
		} else if (change instanceof Remove) {
		    Remove rem = (Remove) change;
		    bbs.publishRemove(rem.object);
		}
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
    
    void publishRemove(Collection<DataFrame> objects) {
	synchronized (change_queue_lock) {
	    for (UniqueObject object : objects) change_queue.add(new Remove(object));
	}
	bbs.signalClientActivity();
    }






    // XML dumping

    void writeDataFrames(PrintWriter writer, int indentation, int offset, UnaryPredicate filter,
	    boolean allSlots) {
	synchronized (kb) {
	    for (Object raw : kb.values()) {
		if (raw instanceof DataFrame && !(raw instanceof RelationFrame)) {
		    if (filter != null && !filter.execute(raw)) {
			// wrong kind of frame
			continue;
		    }
		    DataFrame frame = (DataFrame) raw;
		    frame.write(writer, indentation, offset, allSlots);
		}
	    }
	    for (Object raw : kb.values()) {
		if (raw instanceof RelationFrame) {
		    if (filter != null && !filter.execute(raw)) {
			// wrong kind of frame
			continue;
		    }
		    DataFrame frame = (DataFrame) raw;
		    frame.write(writer, indentation, offset, allSlots);
		}
	    }
	}
    }

    void writeData(File file, int indentation, int offset, UnaryPredicate filter,
	    boolean allSlots)
    throws java.io.IOException {
	FileWriter fwriter = new FileWriter(file);
	PrintWriter writer = new PrintWriter(fwriter);

	indentation += offset;
	writer.println("<!-- !DOCTYPE " +domain+ " SYSTEM \"" +domain+ ".dtd\" -->");
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<" + domain+ ">");
	indentation += offset;

	writeDataFrames(writer, indentation, offset, filter, allSlots);

	indentation -= offset;
	writer.println("</" + domain+ ">");


	writer.close();
    }
    
    private void exportFramesOfType(File file, Set<String> prototypes, boolean allSlots) 
    throws IOException {
	Set<Class> classes = new HashSet<Class>();
	for (String prototype : prototypes) {
	    classes.add(classForPrototype(prototype));
	}
	final Class[] pclasses = new Class[classes.size()];
	classes.toArray(pclasses);
	UnaryPredicate filter = new UnaryPredicate() {
	    public boolean execute(Object o) {
		Class cls = o.getClass();
		for (Class cl : pclasses) {
		    if (cl.isAssignableFrom(cls)) {
			return true;
		    }
		}
		return false;
	    }
	};
	writeData(file, 0, 2, filter, allSlots);
    }

    public void importFrames(URL location) 
    throws IOException{
	FrameSetParser parser = new FrameSetParser();
	parser.parseFrameSetData(name, location, this);
    }

    public void exportFrames(File file)
	throws IOException {
	writeData(file, 0, 2, null, false);
    }
    
    public void exportFrames(File file, Set<String> prototypes)
	throws IOException {
	if (prototypes != null) {
	    exportFramesOfType(file, prototypes, false);
	} else {
	    writeData(file, 0, 2, null, false);
	}
    }
    
    public void exportFrames(File file, Set<String> prototypes, boolean allSlots)
    throws IOException {
	if (prototypes != null) {
	    exportFramesOfType(file, prototypes, allSlots);
	} else {
	    writeData(file, 0, 2, null, allSlots);
	}
    }
}
