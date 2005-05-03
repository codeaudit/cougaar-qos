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

import org.cougaar.core.util.UID;

/**
 * This extension to {@link DataFrame} is the basic representation of a
 * frame representing a relationship.
 */
abstract public class RelationFrame
    extends DataFrame 
{
    protected RelationFrame(FrameSet frameSet, UID uid)
    {
	super(frameSet, uid);
    }

    abstract public Object getParentPrototype();
    abstract public Object getParentSlot();
    abstract public Object getParentValue();

    abstract public Object getChildPrototype();
    abstract public Object getChildSlot();
    abstract public Object getChildValue();

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

    private SlotDescription makeMetaDescription(String name, Object value)
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = name;
        __desc.prototype = "relation-frame";
        __desc.is_writable = false;
	__desc.is_overridden = true;
        __desc.value = value;
        return __desc;
    }

    protected void collectSlotDescriptions(java.util.List list)
    {
        super.collectSlotDescriptions(list);
	list.add(makeMetaDescription("parent-prototype", getParentPrototype()));
	list.add(makeMetaDescription("parent-slot", getParentSlot()));
	list.add(makeMetaDescription("child-prototype", getChildPrototype()));
	list.add(makeMetaDescription("child-slot", getChildSlot()));
    }


}

