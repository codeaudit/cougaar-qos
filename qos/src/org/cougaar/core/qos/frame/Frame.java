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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * This class is the most abstract representation of a frame and
 * includes almost all of the generic implementation.
 */
abstract public class Frame
    implements UniqueObject, Cloneable
{
    private static final Class[] TYPES0 = {};
    private static final Object[] ARGS0 = {};

    private static final Class[] TYPES1 = { Object.class };


    private final UID uid;
    private final String kind;
    protected transient FrameSet frameSet;
    private static Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.Frame.class);

    Frame(FrameSet frameSet, String kind, UID uid)
    {
	this.frameSet = frameSet;
	this.uid = uid;
	this.kind = kind;
    }

    abstract public Properties getLocalSlots();
    abstract Object getValue(Frame origin, String slot);
    abstract public boolean isa(String kind);

    public Frame copy()
    {
	try {
	    Frame copy = (Frame) this.clone();
	    copy.frameSet = null;
	    return copy;
	} catch (CloneNotSupportedException ex) {
	    log.error(null, ex);
	    return null;
	}
    }

    public String toString()
    {
	return "<Frame " +kind+ " " +uid+ ">";
    }

    // Basic accessors

    public String getKind()
    {
	return kind;
    }


    Frame getPrototype()
    {
	if (frameSet == null) return null;
	Frame result = frameSet.getPrototype(this);
	if (result == null) {
	    if (log.isWarnEnabled()) log.warn(this + " has no prototype!");
	}
	return result;
    }

    public FrameSet getFrameSet()
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

    protected Object getInheritedValue(Frame origin, String slot)
    {
	if (frameSet == null) return null;
	Frame prototype = frameSet.getPrototype(this);
	if (prototype == null)  return null;
	return prototype.getValue(origin, slot);
    }

    Object getLocalValue(String slot)
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

    void setLocalValue(String slot, Object value)
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

    Object removeLocalValue(String slot)
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

    public Object getValue(String slot)
    {
	return getValue(this, slot);
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

    /**
     * This trivial extension of Properties has a bean-ish
     * reader method listing the contents as a string (handy for the
     * {@link FrameViewerServlet}).
     */
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



    /**
     * This extension of ChangeReport is used to describe a change to
     * a Frame.  Collections of these are included with frame changes
     * published to the Blackboard.
     */
    public static class Change implements ChangeReport {
	private final String slot;
	private final Object value;
	private final UID frame_uid;
	private final int hashcode;

	public Change(UID frame_uid, String attr, Object val)
	{
	    this.slot = attr;
	    this.value = val;
	    this.frame_uid = frame_uid;
	    String puid = frame_uid.toString() +attr+ val.toString();
	    this.hashcode = puid.hashCode();
	}
	
	public String getSlotName() { return slot; }

	public Object getValue() { return value; }
	
	public UID getFrameUID() { return frame_uid; }

	// Object
	public boolean equals(Object o) 
	{
	    return
		((o == this) ||
		 ((o instanceof Change) &&
		  hashcode == (((Change) o).hashcode)));
	}

	public int hashCode() 
	{
	    return hashcode;
	}
    }


}

