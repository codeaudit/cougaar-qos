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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

abstract public class Frame
    implements UniqueObject, Cloneable
{
    private static final Class[] TYPES0 = {};
    private static final Object[] ARGS0 = {};

    private static final Class[] TYPES1 = { Object.class };


    private final UID uid;
    private final String kind;
    private transient FrameSet frameSet;
    private transient Set localSlots;
    private static Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.Frame.class);

    Frame(FrameSet frameSet, String kind, UID uid)
    {
	this.frameSet = frameSet;
	this.uid = uid;
	this.kind = kind;
	this.localSlots = new HashSet();
    }

    protected void addLocalSlot(String slot)
    {
	synchronized (localSlots) {
	    localSlots.add(slot);
	}
    }

    void copyToFrameSet(FrameSet frameSet)
    {
	try {
	    Frame copy = (Frame) this.clone();
	    copy.frameSet = frameSet;
	    frameSet.makeFrame(copy);
	} catch (CloneNotSupportedException ex) {
	    // Is this possible?
	}
    }



    public String toString()
    {
	return "<Frame " +kind+ " " +uid+ ">";
    }

    // Basic accessors

    public boolean isa(String kind)
    {
	return frameSet.descendsFrom(this, kind);
    }

    public Frame relationshipParent()
    {
	return frameSet.getRelationshipParent(this);
    }

    public Frame relationshipChild()
    {
	return frameSet.getRelationshipChild(this);
    }

    public String getKind()
    {
	return kind;
    }

    // Only here for the Tasks servlet
    Properties getLocalSlots()
    {
	Properties props = new VisibleProperties();
	synchronized (localSlots) {
	    Iterator itr = localSlots.iterator();
	    while (itr.hasNext()) {
		String slot =  (String) itr.next();
		Object value = getLocalValue(slot);
		props.put(slot, value);
	    }
	}
	return props;
    }

    public Properties getIndirectSlots()
    {
	Frame parent = getParent();
	if (parent == null) return null;

	Properties props = new VisibleProperties();
	Class klass = parent.getClass();
	try {
	    java.lang.reflect.Method[] meths = klass.getMethods();
	    for (int i=0; i<meths.length; i++) {
		java.lang.reflect.Method meth = meths[i];
		Class ret_type = meth.getReturnType();
		Class[] params = meth.getParameterTypes();
		String name = meth.getName();
		if (name.startsWith("get") &&
		    ret_type == Object.class &&
		    params.length == 0) {
		    Object value = meth.invoke(parent, ARGS0);
		    props.put(name.substring(3), value);
		}
	    }
	} catch (Exception ex) {
	    // do we care?
	}
	return props;
    }

    public String getParentKind()
    {
	Frame parent = getParent();
	return parent == null ? null : parent.getKind();
    }

    Frame getPrototype()
    {
	Frame result = frameSet.getPrototype(this);
	if (result == null) {
	    if (log.isWarnEnabled()) log.warn(this + " has no prototype!");
	}
	return result;
    }

    Frame getParent()
    {
	Frame result = frameSet.getParent(this);
	if (result == null) {
	    if (log.isDebugEnabled()) log.debug(this + " has no parent!");
	}
	return result;
    }

    FrameSet getFrameSet()
    {
	return frameSet;
    }

    boolean matchesSlots(Properties slot_value_pairs)
    {
	Iterator itr = slot_value_pairs.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String slot = (String) entry.getKey();
	    Object value = getValue(slot);
	    if (value == null) return false;
	    if (!value.equals(entry.getValue())) return false;
	}
	return true;
    }

    // These should only be called from the FrameSet owning the
    // frame. 


    private Object getLocalValue(String slot)
    {
	// reflection
	Class klass = getClass();
	String mname = "get" + FrameGen.fix_name(slot, true);
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
	String mname = "set" + FrameGen.fix_name(slot, true);
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

    protected Object getInheritedValue(Frame origin, String slot)
    {
	Frame prototype = frameSet.getPrototype(this);
	if (prototype != null) {
	    Object result = prototype.getValue(origin, slot);
	    if (result != null) return result;
	}
	Frame parent = frameSet.getParent(this);
	if (parent != null) {
	    return parent.getValue(origin, slot);
	}
	return null;
    }


    public void setValue(String slot, Object value)
    {
	setLocalValue(slot, value);
    }

    public void setValues(Properties values)
    {
	Iterator itr = values.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    setLocalValue((String) entry.getKey(), entry.getValue());
	}
    }

    public Object getValue(String slot)
    {
	return getValue(this, slot);
    }

    Object getValue(Frame origin, String slot)
    {
	Object result = getLocalValue(slot);
	if (result != null) 
	    return result;
	else
	    return getInheritedValue(origin, slot);
    }


    public Set findRelations(String role, String relation)
    {
	return frameSet.findRelations(this, role, relation);
    }


    // UniqueObject
    public UID getUID()
    {
	return uid;
    }

    public void setUID(UID uid)
    {
	if (!uid.equals(this.uid))
	    throw new RuntimeException("UID already set");
    }

    // Object
    public boolean equals(Object o) 
    {
	return
	    ((o == this) ||
	     ((o instanceof Frame) &&
	      uid.equals(((Frame) o).uid)));
    }

    public int hashCode() 
    {
	return uid.hashCode();
    }


    public static class VisibleProperties extends Properties
    {
	VisibleProperties()
	{
	    super();
	}
 
	VisibleProperties(Properties properties) 
	{
	    super();
	    putAll(properties);
	}
 
	public String getContents()
	{
	    return this.toString();
	}
    }



    public static class Change implements ChangeReport {
	public final String slot;
	public final Object value;
	public Change(String attr, Object val)
	{
	    this.slot = attr;
	    this.value = val;
	}
	
	public String getSlot() { return slot; }

	public Object getValue() { return value; }

	// Object
	public boolean equals(Object o) 
	{
	    return
		((o == this) ||
		 ((o instanceof Change) &&
		  slot.equals(((Change) o).slot)));
	}

	public int hashCode() 
	{
	    return slot.hashCode();
	}
    }


}

