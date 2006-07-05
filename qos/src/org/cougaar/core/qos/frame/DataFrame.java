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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.util.UID;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * This extension to {@link Frame} is the basic representation of a
 * frame representing data (cf {@link PrototypeFrame}).  Classes
 * generated from prototype xml extend this one, either directly or
 * indirectly.
 */
abstract public class DataFrame 
    extends Frame
    implements PropertyChangeListener {
    
    public static final String NIL = "NIL";
    
    private static Map<String,Map<String,FrameMaker>> FramePackages = 
	new HashMap<String,Map<String,FrameMaker>>();;
    private static Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.DataFrame.class);


    protected static void registerFrameMaker(String pkg,
					     String proto,
					     FrameMaker maker) {
	if (log.isDebugEnabled())
	    log.debug("Registering FrameMaker " +maker+ " for prototype "
		      +proto+ " in package " +pkg);
	synchronized (FramePackages) {
	    Map<String,FrameMaker> frameTypeMap = FramePackages.get(pkg);
	    if (frameTypeMap == null) {
		frameTypeMap = new HashMap();
		FramePackages.put(pkg, frameTypeMap);
	    }
	    frameTypeMap.put(proto, maker);
	}
    }

    protected static FrameMaker findFrameMaker(String pkg, String proto) {
	synchronized (FramePackages) {
	    Map<String,FrameMaker> frameTypeMap = FramePackages.get(pkg);
	    if (frameTypeMap != null) 
		return frameTypeMap.get(proto);
	    else
		return null;
	}
    }

    protected static Logger getLogger() {
	return log;
    }


    public static DataFrame newFrame(FrameSet frameSet,
				     String proto, 
				     UID uid,
				     Properties initial_values) {
	String pkg = frameSet.getPackageName();
	return newFrame(pkg, frameSet, proto, uid, initial_values);
    }


    public static DataFrame newFrame(String pkg,
				     FrameSet frameSet,
				     String proto, 
				     UID uid,
				     Properties values) {
	if (log.isDebugEnabled())
	    log.debug("Searching for FrameMaker for prototype "
		      +proto+ " in package " +pkg);
	// force a class load (ugh)
	frameSet.classForPrototype(proto);
	DataFrame frame = null;
	FrameMaker maker = findFrameMaker(pkg, proto);
	if (maker != null) {
	    frame = maker.makeFrame(frameSet, uid);
	    frame.initializeValues(values);
	} else {
	    log.error("No FrameMaker for " +proto+ " in package " +pkg);
	}
	return frame;
    }

    
    private Properties props;
    private transient Map<String,DataFrame> ddeps;
    private transient Map<String,List<RelationFrame>> rdeps;
    private transient Map<String,String> slot_map;
    private transient Map<String,Set<DataFrame>> path_dependents;
    private transient Object rlock;
    private transient PropertyChangeSupport pcs;

    protected DataFrame(FrameSet frameSet, UID uid) {
	super(frameSet, uid);
	initializeTransients();
    }

    // Subclass responsibility
    abstract protected Object getLocalValue(String slot);
    abstract protected void setLocalValue(String slot, Object value);
