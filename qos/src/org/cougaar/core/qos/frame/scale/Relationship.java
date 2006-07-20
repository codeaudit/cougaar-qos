package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.RelationFrame;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.util.UID;

public class Relationship
    extends RelationFrame {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Relationship(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "relationship", __fm);
    }
    private String parent_value;
    private String child_value;


    public Relationship(UID uid) {
        this(null, uid);
    }


    public Relationship(FrameSet frameSet,
                        UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "relationship";
    }


    protected void collectSlotValues(java.util.Properties __props) {
        super.collectSlotValues(__props);
        Object __value;
        __value = getParentValue__AsObject();
        __props.put("parent-value", __value != null ? __value : NIL);
        __value = getChildValue__AsObject();
        __props.put("child-value", __value != null ? __value : NIL);
    }


    public String getParentPrototype() {
        return "thing";
    }


    public String getParentSlot() {
        return "name";
    }


    public String getChildPrototype() {
        return "thing";
    }


    public String getChildSlot() {
        return "name";
    }


    public String getParentValue() {
        return parent_value;
    }


    String getParentValue__NoWarn() {
        return parent_value;
    }


    Object getParentValue__AsObject() {
        return parent_value;
    }


    public synchronized void setParentValue(String __new_value) {
        String __old_value = parent_value;
        this.parent_value = __new_value;
        slotModified("parent-value", __old_value, __new_value, true, true);
    }


    public synchronized void setParentValue__AsObject(Object __new_value) {
        Object __old_value = getParentValue__AsObject();
        this.parent_value = force_String(__new_value);
        slotModified("parent-value", __old_value, __new_value, true, true);
    }


    protected void initializeParentValue(String new_value) {
        this.parent_value = new_value;
        slotInitialized("parent-value", new_value);
    }


    void initializeParentValue__AsObject(Object new_value) {
        this.parent_value = force_String(new_value);
        slotInitialized("parent-value", new_value);
    }


    public String getChildValue() {
        return child_value;
    }


    String getChildValue__NoWarn() {
        return child_value;
    }


    Object getChildValue__AsObject() {
        return child_value;
    }


    public synchronized void setChildValue(String __new_value) {
        String __old_value = child_value;
        this.child_value = __new_value;
        slotModified("child-value", __old_value, __new_value, true, true);
    }


    public synchronized void setChildValue__AsObject(Object __new_value) {
        Object __old_value = getChildValue__AsObject();
        this.child_value = force_String(__new_value);
        slotModified("child-value", __old_value, __new_value, true, true);
    }


    protected void initializeChildValue(String new_value) {
        this.child_value = new_value;
        slotInitialized("child-value", new_value);
    }


    void initializeChildValue__AsObject(Object new_value) {
        this.child_value = force_String(new_value);
        slotInitialized("child-value", new_value);
    }
    private static final int parent_value__HashVar__ = "parent-value".hashCode();
    private static final int child_value__HashVar__ = "child-value".hashCode();


    protected Object getLocalValue(String __slot) {
       int __key = __slot.hashCode();
       if (parent_value__HashVar__ == __key)
            return getParentValue__AsObject();
       else if (child_value__HashVar__ == __key)
            return getChildValue__AsObject();
       else
           return null;
    }


    protected void setLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (parent_value__HashVar__ == __key)
            setParentValue__AsObject(__value);
       else if (child_value__HashVar__ == __key)
            setChildValue__AsObject(__value);
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (parent_value__HashVar__ == __key)
            initializeParentValue__AsObject(__value);
       else if (child_value__HashVar__ == __key)
            initializeChildValue__AsObject(__value);
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
        slots.add("parent-value");
        slots.add("child-value");
    }


    public SlotDescription slotMetaData__ParentValue() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "parent-value";
        __desc.prototype = "relationship";
        __desc.is_writable = true;
        Object __value;
        __value = parent_value;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__ChildValue() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "child-value";
        __desc.prototype = "relationship";
        __desc.is_writable = true;
        Object __value;
        __value = child_value;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map<String,SlotDescription> map) {
        super.collectSlotDescriptions(map);
        map.put("parent-value", slotMetaData__ParentValue());
        map.put("child-value", slotMetaData__ChildValue());
    }
}
