/*
 * Generated by Cougaar QoS FrameGen
 *   from /Projects/cougaar-cvs/HEAD/qos/src/org/cougaar/core/qos/frame/scale/test-scale-protos.xml
 *   at Aug 1, 2006 8:51:12 PM
 *
 * Copyright BBN Technologies 2006
 *
 */
package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.util.UID;

public class Level1
    extends Thing {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level1(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "level1", __fm);
    }
    private String level1SlotString;
    private float level1SlotFloat;


    public Level1(UID uid) {
        this(null, uid);
    }


    public Level1(FrameSet frameSet,
                  UID uid) {
        super(frameSet, uid);
        initializeLevel1SlotString("One");
        initializeLevel1SlotFloat(1.0f);
    }


    public String getKind() {
        return "level1";
    }


    protected void collectSlotValues(java.util.Properties __props) {
        super.collectSlotValues(__props);
        Object __value;
        __value = getLevel1SlotString__AsObject();
        __props.put("level1SlotString", __value != null ? __value : NIL);
        __value = getLevel1SlotFloat__AsObject();
        __props.put("level1SlotFloat", __value != null ? __value : NIL);
    }


    protected void collectContainerSlotValues(java.util.Properties __props) {
        super.collectContainerSlotValues(__props);
        Object __value;
        __value = getRootSlotFloat__AsObject();
        __props.put("rootSlotFloat", __value != null ? __value : NIL);
        __value = getRootSlotString__AsObject();
        __props.put("rootSlotString", __value != null ? __value : NIL);
    }


    public String getLevel1SlotString() {
        return level1SlotString;
    }


    String getLevel1SlotString__NoWarn() {
        return level1SlotString;
    }


    Object getLevel1SlotString__AsObject() {
        return level1SlotString;
    }


    public void setLevel1SlotString(String __new_value) {
        String __old_value = level1SlotString;
        this.level1SlotString = __new_value;
        slotModified("level1SlotString", __old_value, __new_value, true, true);
    }


    public void setLevel1SlotString__AsObject(Object __new_value) {
        Object __old_value = getLevel1SlotString__AsObject();
        this.level1SlotString = force_String(__new_value);
        slotModified("level1SlotString", __old_value, __new_value, true, true);
    }


    protected void initializeLevel1SlotString(String new_value) {
        this.level1SlotString = new_value;
        slotInitialized("level1SlotString", new_value);
    }


    void initializeLevel1SlotString__AsObject(Object new_value) {
        this.level1SlotString = force_String(new_value);
        slotInitialized("level1SlotString", new_value);
    }


    public float getLevel1SlotFloat() {
        return level1SlotFloat;
    }


    float getLevel1SlotFloat__NoWarn() {
        return level1SlotFloat;
    }


    Object getLevel1SlotFloat__AsObject() {
        return new Float(level1SlotFloat);
    }


    public void setLevel1SlotFloat(float __new_value) {
        float __old_value = level1SlotFloat;
        this.level1SlotFloat = __new_value;
        slotModified("level1SlotFloat", new Float(__old_value), new Float(__new_value), true, true);
    }


    public void setLevel1SlotFloat__AsObject(Object __new_value) {
        Object __old_value = getLevel1SlotFloat__AsObject();
        this.level1SlotFloat = force_float(__new_value);
        slotModified("level1SlotFloat", __old_value, __new_value, true, true);
    }


    protected void initializeLevel1SlotFloat(float new_value) {
        this.level1SlotFloat = new_value;
        slotInitialized("level1SlotFloat", new Float(new_value));
    }


    void initializeLevel1SlotFloat__AsObject(Object new_value) {
        this.level1SlotFloat = force_float(new_value);
        slotInitialized("level1SlotFloat", new_value);
    }


    public float getRootSlotFloat() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Root))
            throw new RuntimeException("Bogus container!");
       Root __container = (Root) __raw_container;
       return __container.getRootSlotFloat();
    }


    Object getRootSlotFloat__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Root)) {
            getLogger().warn("Container of " +this+ " is not a Root: " + __raw_container);
            return null;
       }
       Root __container = (Root) __raw_container;
       return __container.getRootSlotFloat__AsObject();
    }


    public String getRootSlotString() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Root))
            throw new RuntimeException("Bogus container!");
       Root __container = (Root) __raw_container;
       return __container.getRootSlotString();
    }


    Object getRootSlotString__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Root)) {
            getLogger().warn("Container of " +this+ " is not a Root: " + __raw_container);
            return null;
       }
       Root __container = (Root) __raw_container;
       return __container.getRootSlotString__AsObject();
    }


    protected void fireContainerChanges(DataFrame __raw_old, DataFrame __raw_new) {
        if (!(__raw_old instanceof Root)) {
            getLogger().warn("Container of " +this+ " is not a Root: " + __raw_old);
            return;
        }
        if (!(__raw_new instanceof Root)) {
            getLogger().warn("Container of " +this+ " is not a Root: " + __raw_new);
            return;
        }
        Root __old_frame = (Root) __raw_old;
        Root __new_frame = (Root) __raw_new;
        Object __old;
        Object __new;
        __old = __old_frame.getRootSlotFloat__AsObject();
        __new = __new_frame.getRootSlotFloat__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("rootSlotFloat", __old, __new);
            }
        }
        __old = __old_frame.getRootSlotString__AsObject();
        __new = __new_frame.getRootSlotString__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("rootSlotString", __old, __new);
            }
        }
    }


    protected void fireContainerChanges(DataFrame __raw) {
        if (!(__raw instanceof Root)) {
            getLogger().warn("Container of " +this+ " is not a Root: " + __raw);
            return;
        }
        Root __new_frame = (Root) __raw;
        Object __new;
        __new = __new_frame.getRootSlotFloat__AsObject();
        if (__new != null) {
            fireChange("rootSlotFloat", null, __new);
        }
        __new = __new_frame.getRootSlotString__AsObject();
        if (__new != null) {
            fireChange("rootSlotString", null, __new);
        }
    }


    protected Object getLocalValue(String __slot) {
       String __key = __slot.intern();
       if ("level1SlotString" == __key)
            return getLevel1SlotString__AsObject();
       else if ("rootSlotFloat" == __key)
            return getRootSlotFloat__AsObject();
       else if ("level1SlotFloat" == __key)
            return getLevel1SlotFloat__AsObject();
       else if ("name" == __key)
            return getName__AsObject();
       else if ("rootSlotString" == __key)
            return getRootSlotString__AsObject();
       else
           return super.getLocalValue(__slot);
    }


    protected void setLocalValue(String __slot,
                                 Object __value) {
       String __key = __slot.intern();
       if ("level1SlotString" == __key)
            setLevel1SlotString__AsObject(__value);
       else if ("level1SlotFloat" == __key)
            setLevel1SlotFloat__AsObject(__value);
       else
            super.setLocalValue(__slot, __value);
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value) {
       String __key = __slot.intern();
       if ("level1SlotString" == __key)
            initializeLevel1SlotString__AsObject(__value);
       else if ("level1SlotFloat" == __key)
            initializeLevel1SlotFloat__AsObject(__value);
       else
            super.initializeLocalValue(__slot, __value);
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
        slots.add("level1SlotString");
        slots.add("level1SlotFloat");
        slots.add("rootSlotFloat");
        slots.add("rootSlotString");
    }


    public SlotDescription slotMetaData__Level1SlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level1SlotString";
        __desc.prototype = "level1";
        __desc.is_writable = true;
        Object __value;
        __value = level1SlotString;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = "One";
        }
        return __desc;
    }


    public SlotDescription slotMetaData__Level1SlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level1SlotFloat";
        __desc.prototype = "level1";
        __desc.is_writable = true;
        Object __value;
        __value = new Float(level1SlotFloat);
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = new Float(1.0f);
        }
        return __desc;
    }


    public SlotDescription slotMetaData__RootSlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "rootSlotFloat";
        __desc.prototype = "root";
        __desc.value = getRootSlotFloat__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__RootSlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "rootSlotString";
        __desc.prototype = "root";
        __desc.value = getRootSlotString__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map<String,SlotDescription> map) {
        super.collectSlotDescriptions(map);
        map.put("level1SlotString", slotMetaData__Level1SlotString());
        map.put("level1SlotFloat", slotMetaData__Level1SlotFloat());
        map.put("rootSlotFloat", slotMetaData__RootSlotFloat());
        map.put("rootSlotString", slotMetaData__RootSlotString());
    }
}
