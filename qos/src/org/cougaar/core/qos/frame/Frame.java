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


import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.qos.metrics.VariableEvaluator;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * This class is the most abstract representation of a frame and
 * includes almost all of the generic implementation.
 */
abstract public class Frame
    implements UniqueObject, Cloneable, VariableEvaluator
{
    private final UID uid;
    protected transient FrameSet frameSet;
    private static Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.Frame.class);

    Frame(FrameSet frameSet, UID uid)
    {
	this.frameSet = frameSet;
	this.uid = uid;
    }

    abstract public String getKind();
    abstract public Properties getLocalSlots();
    abstract public void setValue(String slot, Object value);
    abstract public boolean isa(String kind);
    abstract Object getValue(Frame origin, String slot);



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


    public PrototypeFrame getPrototype()
    {
	if (frameSet == null) return null;
	PrototypeFrame result = frameSet.getPrototype(this);
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



    // org.cougaar.core.qos.metrics.VariableEvaluator
    public String evaluateVariable(String var)
    {
	return getValue(var).toString();
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