//     abstract protected void removeLocalValue(String slot);
    abstract protected void initializeLocalValue(String slot, Object value);



    // Serialization
    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, ClassNotFoundException {
	in.defaultReadObject();
	initializeTransients();
    }
    
    private void initializeTransients() {
	pcs = new PropertyChangeSupport(this);
	ddeps = new HashMap<String,DataFrame>();
	rdeps = new HashMap<String,List<RelationFrame>>();
	slot_map = new HashMap<String,String>();
	path_dependents = new HashMap<String,Set<DataFrame>>();
	rlock = new Object();
    }

    // PropertyChangeListener

    public void propertyChange(PropertyChangeEvent event) {
	// Some frame I depend on has changed (container only, for now).
	// Resignal to my listeners.
	if (log.isDebugEnabled())
	    log.debug("Propagate PropertyChange " +event.getPropertyName()+
		      " old value = " +event.getOldValue()+
		      " new value = " +event.getNewValue());
	pcs.firePropertyChange(event);
    }

    // Path dependencies.


    Object get_rlock() {
	return 	rlock;
    }


    // Caller should lock rlock
    void clearRelationDependencies(String slot) {
	List<RelationFrame> rframes = rdeps.get(slot);
	if (rframes == null) return;
	for (RelationFrame rframe: rframes) {
	    rframe.removePathDependent(this, slot);
	}
	rdeps.remove(slot);
	DataFrame dframe = ddeps.get(slot);
	dframe.removePathDependent(this, slot);
	ddeps.remove(slot);
    }

    // Caller should lock rlock
    void addRelationSlotDependency(DataFrame frame, 
				   String my_slot, 
				   String owner_slot) {
	ddeps.put(my_slot, frame);
	synchronized (slot_map) {
	    slot_map.put(owner_slot, my_slot);
	}
	frame.addPathDependent(this, owner_slot);
    }

    // Caller should lock rlock
    void addRelationDependency(RelationFrame rframe, String slot) {
	List<RelationFrame> rframes = rdeps.get(slot);
	if (rframes == null) {
	    rframes = new ArrayList<RelationFrame>();
	    rdeps.put(slot, rframes);
	}
	rframes.add(rframe);
	rframe.addPathDependent(this, slot);
    }

    // Caller should lock rlock
    void removeRelationDependency(RelationFrame rframe, String slot) {
	if (log.isDebugEnabled())
	    log.debug("Removing relation dependency " +rframe+ " on " +this+
		     " for slot " +slot);
	List<RelationFrame> rframes = rdeps.get(slot);
	if (rframes != null) rframes.remove(rframe);
	rframe.removePathDependent(this, slot);
    }


    void addPathDependent(DataFrame dependent, String slot) {
	if (log.isDebugEnabled())
	    log.debug(this + " is adding dependent " +dependent+
		      " for slot " +slot);
	synchronized (path_dependents) {
	    Set<DataFrame> dependents = path_dependents.get(slot);
	    if (dependents == null) {
		dependents = new HashSet<DataFrame>();
		path_dependents.put(slot, dependents);
	    }
	    dependents.add(dependent);
	}
    }

    void removePathDependent(DataFrame dependent, String slot) {
	if (log.isDebugEnabled())
	    log.debug(this + " is removing dependent " +dependent+
		      " for slot " +slot);
	synchronized (path_dependents) {
	    Set<DataFrame> dependents = path_dependents.get(slot);
	    if (dependents != null) dependents.remove(dependent);
	}
    }



    void pathDependencyChange(String slot) {
	if (log.isInfoEnabled())
	    log.info("Path dependency has changed for frame " +this+
		      " and for slot " +slot);
	Object new_value = getValue(slot);
	pcs.firePropertyChange(FrameGen.fixName(slot, true, true),
			       null, new_value);
	notifyPathDependents(slot);
	// slotModified(slot, null, new_value, true, true);
    }


    private void notifyPathDependents(String slot, Set<DataFrame> dependents) {
	for (DataFrame frame : dependents) {
	    frame.pathDependencyChange(slot);
	}
    }

    void notifyPathDependents(String owner_slot) {
	String slot = null;
	synchronized (slot_map) {
	    slot = slot_map.get(owner_slot);
	}
	Set<DataFrame> copy = null;
	synchronized (path_dependents) {
	    Set<DataFrame> dependents = path_dependents.get(slot);
	    if (dependents == null) return;
	    copy = new HashSet(dependents);
	}
	notifyPathDependents(slot, copy);
    }

    void notifyAllPathDependents() {
	synchronized (path_dependents) {
	    for (Map.Entry<String,Set<DataFrame>> entry : path_dependents.entrySet()) {
		String slot = entry.getKey();
		Set<DataFrame> dependents = entry.getValue();
		notifyPathDependents(slot, new HashSet<DataFrame>(dependents));
	    }
	}
    }


    // Jess ShadowFact
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
	pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
	pcs.removePropertyChangeListener(pcl);
    }


    
    // Public accesssors

    // In this context "local" means: include slots the prototype tree
    // but not from the container tree.
    public Properties getLocalSlots() {
	Properties props = new VisibleProperties();
	collectSlotValues(props);
	return props;
    }

    public Properties getAllSlots() {
	Properties props = new VisibleProperties();
	collectSlotValues(props);
	collectContainerSlotValues(props);
	return props;
    }

    public Map<String,SlotDescription> slotDescriptions() {
	Map<String,SlotDescription> descriptions = new HashMap<String,SlotDescription>();
	collectSlotDescriptions(descriptions);
	return descriptions;
    }

    public void setValue(String slot, Object value) {
	setLocalValue(slot, value);
    }

    public void initializeValues(Properties values) {
	Iterator itr = values.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    try {
		initializeLocalValue((String) entry.getKey(), entry.getValue());
	    } catch (Throwable t) {
		log.error("Error initializing slot " +entry.getKey()+
			  " of " +this+ " to "+entry.getValue(),
			  t);
	    }
	}
	postInitialize();
    }

    public String getContainerKind() {
	Frame container = containerFrame();
	return container == null ? null : container.getKind();
    }

    // Don't use a beany name here...
    public DataFrame containerFrame() {
	if (frameSet == null) return null;
	DataFrame result = frameSet.getContainer(this);
	if (result == null) {
	    if (log.isDebugEnabled()) log.debug(this + " has no container!");
	}
	return result;
    }

    public Set<DataFrame> findRelations(String role, String relation) {
	if (frameSet == null) return null;
	return frameSet.findRelations(this, role, relation);
    }

    public Map<RelationFrame,DataFrame> findRelationshipFrames(String role, String relation) {
	if (frameSet == null) return null;
	return frameSet.findRelationshipFrames(this, role, relation);
    }

    public boolean isa(String kind) {
	if (frameSet == null) return kind.equals(getKind());
	return frameSet.descendsFrom(this, kind);
    }



    // Support

    protected void slotModified(String slot, 
				Object old_value, 
				Object new_value,
				boolean notify_listeners,
				boolean notify_blackboard) {
	if (notify_blackboard && frameSet != null) 
	    frameSet.valueUpdated(this, slot, new_value);
	if (notify_listeners) {
	    String fixed_name = FrameGen.fixName(slot, true, true);
	    fireChange(fixed_name, old_value, new_value);
	    notifyPathDependents(slot);
	}
    }


    protected void slotInitialized(String slot, Object value) {
	// nothing to be done at the moment
    }

    protected void postInitialize() {
	// Nothing at this level.  Frame types with Metric-value slots
	// should subscribe to the MetricsService here
    }

    //  Converters used in generated code
    protected String force_String(Object x) {
	if (x instanceof String)
	    return ((String) x);
	else
	    return x.toString(); // hmm
    }

    protected Metric force_Metric(Object x) {
	if (x instanceof Metric)
	    return ((Metric) x);
	else
	    throw new RuntimeException(x + " cannot be coerced to a Metric");
    }

    protected double force_double(Object x) {
	if (x instanceof String)
	    return Double.parseDouble((String) x);
	else if (x instanceof Double)
	    return ((Double) x).doubleValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a double");
    }

    protected Double force_Double(Object x) {
	if (x instanceof String) {
	    return Double.valueOf((String) x);
	} else if (x instanceof Double) {
	    return (Double) x;
	} else {
	    throw new RuntimeException(x + " cannot be coerced to a Double");
	}
    }

    protected float force_float(Object x) {
	if (x instanceof String)
	    return Float.parseFloat((String) x);
	else if (x instanceof Float)
	    return ((Float) x).floatValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a float");
    }

    protected Float force_Float(Object x) {
	if (x instanceof String)
	    return Float.valueOf((String) x);
	else if (x instanceof Float)
	    return (Float) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Float");
    }


    protected long force_long(Object x) {
	if (x instanceof String)
	    return Long.parseLong((String) x);
	else if (x instanceof Long)
	    return ((Long) x).longValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a long");
    }


    protected Long force_Long(Object x) {
	if (x instanceof String)
	    return Long.valueOf((String) x);
	else if (x instanceof Long)
	    return (Long) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Long");
    }


    protected int force_int(Object x) {
	if (x instanceof String)
	    return Integer.parseInt((String) x);
	else if (x instanceof Integer)
	    return ((Integer) x).intValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to an int");
    }

    protected Integer force_Integer(Object x) {
	if (x instanceof String)
	    return Integer.valueOf((String) x);
	else if (x instanceof Integer)
	    return (Integer) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Long");
    }


    protected boolean force_boolean(Object x) {
	if (x instanceof String)
	    return ((String) x).equalsIgnoreCase("true");
	else if (x instanceof Boolean)
	    return ((Boolean) x).booleanValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a boolean");
    }


    protected Boolean force_Boolean(Object x) {
	if (x instanceof String)
	    return Boolean.valueOf((String) x);
	else if (x instanceof Boolean)
	    return (Boolean) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Long");
    }




    protected void fireChange(String property, 
			      Object old_value, 
			      Object new_value) {
	if (log.isDebugEnabled())
	    log.debug("Fire PropertyChange " +property+
		      " old value = " +old_value+
		      " new value = " +new_value);
	// Both null: no change
	if (old_value == null && new_value == null) return;

	if (old_value == null || new_value == null // One null, one not
	    || !old_value.equals(new_value) // Different non-nulls
	    )
	    pcs.firePropertyChange(property, old_value, new_value);
    }

    protected void fireContainerChanges(DataFrame old_frame, 
					DataFrame new_frame) {
	// default is no-op
    }

    protected void fireContainerChanges(DataFrame new_frame) {
	// default is no-op
    }

    protected void collectSlotDescriptions(Map<String,SlotDescription> map) {
	// default is no-op
    }

    protected void collectSlotValues(Properties props) {
	// default is no-op
    }

    protected void collectContainerSlotValues(Properties props) {
	// default is no-op
    }

    protected synchronized Object getProperty(String slot) {
	if (props == null) props = new Properties();
	return props.get(slot);
    }

    protected synchronized void setProperty(String slot, Object value) {
	if (props == null) props = new Properties();
	props.put(slot, value);
    }

    protected synchronized void removeProperty(String slot) {
	if (props != null) props.remove(slot);
    }

    protected Object getInheritedValue(Frame origin, String slot) {
	Object result = super.getInheritedValue(origin, slot);
	if (result != null) return result;

	if (frameSet == null) return null;

	Frame container = frameSet.getContainer(this);
	if (container != null) {
	    return container.getValue(slot);
	} else {
	    return null;
	}
    }

    Object getValue(Frame origin, String slot) {
	Object result = getLocalValue(slot);
	if (result != null) 
	    return result;
	else
	    return getInheritedValue(origin, slot);
    }

    void addToFrameSet(FrameSet frameSet) {
	if (this.frameSet != null && this.frameSet != frameSet) {
	    throw new RuntimeException(this +" is already in FrameSet "
				       +this.frameSet+
				       ".  It can't be added to FrameSet "
				       +frameSet);
	} else {
	    this.frameSet = frameSet;
	    frameSet.makeFrame(this);
	    postInitialize();
	}
    }



    void dumpLocalSlots(PrintWriter writer, int indentation, int offset) {
	Map slots = getLocalSlots();
	Iterator itr = slots.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String slot_name = (String) entry.getKey();
	    Object slot_value = entry.getValue();
	    for (int i=0; i<indentation; i++) writer.print(' ');
	    writer.println("<" +slot_name+ ">" +slot_value+ "</"
			   +slot_name+ ">");
	}
    }

    void dump(PrintWriter writer, int indentation, int offset) {
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<frame prototype=\"" +getKind()+ "\">");
	dumpLocalSlots(writer, indentation+offset, offset);
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("</frame>");
    }


    void containerChange(DataFrame old_container, DataFrame new_container) {
	if (log.isDebugEnabled())
	    log.debug(" Old container = " +old_container+
		      " New container = " +new_container);
	if (old_container != null) {
	    // No longer subscribe to changes on old container
	    old_container.removePropertyChangeListener(this);
	}
	if (new_container != null) {
	    // Subscribe to changes on new container.  Must also do
	    // immediate property changes for all container accessors!
	    new_container.addPropertyChangeListener(this);
	    if (old_container != null)
		fireContainerChanges(old_container, new_container);
	    else
		fireContainerChanges(new_container);
	}
    }

}
