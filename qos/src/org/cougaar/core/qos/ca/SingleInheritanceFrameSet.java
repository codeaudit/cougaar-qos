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

import java.util.HashMap;
import java.util.HashSet;


public class SingleInheritanceFrameSet
    implements FrameSet
{
    private HashSet kb;
    private Frame parent_relation;
    private HashMap prototypes, parents;
    private String parent_relation_name,
	parent_slot,
	child_slot,
	parent_kind,
	child_kind,
	parent_id_slot,
	child_id_slot;

    public SingleInheritanceFrameSet(Frame parent_relation)
    {
	this.kb = new HashSet();
	this.prototypes = new HashMap();
	this.parents = new HashMap();
	this.parent_relation = parent_relation;
	// The parent_relation frame must look like this:
	// (parent-relation (name <relationship-name>)
	//                  (parent-slot-name <slot-name>)
	//                  (child-slot-name <slot-name>)
	//                  (parent-kind <kind-name>)
	//                  (child-kind <kind-name>)
	//                  (parent-identifier-slot-name <slot-name>)
	//                  (child-identifier-slot-name <slot-name>))
	// 
	// Cache these values for faster access
	this.parent_relation_name = (String)
	    parent_relation.getValue("name");
	this.parent_slot = (String)
	    parent_relation.getValue("parent-slot-name");
	this.child_slot = (String)
	    parent_relation.getValue("child-slot-name");
	this.parent_kind = (String) parent_relation.getValue("parent-kind");
	this.child_kind = (String) parent_relation.getValue("child-kind");
	this.parent_id_slot = (String)
	    parent_relation.getValue("parent-identifier-slot-name");
	this.child_id_slot = (String)
	    parent_relation.getValue("parent-identifier-slot-name");
    }

    public Frame findFrame(String kind, String slot, Object value)
    {
	return null;  // TBD
    }

    public Object getFrameValue(Frame frame, String attribute)
    {
	Object result = frame.getValue(attribute);
	if (result != null) return result;
	Frame parent = getParent(frame);
	if (parent != null) {
	    result = getFrameValue(parent, attribute);
	    if (result != null) return result;
	}
	Frame prototype = getPrototype(frame);
	if (prototype != null) {
	    return prototype.getValue(attribute);
	}

	return null;
    }

    public void setFrameValue(Frame frame, String attribute, Object value)
    {
	frame.setValue(attribute, value);
	// handle the modification of parent-child relationship frames
    }

    public void addFrame(Frame frame)
    {
	synchronized (kb) {
	    kb.add(frame);
	}
	if (frame.getKind().equals(PROTOTYPE)) {
	    // cache a prototype
	    String name = (String) frame.getValue("name");
	    synchronized (prototypes) { prototypes.put(name, frame); }
	} else if (frame.getKind().equals(parent_relation_name)) {
	    // cache a parent-child relationship
	    Object parent_id = frame.getValue(parent_slot);
	    Object child_id = frame.getValue(child_slot);
	    Frame parent = findFrame(parent_kind, parent_id_slot, parent_id);
	    Frame child = findFrame(child_kind, child_id_slot, child_id);
	    synchronized (parents) {
		parents.put(child, parent);
	    }
	}
    }

    public void removeFrame(Frame frame)
    {
	synchronized (kb) {
	    kb.remove(frame);
	}
	if (frame.getKind().equals(PROTOTYPE)) {
	    String name = (String) frame.getValue("name");
	    synchronized (prototypes) { prototypes.remove(name); }
	}
	// handle the removal of parent-child relationship frames
    }

    public Frame getParent(Frame frame)
    {
	synchronized (parents) {
	    return (Frame) parents.get(frame);
	}
    }

    public Frame getPrototype(Frame frame)
    {
	synchronized (prototypes) {
	    return (Frame) prototypes.get(frame.getKind());
	}
    }


}
