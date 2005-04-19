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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
    private static final Object[] ARGS0 = {};
    private static final Class[] TYPES0 = {};
    private static final Class[] TYPES1 = { Object.class };


    private static transient Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.DataFrame.class);

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

    private static final Class[] CTYPES = { FrameSet.class, 
					    String.class,
					    UID.class};

    public static DataFrame newFrame(String pkg,
				     FrameSet frameSet,
				     String proto, 
				     UID uid,
				     Properties values)
    {
	// use reflection here!
	Class klass = frameSet.classForPrototype(proto);
	if (klass == null) return null;

	DataFrame frame = null;
	Object[] args = { frameSet, proto, uid };
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
    private transient PropertyChangeSupport pcs;

    protected DataFrame(FrameSet frameSet, 
			String kind, 
			UID uid)
    {
	super(frameSet, kind, uid);
	this.props = new Properties();
	this.pcs = new PropertyChangeSupport(this);
    }

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
    public Properties getLocalSlots()
    {
	Properties props = new VisibleProperties();
	collectSlotValues(props);
	return props;
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
	    initializeLocalValue((String) entry.getKey(), entry.getValue());
	}
    }

    public DataFrame relationshipParent()
    {
	if (frameSet == null) return null;
	return frameSet.getRelationshipParent(this);
    }

    public DataFrame relationshipChild()
    {
	if (frameSet == null) return null;
	return frameSet.getRelationshipChild(this);
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

    public boolean isa(String kind)
    {
	if (frameSet == null) return kind.equals(getKind());
	return frameSet.descendsFrom(this, kind);
    }

    public Properties getAllSlots()
    {
	Properties props = new Properties();
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

    protected void slotModified(String slot, Object old_value, Object new_value)
    {
	if (frameSet != null) frameSet.valueUpdated(this, slot, new_value);
	String fixed_name = FrameGen.fixName(slot, true, true);
	fireChange(fixed_name, old_value, new_value);
    }


    protected void slotInitialized(String slot, Object value)
    {
	// nothing to be done at the moment
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

    protected void collectSlotValues(Properties props)
    {
	// default is no-op
    }

    protected Object getProperty(String slot)
    {
	return props.get(slot);
    }

    protected void setProperty(String slot, Object value)
    {
	props.put(slot, value);
    }

    protected void removeProperty(String slot)
    {
	props.remove(slot);
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


    private Object getLocalValue(String slot)
    {
	// reflection
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

    private void setLocalValue(String slot, Object value)
    {
	// reflection
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

    private Object removeLocalValue(String slot)
    {
	// reflection
	Class klass = getClass();
	String mname = "remove" + FrameGen.fixName(slot, true);
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

    private void initializeLocalValue(String slot, Object value)
    {
	// reflection
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
