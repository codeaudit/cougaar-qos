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
    implements PropertyChangeListener
{
    // Used only by the reflective methods. none of which are called
    // anymore.
    private static final Object[] ARGS0 = {};
    private static final Class[] TYPES0 = {};
    private static final Class[] TYPES1 = { Object.class };
    private static final Class[] CTYPES = { FrameSet.class, UID.class};

    public static final String NIL = "NIL";
    
    private static HashMap FramePackages = new HashMap();;
    private static Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.DataFrame.class);


    protected static void registerFrameMaker(String pkg,
					     String proto,
					     FrameMaker maker)
    {
	if (log.isDebugEnabled())
	    log.debug("Registering FrameMaker " +maker+ " for prototype "
		      +proto+ " in package " +pkg);
	synchronized (FramePackages) {
	    HashMap frameTypeMap = (HashMap) FramePackages.get(pkg);
	    if (frameTypeMap == null) {
		frameTypeMap = new HashMap();
		FramePackages.put(pkg, frameTypeMap);
	    }
	    frameTypeMap.put(proto, maker);
	}
    }

    protected static FrameMaker findFrameMaker(String pkg, String proto)
    {
	synchronized (FramePackages) {
	    HashMap frameTypeMap = (HashMap) FramePackages.get(pkg);
	    if (frameTypeMap != null) 
		return (FrameMaker) frameTypeMap.get(proto);
	    else
		return null;
	}
    }

    protected static Logger getLogger()
    {
	return log;
    }


    public static DataFrame newFrame(FrameSet frameSet,
				     String proto, 
				     UID uid,
				     Properties initial_values)
    {
	String pkg = frameSet.getPackageName();
	return newFrame(pkg, frameSet, proto, uid, initial_values);
    }


    public static DataFrame newFrame(String pkg,
				     FrameSet frameSet,
				     String proto, 
				     UID uid,
				     Properties values)
    {
	if (log.isDebugEnabled())
	    log.debug("Searching for FrameMaker for prototype "
		      +proto+ " in package " +pkg);
	// force a class load (ugh)
	Class klass = frameSet.classForPrototype(proto);
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

    

    // Not used anymore, here for documentation
    private static DataFrame newFrameReflective(String pkg,
						FrameSet frameSet,
						String proto, 
						UID uid,
						Properties values)
    {
	Class klass = frameSet.classForPrototype(proto);
	if (klass == null) return null;

	DataFrame frame = null;
	Object[] args = { frameSet, uid };
	try {
	    java.lang.reflect.Constructor cons = klass.getConstructor(CTYPES);
	    frame = (DataFrame) cons.newInstance(args);
	    if (log.isInfoEnabled())
		log.info("Made frame " +frame);
	} catch (Exception ex) {
	    log.error("Error making frame", ex);
	    return null;
	}
	frame.initializeValues(values);
	return frame;
    }




    private Properties props;
    private transient HashMap ddeps = new HashMap();
    private transient HashMap rdeps = new HashMap();
    private transient HashMap path_dependents = new HashMap();
    private transient Object rlock = new Object();
    private transient PropertyChangeSupport pcs;

    protected DataFrame(FrameSet frameSet, UID uid)
    {
	super(frameSet, uid);
	this.pcs = new PropertyChangeSupport(this);
    }

    // Subclass responsibility
    abstract protected Object getLocalValue(String slot);
    abstract protected void setLocalValue(String slot, Object value);
//     abstract protected void removeLocalValue(String slot);
    abstract protected void initializeLocalValue(String slot, Object value);



    // Serialization
    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	pcs = new PropertyChangeSupport(this);
    }

    // PropertyChangeListener

    public void propertyChange(PropertyChangeEvent event)
    {
	// Some frame I depend on has changed (container only, for now).
	// Resignal to my listeners.
	if (log.isInfoEnabled())
	    log.info("Propagate PropertyChange " +event.getPropertyName()+
		      " old value = " +event.getOldValue()+
		      " new value = " +event.getNewValue());
	pcs.firePropertyChange(event);
    }

    // Path dependencies.


    Object get_rlock()
    {
	return 	rlock;
    }


    // Caller should lock rlock
    void clearRelationDependencies(String slot)
    {
	if (log.isInfoEnabled())
	    log.info("Clearing relation dependencies of " +this+
		     " slot " +slot);
	List rframes = (List) rdeps.get(slot);
	if (rframes == null) return;
	int count = rframes.size();
	for (int i=0; i<count; i++) {
	    RelationFrame rframe = (RelationFrame) rframes.get(i);
	    rframe.removePathDependent(this, slot);
	}
	rdeps.remove(slot);
	DataFrame dframe = (DataFrame) ddeps.get(slot);
	dframe.removePathDependent(this, slot);
	ddeps.remove(slot);
    }

    // Caller should lock rlock
    void addRelationSlotDependency(DataFrame frame, String slot)
    {
	if (log.isInfoEnabled())
	    log.info("Clearing relation dependencies of " +this+
		     " for slot " +slot);
	ddeps.put(slot, frame);
	frame.addPathDependent(this, slot);
    }

    // Caller should lock rlock
    void addRelationDependency(RelationFrame rframe, String slot)
    {
	if (log.isInfoEnabled())
	    log.info("Adding relation dependency " +rframe+ " on " +this+
		     " slot " +slot);
	List rframes = (List) rdeps.get(slot);
	if (rframes == null) {
	    rframes = new ArrayList();
	    rdeps.put(slot, rframes);
	}
	rframes.add(rframe);
	rframe.addPathDependent(this, slot);
    }

    // Caller should lock rlock
    void removeRelationDependency(RelationFrame rframe, String slot)
    {
	if (log.isInfoEnabled())
	    log.info("Removing relation dependency " +rframe+ " on " +this+
		     " for slot " +slot);
	List rframes = (List) rdeps.get(slot);
	if (rframes != null) rframes.remove(rframe);
	rframe.removePathDependent(this, slot);
    }

    void pathDependencyChange(String slot)
    {
	if (log.isInfoEnabled())
	    log.info("Path dependency has changed for frame " +this+
		      " and for slot " +slot);
	Object new_value = getValue(slot);
	slotModified(slot, null, new_value, true, true);
    }

    void addPathDependent(DataFrame dependent, String slot)
    {
	if (log.isInfoEnabled())
	    log.info(this + " is adding dependent " +dependent+
		     " for slot " +slot);
	synchronized (path_dependents) {
	    Set dependents = (Set)  path_dependents.get(slot);
	    if (dependents == null) {
		dependents = new HashSet();
		path_dependents.put(slot, dependents);
	    }
	    dependents.add(dependent);
	}
    }

    void removePathDependent(DataFrame dependent, String slot)
    {
	if (log.isInfoEnabled())
	    log.info(this + " is removing dependent " +dependent+
		     " for slot " +slot);
	synchronized (path_dependents) {
	    Set dependents = (Set) path_dependents.get(slot);
	    if (dependents != null) {
		dependents.remove(dependent);
	    }
	}
    }

    void notifyPathDependents(String slot)
    {
	if (log.isInfoEnabled())
	    log.info(" Notify dependents of " +this+
		     " for slot " +slot);
	synchronized (path_dependents) {
	    Set dependents = (Set) path_dependents.get(slot);
	    if (dependents != null) {
		Iterator itr = dependents.iterator();
		while (itr.hasNext()) {
		    DataFrame frame = (DataFrame) itr.next();
		    frame.pathDependencyChange(slot);
		}
	    }
	}
    }

    void notifyAllPathDependents()
    {
	if (log.isInfoEnabled())
	    log.info(" Notify all dependents of " +this);
	synchronized (path_dependents) {
	    Iterator itr = path_dependents.entrySet().iterator();
	    while (itr.hasNext()) {
		Map.Entry entry = (Map.Entry) itr.next();
		String slot = (String) entry.getKey();
		Set dependents = (Set) entry.getValue();
		Iterator itr2 = dependents.iterator();
		while (itr.hasNext()) {
		    DataFrame frame = (DataFrame) itr.next();
		    frame.pathDependencyChange(slot);
		}
	    }
	}
    }
    


    // Jess ShadowFact
    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
	pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl)
    {
	pcs.removePropertyChangeListener(pcl);
    }


    
    // Public accesssors

    // In this context "local" means: include slots the prototype tree
    // but not from the container tree.
    public Properties getLocalSlots()
    {
	Properties props = new VisibleProperties();
	collectSlotValues(props);
	return props;
    }

    public Properties getAllSlots()
    {
	Properties props = new VisibleProperties();
	collectSlotValues(props);
	collectContainerSlotValues(props);
	return props;
    }

    public Map slotDescriptions()
    {
	HashMap descriptions = new HashMap();
	collectSlotDescriptions(descriptions);
	return descriptions;
    }

    public void setValue(String slot, Object value)
    {
	setLocalValue(slot, value);
    }

    public void initializeValues(Properties values)
    {
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

    public String getContainerKind()
    {
	Frame container = containerFrame();
	return container == null ? null : container.getKind();
    }

    // Don't use a beany name here...
    public DataFrame containerFrame()
    {
	if (frameSet == null) return null;
	DataFrame result = frameSet.getContainer(this);
	if (result == null) {
	    if (log.isDebugEnabled()) log.debug(this + " has no container!");
	}
	return result;
    }

    public Set findRelations(String role, String relation)
    {
	if (frameSet == null) return null;
	return frameSet.findRelations(this, role, relation);
    }

    public Map findRelationshipFrames(String role, String relation)
    {
	if (frameSet == null) return null;
	return frameSet.findRelationshipFrames(this, role, relation);
    }

    public boolean isa(String kind)
    {
	if (frameSet == null) return kind.equals(getKind());
	return frameSet.descendsFrom(this, kind);
    }

    // Not used, here for documentation
    private Properties getAllSlotsReflective()
    {
	Properties props = new VisibleProperties();
	Class klass = getClass();
	java.lang.reflect.Method[] methods = klass.getMethods();
	for (int i=0; i<methods.length; i++) {
	    java.lang.reflect.Method meth = methods[i];
	    Class rtype = meth.getReturnType();
	    Class[] ptypes = meth.getParameterTypes();
	    String name = meth.getName();
	    if (rtype == Object.class && ptypes.length == 0 && name.startsWith("get")) {
		try {
		    Object value = meth.invoke(this, ARGS0);
		    if (value != null) {
			String attr_name = name.substring(3);
			props.put(attr_name, value);
		    }
		} catch (Exception ex) {
		    if (log.isWarnEnabled())
			log.warn("Couldn't invoke " +name+ " on " +this);
		}
	    }
	}
	return props;
    }





    // Support

    protected void slotModified(String slot, 
				Object old_value, 
				Object new_value,
				boolean notify_listeners,
				boolean notify_blackboard)
    {
	if (notify_blackboard && frameSet != null) 
	    frameSet.valueUpdated(this, slot, new_value);
	if (notify_listeners) {
	    String fixed_name = FrameGen.fixName(slot, true, true);
	    fireChange(fixed_name, old_value, new_value);
	    notifyPathDependents(slot);
	}
    }


    protected void slotInitialized(String slot, Object value)
    {
	// nothing to be done at the moment
    }

    protected void postInitialize()
    {
	// Nothing at this level.  Frame types with Metric-value slots
	// should subscribe to the MetricsService here
    }

    //  Converters used in generated code
    protected String force_String(Object x)
    {
	if (x instanceof String)
	    return ((String) x);
	else
	    return x.toString(); // hmm
    }

    protected Metric force_Metric(Object x)
    {
	if (x instanceof Metric)
	    return ((Metric) x);
	else
	    throw new RuntimeException(x + " cannot be coerced to a Metric");
    }

    protected double force_double(Object x)
    {
	if (x instanceof String)
	    return Double.parseDouble((String) x);
	else if (x instanceof Double)
	    return ((Double) x).doubleValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a double");
    }

    protected Double force_Double(Object x)
    {
	if (x instanceof String) {
	    return Double.valueOf((String) x);
	} else if (x instanceof Double) {
	    return (Double) x;
	} else {
	    throw new RuntimeException(x + " cannot be coerced to a Double");
	}
    }

    protected float force_float(Object x)
    {
	if (x instanceof String)
	    return Float.parseFloat((String) x);
	else if (x instanceof Float)
	    return ((Float) x).floatValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a float");
    }

    protected Float force_Float(Object x)
    {
	if (x instanceof String)
	    return Float.valueOf((String) x);
	else if (x instanceof Float)
	    return (Float) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Float");
    }


    protected long force_long(Object x)
    {
	if (x instanceof String)
	    return Long.parseLong((String) x);
	else if (x instanceof Long)
	    return ((Long) x).longValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a long");
    }


    protected Long force_Long(Object x)
    {
	if (x instanceof String)
	    return Long.valueOf((String) x);
	else if (x instanceof Long)
	    return (Long) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Long");
    }


    protected int force_int(Object x)
    {
	if (x instanceof String)
	    return Integer.parseInt((String) x);
	else if (x instanceof Integer)
	    return ((Integer) x).intValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to an int");
    }

    protected Integer force_Integer(Object x)
    {
	if (x instanceof String)
	    return Integer.valueOf((String) x);
	else if (x instanceof Integer)
	    return (Integer) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Long");
    }


    protected boolean force_boolean(Object x)
    {
	if (x instanceof String)
	    return ((String) x).equalsIgnoreCase("true");
	else if (x instanceof Boolean)
	    return ((Boolean) x).booleanValue();
	else
	    throw new RuntimeException(x + " cannot be coerced to a boolean");
    }


    protected Boolean force_Boolean(Object x)
    {
	if (x instanceof String)
	    return Boolean.valueOf((String) x);
	else if (x instanceof Boolean)
	    return (Boolean) x;
	else
	    throw new RuntimeException(x + " cannot be coerced to a Long");
    }




    protected void fireChange(String property, 
			      Object old_value, 
			      Object new_value)
    {
	if (log.isInfoEnabled())
	    log.info("Fire PropertyChange " +property+
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
					DataFrame new_frame)
    {
	// default is no-op
    }

    protected void fireContainerChanges(DataFrame new_frame)
    {
	// default is no-op
    }

    protected void collectSlotDescriptions(Map map)
    {
	// default is no-op
    }

    protected void collectSlotValues(Properties props)
    {
	// default is no-op
    }

    protected void collectContainerSlotValues(Properties props)
    {
	// default is no-op
    }

    protected synchronized Object getProperty(String slot)
    {
	if (props == null) props = new Properties();
	return props.get(slot);
    }

    protected synchronized void setProperty(String slot, Object value)
    {
	if (props == null) props = new Properties();
	props.put(slot, value);
    }

    protected synchronized void removeProperty(String slot)
    {
	if (props != null) props.remove(slot);
    }

    protected Object getInheritedValue(Frame origin, String slot)
    {
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

    Object getValue(Frame origin, String slot)
    {
	Object result = getLocalValue(slot);
	if (result != null) 
	    return result;
	else
	    return getInheritedValue(origin, slot);
    }

    void addToFrameSet(FrameSet frameSet)
    {
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



    void dumpLocalSlots(PrintWriter writer, int indentation, int offset)
    {
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

    void dump(PrintWriter writer, int indentation, int offset)
    {
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<frame prototype=\"" +getKind()+ "\">");
	dumpLocalSlots(writer, indentation+offset, offset);
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("</frame>");
    }


    void containerChange(DataFrame old_container, DataFrame new_container)
    {
	if (log.isInfoEnabled())
	    log.info(" Old container = " +old_container+
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



    
    // The following four methods are no longer used and are kept here
    // for documentation purposes.
    private Object getLocalValueReflective(String slot)
    {
	Class klass = getClass();
	String mname = "get" + FrameGen.fixName(slot, true);
	try {
	    java.lang.reflect.Method meth = klass.getMethod(mname, TYPES0);
	    Object result = meth.invoke(this, ARGS0);
	    if (log.isInfoEnabled())
		log.info("Slot " +slot+ " of " +this+ " = " +result);
	    return result;
	} catch (Exception ex) {
	    // This is not necessarily an error.  It could mean one of
	    // our children was supposed to have this value and
	    // didn't, so it asked us.
	    if (log.isInfoEnabled())
		log.info("Couldn't get slot " +slot+ " of " +this+
			  " via " +mname);
	    return null;
	}
    }

    private void setLocalValueReflective(String slot, Object value)
    {
	Class klass = getClass();
	String mname = "set" + FrameGen.fixName(slot, true);
	try {
	    java.lang.reflect.Method meth = klass.getMethod(mname, TYPES1);
	    Object[] args1 = { value };
	    meth.invoke(this, args1);
	    if (log.isInfoEnabled())
		log.info("Set slot " +slot+ " of " +this+ " to " + value);
	} catch (Exception ex) {
	    log.error("Error setting slot " +slot+ " of " +this+
		      " via " +mname);
	}
    }


    private void removeLocalValueReflective(String slot)
    {
	Class klass = getClass();
	String mname = "remove" + FrameGen.fixName(slot, true);
	try {
	    java.lang.reflect.Method meth = klass.getMethod(mname, TYPES0);
	    meth.invoke(this, ARGS0);
	    if (log.isInfoEnabled())
		log.info("Removed value of slot " +slot);
	} catch (Exception ex) {
	    if (log.isInfoEnabled())
		log.info("Couldn't remove value of slot " +slot+ " of " +this+
			  " via " +mname);
	}
    }

    private void initializeLocalValueReflective(String slot, Object value)
    {
	Class klass = getClass();
	String mname = "initialize" + FrameGen.fixName(slot, true);
	try {
	    java.lang.reflect.Method meth = klass.getMethod(mname, TYPES1);
	    Object[] args1 = { value };
	    meth.invoke(this, args1);
	    if (log.isInfoEnabled())
		log.info("Initializing slot " +slot+ " of " +this+ 
			 " to " + value);
	} catch (Exception ex) {
	    log.error("Error initializing slot " +slot+ " of " +this+
		      " via " +mname);
	}
    }


}
