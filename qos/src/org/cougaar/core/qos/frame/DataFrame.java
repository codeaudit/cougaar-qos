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
//     private Set localSlots;
    private transient PropertyChangeSupport pcs;

    protected DataFrame(FrameSet frameSet, 
			String kind, 
			UID uid)
    {
	super(frameSet, kind, uid);
	this.props = new Properties();
// 	this.localSlots = new HashSet();
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
	// Some frame I depend on has changed (parent only, for now).
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
// 	synchronized (localSlots) {
// 	    Iterator itr = localSlots.iterator();
// 	    while (itr.hasNext()) {
// 		String slot =  (String) itr.next();
// 		Object value = getLocalValue(slot);
// 		if (value != null) props.put(slot, value);
// 	    }
// 	}
	return props;
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

    public String getParentKind()
    {
	Frame parent = parentFrame();
	return parent == null ? null : parent.getKind();
    }

    // Don't use a beany name here...
    public DataFrame parentFrame()
    {
	if (frameSet == null) return null;
	DataFrame result = frameSet.getParent(this);
	if (result == null) {
	    if (log.isDebugEnabled()) log.debug(this + " has no parent!");
	}
	return result;
    }

    public Set findRelations(String role, String relation)
    {
	if (frameSet == null) return null;
	return frameSet.findRelations(this, role, relation);
    }




    // Support

    protected void slotModified(String slot, Object old_value, Object new_value)
    {
// 	synchronized (localSlots) {
// 	    localSlots.add(slot);
// 	}
	if (frameSet != null) frameSet.valueUpdated(this, slot, new_value);
	String fixed_name = FrameGen.fixName(slot, true, true);
	fireChange(fixed_name, old_value, new_value);
    }

    protected void fireChange(String property, 
			      Object old_value, 
			      Object new_value)
    {
	if (log.isInfoEnabled())
	    log.info("Fire PropertyChange " +property+
		      " old value = " +old_value+
		      " new value = " +new_value);
	pcs.firePropertyChange(property, old_value, new_value);
    }

    protected void fireParentChanges(DataFrame old_frame, DataFrame new_frame)
    {
	// no-op at this level
    }

    protected void fireParentChanges(DataFrame new_frame)
    {
	// no-op at this level
    }

    protected void collectSlotValues(Properties props)
    {
	// no-op at this level
    }

    protected void slotInitialized(String slot, Object value)
    {
// 	synchronized (localSlots) {
// 	    localSlots.add(slot);
// 	}
    }

    protected Object getProperty(String slot)
    {
	return props.get(slot);
    }

    protected void setProperty(String slot, Object value)
    {
	props.put(slot, value);
    }

    protected Object getInheritedValue(Frame origin, String slot)
    {
	Object result = super.getInheritedValue(origin, slot);
	if (result != null) return result;

	if (frameSet == null) return null;

	Frame parent = frameSet.getParent(this);
	if (parent != null) {
	    return parent.getValue(slot);
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


    void parentChange(DataFrame old_parent, DataFrame new_parent)
    {
	if (log.isInfoEnabled())
	    log.info(" Old parent = " +old_parent+
		      " New parent = " +new_parent);
	if (old_parent != null) {
	    // No longer subscribe to changes on old parent
	    old_parent.removePropertyChangeListener(this);
	}
	if (new_parent != null) {
	    // Subscribe to changes on new parent.  Must also do
	    // immediate property changes for all parent accessors!
	    new_parent.addPropertyChangeListener(this);
	    if (old_parent != null)
		fireParentChanges(old_parent, new_parent);
	    else
		fireParentChanges(new_parent);
	}
    }


}
