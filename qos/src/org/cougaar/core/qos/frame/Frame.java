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

import java.util.Properties;


import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;

public final class Frame
    implements UniqueObject
{
    private final UID uid;
    private final String kind;
    private VisibleProperties properties;

    Frame(String kind, UID uid, Properties properties)
    {
	this.uid = uid;
	this.kind = kind;
	this.properties = new VisibleProperties(properties);
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

    // Only here for the Tasks servlet
    public VisibleProperties getProperties()
    {
	return properties;
    }

    // These should only be called from the FrameSet owning the
    // frame. 

    void setValue(String attribute, Object object)
    {
	properties.put(attribute, object);
    }


    Object getValue(String attribute)
    {
	return properties.get(attribute);
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


    // Hack for servlet
    public static class VisibleProperties extends Properties
    {
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
	public final String attribute;
	public final Object value;
	public Change(String attr, Object val)
	{
	    this.attribute = attr;
	    this.value = val;
	}
	
	public String getAttribute() { return attribute; }

	public Object getValue() { return value; }

	// Object
	public boolean equals(Object o) 
	{
	    return
		((o == this) ||
		 ((o instanceof Change) &&
		  attribute.equals(((Change) o).attribute)));
	}

	public int hashCode() 
	{
	    return attribute.hashCode();
	}
    }


}

