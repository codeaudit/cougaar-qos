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

package org.cougaar.core.qos.ca;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple representation of a fact, likely to be replaced later by
 * Jess facts.  This implementation is for immutable facts.
 */
public class Fact implements Serializable
{
    private String type;
    private HashMap attributes;

    public Fact(String type, HashMap attributes)
    {
	this.type = type;
	this.attributes = attributes;
    }

    public Fact(Fact fact, HashMap updates)
    {
	this.type = fact.type;
	this.attributes = (HashMap) fact.attributes.clone();
	this.attributes.putAll(updates);
    }

    public String getType()
    {
	return type;
    }

    public Object getAttribute(String key)
    {
	return attributes.get(key);
    }

    public String toString()
    {
	return "<Fact " +type+ " [" +attributes.size()+ "]>";
    }

    public Iterator attributes()
    {
	return attributes.entrySet().iterator();
    }

    public String debugString()
    {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Fact ");
	buffer.append(type);
	buffer.append('\n');
	Iterator itr = attributes();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    buffer.append(entry.getKey());
	    buffer.append('=');
	    buffer.append(entry.getValue());
	    buffer.append('\n');
	}
	return buffer.toString();
    }
}

