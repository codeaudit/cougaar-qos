package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.util.UID;

public class Antilevel2
    extends Thing {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Antilevel2(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "antilevel2", __fm);
    }


    public Antilevel2(UID uid) {
        this(null, uid);
    }


    public Antilevel2(FrameSet frameSet,
                      UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "antilevel2";
    }


    protected void collectSlotValues(java.util.Properties __props) {
        super.collectSlotValues(__props);
        Object __value;
        __value = getLevel2Path__AsObject();
        __props.put("level2Path", __value != null ? __value : NIL);
    }


    public Float getLevel2Path() {
        Float __result = (Float) getProperty("level2Path");
        if (__result != null) return __result;
        return (Float) getInheritedValue(this, "level2Path");
    }


    Float getLevel2Path__NoWarn() {
        Float __result = (Float) getProperty("level2Path");
        if (__result != null) return __result;
        return (Float) getInheritedValue(this, "level2Path");
    }


    Object getLevel2Path__AsObject() {
        Object __result = getProperty("level2Path");
        if (__result != null) return __result;
        return getInheritedValue(this, "level2Path");
    }


    public void setLevel2Path(Float __new_value) {
        Float __old_value = (Float) getProperty("level2Path");
        setProperty("level2Path", __new_value);
        slotModified("level2Path", __old_value, __new_value, true, true);
    }


    public void setLevel2Path__AsObject(Object __new_value) {
        Object __old_value = getProperty("level2Path");
        setProperty("level2Path", force_Float(__new_value));
        slotModified("level2Path", __old_value, __new_value, true, true);
    }


    protected void initializeLevel2Path(Float new_value) {
        setProperty("level2Path", new_value);
        slotInitialized("level2Path", new_value);
    }


    void initializeLevel2Path__AsObject(Object new_value) {
        setProperty("level2Path", force_Float(new_value));
        slotInitialized("level2Path", new_value);
    }


    protected void removeLevel2Path() {
        Object __old_value = getProperty("level2Path");
        removeProperty("level2Path");
        slotModified("level2Path", __old_value, getLevel2Path(), true, true);
    }
    private static final int level2Path__HashVar__ = "level2Path".hashCode();


    protected Object getLocalValue(String __slot) {
       int __key = __slot.hashCode();
       if (level2Path__HashVar__ == __key)
            return getLevel2Path__AsObject();
       else
           return super.getLocalValue(__slot);
    }


    protected void setLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (level2Path__HashVar__ == __key)
            setLevel2Path__AsObject(__value);
       else
            super.setLocalValue(__slot, __value);
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (level2Path__HashVar__ == __key)
            initializeLevel2Path__AsObject(__value);
       else
            super.initializeLocalValue(__slot, __value);
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
        slots.add("level2Path");
    }


    public SlotDescription slotMetaData__Level2Path() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level2Path";
        __desc.prototype = "antilevel2";
        __desc.is_writable = true;
        Object __value;
        __value = getProperty("level2Path");
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = getInheritedValue(this, "level2Path");
        }
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map<String,SlotDescription> map) {
        super.collectSlotDescriptions(map);
        map.put("level2Path", slotMetaData__Level2Path());
    }
}
