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
import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UID;


public class SingleInheritanceFrameSet
    implements FrameSet
{
    private final static String PROTOTYPE = "frame::prototype";

    private UIDService uids;
    private BlackboardService bbs;
    private HashSet kb;
    private HashMap prototypes, parents;
    private String 
	parent_relation,
	parent_kind_slot,
	parent_slot_slot,
	parent_value_slot,
	child_kind_slot,
	child_slot_slot,
	child_value_slot;
    

    public SingleInheritanceFrameSet(ServiceBroker sb,
				     BlackboardService bbs,
				     String parent_relation,
				     String parent_kind_slot,
				     String parent_slot_slot,
				     String parent_value_slot,
				     String child_kind_slot,
				     String child_slot_slot,
				     String child_value_slot)
    {
	this.bbs = bbs;
	uids = (UIDService)
	    sb.getService(this, UIDService.class, null);

	this.kb = new HashSet();
	this.prototypes = new HashMap();
	this.parents = new HashMap();

	// The kind tag of Frames representing a parent-child relationship
	this.parent_relation = parent_relation;

	// Any given Frame of this kind will have three slots each,
	// for the parent and child respectively: a kind, a slot, and
	// value.  The names of these six slots in the relation Frame
	// are given here

	this.parent_kind_slot = parent_kind_slot;
	this.parent_slot_slot = parent_slot_slot;
	this.parent_value_slot = parent_value_slot;

	this.child_kind_slot = child_kind_slot;
	this.child_slot_slot = child_slot_slot;
	this.child_value_slot = child_value_slot;
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
	// **** TBD ****
	// handle the modification of parent-child relationship frames

	if (bbs != null) bbs.publishChange(frame);
    }

    public Frame makeFrame(Frame frame)
    {
	String kind = frame.getKind();
	Properties values = frame.cloneValues();
	return makeFrame(kind, values);
    }

    public Frame makeFrame(String kind, Properties values)
    {
	UID uid = uids.nextUID();
	Frame result = new Frame(kind, uid, values);

	if (kind.equals(parent_relation)) {
	    // cache a parent-child relationship
	    String parent_kind = (String)
		result.getValue(parent_kind_slot);
	    String parent_slot = (String)
		result.getValue(parent_slot_slot);
	    Object parent_value = result.getValue(parent_value_slot);

	    String child_kind = (String)
		result.getValue(child_kind_slot);
	    String child_slot = (String)
		result.getValue(child_slot_slot);
	    Object child_value = result.getValue(child_value_slot);

	    Frame parent = findFrame(parent_kind, parent_slot, parent_value);
	    Frame child = findFrame(child_kind, child_slot, child_value);
	    synchronized (parents) {
		parents.put(child, parent);
	    }
	}
	addFrame(result);
	return result;
    }

    // In this case the kind argument refers to what the prototype
    // should be a prototype of.  The prototype Frame itself always
    // has a kind of PROTOTYPE.
    public Frame makePrototype(String kind, Properties values)
    {
	UID uid = uids.nextUID();
	Frame result = new Frame(PROTOTYPE, uid, values);
	synchronized (prototypes) { prototypes.put(kind, result); }
	addFrame(result);
	return result;
    }

    private void addFrame(Frame frame)
    {
	synchronized (kb) {
	    kb.add(frame);
	}
	if (bbs != null) bbs.publishAdd(frame);
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
	// ***** TBD *****
	// Handle the removal of parent-child relationship frames


	if (bbs != null) bbs.publishRemove(frame);
    }

    private Frame getParent(Frame frame)
    {
	synchronized (parents) {
	    return (Frame) parents.get(frame);
	}
    }

    private Frame getPrototype(Frame frame)
    {
	synchronized (prototypes) {
	    return (Frame) prototypes.get(frame.getKind());
	}
    }


}
