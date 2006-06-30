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

import java.util.Map;

import org.cougaar.core.util.UID;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * This extension to {@link DataFrame} is the basic representation of a
 * frame representing a relationship.
 */
abstract public class RelationFrame
    extends DataFrame 
{
    private static Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.RelationFrame.class);

    // This is only here to suppress useless warnings when the
    // frameset fails to find one of the relations two operands.
    private transient long failed_lookup_time;

    long failed_lookup_time()
    {
	long now = System.currentTimeMillis();
	if (failed_lookup_time == 0) failed_lookup_time = now;
	return now - failed_lookup_time;
    }

    void clear_failed_lookup_time()
    {
	failed_lookup_time = 0;
    }
	


    protected RelationFrame(FrameSet frameSet, UID uid)
    {
	super(frameSet, uid);
    }

    abstract public String getParentPrototype();
    abstract public String getParentSlot();
    abstract public String getParentValue();

    abstract public String getChildPrototype();
    abstract public String getChildSlot();
    abstract public String getChildValue();

    // Path dependencies.

    void notifyPathDependents(String slot)
    {
	notifyAllPathDependents();
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

    protected void collectSlotValues(java.util.Properties __props)
    {
        super.collectSlotValues(__props);
	Object __value;
        __value = getParentPrototype();
        __props.put("parent-prototype", __value != null ? __value : NIL);
        __value = getChildPrototype();
        __props.put("child-prototype", __value != null ? __value : NIL);
        __value = getParentSlot();
        __props.put("parent-slot", __value != null ? __value : NIL);
        __value = getChildSlot();
        __props.put("child-slot", __value != null ? __value : NIL);
    }

    private void makeMetaDescription(String name, Object value,	Map map)
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = name;
        __desc.prototype = "relation-frame";
        __desc.is_writable = false;
	__desc.is_overridden = true;
        __desc.value = value;
	map.put(name, __desc);
    }

    protected void collectSlotDescriptions(Map map)
    {
        super.collectSlotDescriptions(map);
	makeMetaDescription("parent-prototype", getParentPrototype(), map);
	makeMetaDescription("parent-slot", getParentSlot(), map);
	makeMetaDescription("child-prototype", getChildPrototype(), map);
	makeMetaDescription("child-slot", getChildSlot(), map);
    }


}

