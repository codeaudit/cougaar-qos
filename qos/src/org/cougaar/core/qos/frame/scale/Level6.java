package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.util.UID;

public class Level6
    extends Thing {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level6(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "level6", __fm);
    }
    private float level6SlotFloat;
    private String level6SlotString;


    public Level6(UID uid) {
        this(null, uid);
    }


    public Level6(FrameSet frameSet,
                  UID uid) {
        super(frameSet, uid);
        initializeLevel6SlotFloat(6.0f);
        initializeLevel6SlotString("Six");
    }


    public String getKind() {
        return "level6";
    }


    protected void collectSlotValues(java.util.Properties __props) {
        super.collectSlotValues(__props);
        Object __value;
        __value = getLevel6SlotFloat__AsObject();
        __props.put("level6SlotFloat", __value != null ? __value : NIL);
        __value = getLevel6SlotString__AsObject();
        __props.put("level6SlotString", __value != null ? __value : NIL);
    }


    protected void collectContainerSlotValues(java.util.Properties __props) {
        super.collectContainerSlotValues(__props);
        Object __value;
        __value = getLevel4SlotFloat__AsObject();
        __props.put("level4SlotFloat", __value != null ? __value : NIL);
        __value = getRootSlotFloat__AsObject();
        __props.put("rootSlotFloat", __value != null ? __value : NIL);
        __value = getLevel1SlotString__AsObject();
        __props.put("level1SlotString", __value != null ? __value : NIL);
        __value = getLevel2SlotString__AsObject();
        __props.put("level2SlotString", __value != null ? __value : NIL);
        __value = getLevel5SlotFloat__AsObject();
        __props.put("level5SlotFloat", __value != null ? __value : NIL);
        __value = getLevel4SlotString__AsObject();
        __props.put("level4SlotString", __value != null ? __value : NIL);
        __value = getLevel2SlotFloat__AsObject();
        __props.put("level2SlotFloat", __value != null ? __value : NIL);
        __value = getLevel3SlotString__AsObject();
        __props.put("level3SlotString", __value != null ? __value : NIL);
        __value = getLevel5SlotString__AsObject();
        __props.put("level5SlotString", __value != null ? __value : NIL);
        __value = getLevel1SlotFloat__AsObject();
        __props.put("level1SlotFloat", __value != null ? __value : NIL);
        __value = getRootSlotString__AsObject();
        __props.put("rootSlotString", __value != null ? __value : NIL);
        __value = getLevel3SlotFloat__AsObject();
        __props.put("level3SlotFloat", __value != null ? __value : NIL);
    }


    public float getLevel6SlotFloat() {
        return level6SlotFloat;
    }


    float getLevel6SlotFloat__NoWarn() {
        return level6SlotFloat;
    }


    Object getLevel6SlotFloat__AsObject() {
        return new Float(level6SlotFloat);
    }


    public void setLevel6SlotFloat(float __new_value) {
        float __old_value = level6SlotFloat;
        this.level6SlotFloat = __new_value;
        slotModified("level6SlotFloat", new Float(__old_value), new Float(__new_value), true, true);
    }


    public void setLevel6SlotFloat__AsObject(Object __new_value) {
        Object __old_value = getLevel6SlotFloat__AsObject();
        this.level6SlotFloat = force_float(__new_value);
        slotModified("level6SlotFloat", __old_value, __new_value, true, true);
    }


    protected void initializeLevel6SlotFloat(float new_value) {
        this.level6SlotFloat = new_value;
        slotInitialized("level6SlotFloat", new Float(new_value));
    }


    void initializeLevel6SlotFloat__AsObject(Object new_value) {
        this.level6SlotFloat = force_float(new_value);
        slotInitialized("level6SlotFloat", new_value);
    }


    public String getLevel6SlotString() {
        return level6SlotString;
    }


    String getLevel6SlotString__NoWarn() {
        return level6SlotString;
    }


    Object getLevel6SlotString__AsObject() {
        return level6SlotString;
    }


    public void setLevel6SlotString(String __new_value) {
        String __old_value = level6SlotString;
        this.level6SlotString = __new_value;
        slotModified("level6SlotString", __old_value, __new_value, true, true);
    }


    public void setLevel6SlotString__AsObject(Object __new_value) {
        Object __old_value = getLevel6SlotString__AsObject();
        this.level6SlotString = force_String(__new_value);
        slotModified("level6SlotString", __old_value, __new_value, true, true);
    }


    protected void initializeLevel6SlotString(String new_value) {
        this.level6SlotString = new_value;
        slotInitialized("level6SlotString", new_value);
    }


    void initializeLevel6SlotString__AsObject(Object new_value) {
        this.level6SlotString = force_String(new_value);
        slotInitialized("level6SlotString", new_value);
    }


    public float getLevel4SlotFloat() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel4SlotFloat();
    }


    Object getLevel4SlotFloat__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel4SlotFloat__AsObject();
    }


    public float getRootSlotFloat() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getRootSlotFloat();
    }


    Object getRootSlotFloat__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getRootSlotFloat__AsObject();
    }


    public String getLevel1SlotString() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel1SlotString();
    }


    Object getLevel1SlotString__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel1SlotString__AsObject();
    }


    public String getLevel2SlotString() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel2SlotString();
    }


    Object getLevel2SlotString__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel2SlotString__AsObject();
    }


    public float getLevel5SlotFloat() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel5SlotFloat();
    }


    Object getLevel5SlotFloat__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel5SlotFloat__AsObject();
    }


    public String getLevel4SlotString() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel4SlotString();
    }


    Object getLevel4SlotString__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel4SlotString__AsObject();
    }


    public float getLevel2SlotFloat() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel2SlotFloat();
    }


    Object getLevel2SlotFloat__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel2SlotFloat__AsObject();
    }


    public String getLevel3SlotString() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel3SlotString();
    }


    Object getLevel3SlotString__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel3SlotString__AsObject();
    }


    public String getLevel5SlotString() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel5SlotString();
    }


    Object getLevel5SlotString__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel5SlotString__AsObject();
    }


    public float getLevel1SlotFloat() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel1SlotFloat();
    }


    Object getLevel1SlotFloat__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel1SlotFloat__AsObject();
    }


    public String getRootSlotString() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getRootSlotString();
    }


    Object getRootSlotString__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getRootSlotString__AsObject();
    }


    public float getLevel3SlotFloat() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Level5))
            throw new RuntimeException("Bogus container!");
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel3SlotFloat();
    }


    Object getLevel3SlotFloat__AsObject() {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_container);
            return null;
       }
       Level5 __container = (Level5) __raw_container;
       return __container.getLevel3SlotFloat__AsObject();
    }


    protected void fireContainerChanges(DataFrame __raw_old, DataFrame __raw_new) {
        if (!(__raw_old instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_old);
            return;
        }
        if (!(__raw_new instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw_new);
            return;
        }
        Level5 __old_frame = (Level5) __raw_old;
        Level5 __new_frame = (Level5) __raw_new;
        Object __old;
        Object __new;
        __old = __old_frame.getLevel4SlotFloat__AsObject();
        __new = __new_frame.getLevel4SlotFloat__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level4SlotFloat", __old, __new);
            }
        }
        __old = __old_frame.getRootSlotFloat__AsObject();
        __new = __new_frame.getRootSlotFloat__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("rootSlotFloat", __old, __new);
            }
        }
        __old = __old_frame.getLevel1SlotString__AsObject();
        __new = __new_frame.getLevel1SlotString__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level1SlotString", __old, __new);
            }
        }
        __old = __old_frame.getLevel2SlotString__AsObject();
        __new = __new_frame.getLevel2SlotString__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level2SlotString", __old, __new);
            }
        }
        __old = __old_frame.getLevel5SlotFloat__AsObject();
        __new = __new_frame.getLevel5SlotFloat__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level5SlotFloat", __old, __new);
            }
        }
        __old = __old_frame.getLevel4SlotString__AsObject();
        __new = __new_frame.getLevel4SlotString__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level4SlotString", __old, __new);
            }
        }
        __old = __old_frame.getLevel2SlotFloat__AsObject();
        __new = __new_frame.getLevel2SlotFloat__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level2SlotFloat", __old, __new);
            }
        }
        __old = __old_frame.getLevel3SlotString__AsObject();
        __new = __new_frame.getLevel3SlotString__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level3SlotString", __old, __new);
            }
        }
        __old = __old_frame.getLevel5SlotString__AsObject();
        __new = __new_frame.getLevel5SlotString__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level5SlotString", __old, __new);
            }
        }
        __old = __old_frame.getLevel1SlotFloat__AsObject();
        __new = __new_frame.getLevel1SlotFloat__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level1SlotFloat", __old, __new);
            }
        }
        __old = __old_frame.getRootSlotString__AsObject();
        __new = __new_frame.getRootSlotString__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("rootSlotString", __old, __new);
            }
        }
        __old = __old_frame.getLevel3SlotFloat__AsObject();
        __new = __new_frame.getLevel3SlotFloat__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("level3SlotFloat", __old, __new);
            }
        }
    }


    protected void fireContainerChanges(DataFrame __raw) {
        if (!(__raw instanceof Level5)) {
            getLogger().warn("Container of " +this+ " is not a Level5: " + __raw);
            return;
        }
        Level5 __new_frame = (Level5) __raw;
        Object __new;
        __new = __new_frame.getLevel4SlotFloat__AsObject();
        if (__new != null) {
            fireChange("level4SlotFloat", null, __new);
        }
        __new = __new_frame.getRootSlotFloat__AsObject();
        if (__new != null) {
            fireChange("rootSlotFloat", null, __new);
        }
        __new = __new_frame.getLevel1SlotString__AsObject();
        if (__new != null) {
            fireChange("level1SlotString", null, __new);
        }
        __new = __new_frame.getLevel2SlotString__AsObject();
        if (__new != null) {
            fireChange("level2SlotString", null, __new);
        }
        __new = __new_frame.getLevel5SlotFloat__AsObject();
        if (__new != null) {
            fireChange("level5SlotFloat", null, __new);
        }
        __new = __new_frame.getLevel4SlotString__AsObject();
        if (__new != null) {
            fireChange("level4SlotString", null, __new);
        }
        __new = __new_frame.getLevel2SlotFloat__AsObject();
        if (__new != null) {
            fireChange("level2SlotFloat", null, __new);
        }
        __new = __new_frame.getLevel3SlotString__AsObject();
        if (__new != null) {
            fireChange("level3SlotString", null, __new);
        }
        __new = __new_frame.getLevel5SlotString__AsObject();
        if (__new != null) {
            fireChange("level5SlotString", null, __new);
        }
        __new = __new_frame.getLevel1SlotFloat__AsObject();
        if (__new != null) {
            fireChange("level1SlotFloat", null, __new);
        }
        __new = __new_frame.getRootSlotString__AsObject();
        if (__new != null) {
            fireChange("rootSlotString", null, __new);
        }
        __new = __new_frame.getLevel3SlotFloat__AsObject();
        if (__new != null) {
            fireChange("level3SlotFloat", null, __new);
        }
    }
    private static final int level5SlotFloat__HashVar__ = "level5SlotFloat".hashCode();
    private static final int level4SlotString__HashVar__ = "level4SlotString".hashCode();
    private static final int level5SlotString__HashVar__ = "level5SlotString".hashCode();
    private static final int level1SlotFloat__HashVar__ = "level1SlotFloat".hashCode();
    private static final int level3SlotFloat__HashVar__ = "level3SlotFloat".hashCode();
    private static final int rootSlotString__HashVar__ = "rootSlotString".hashCode();
    private static final int level4SlotFloat__HashVar__ = "level4SlotFloat".hashCode();
    private static final int level1SlotString__HashVar__ = "level1SlotString".hashCode();
    private static final int rootSlotFloat__HashVar__ = "rootSlotFloat".hashCode();
    private static final int level2SlotString__HashVar__ = "level2SlotString".hashCode();
    private static final int level6SlotFloat__HashVar__ = "level6SlotFloat".hashCode();
    private static final int level2SlotFloat__HashVar__ = "level2SlotFloat".hashCode();
    private static final int level3SlotString__HashVar__ = "level3SlotString".hashCode();
    private static final int name__HashVar__ = "name".hashCode();
    private static final int level6SlotString__HashVar__ = "level6SlotString".hashCode();


    protected Object getLocalValue(String __slot) {
       int __key = __slot.hashCode();
       if (level5SlotFloat__HashVar__ == __key)
            return getLevel5SlotFloat__AsObject();
       else if (level4SlotString__HashVar__ == __key)
            return getLevel4SlotString__AsObject();
       else if (level5SlotString__HashVar__ == __key)
            return getLevel5SlotString__AsObject();
       else if (level1SlotFloat__HashVar__ == __key)
            return getLevel1SlotFloat__AsObject();
       else if (level3SlotFloat__HashVar__ == __key)
            return getLevel3SlotFloat__AsObject();
       else if (rootSlotString__HashVar__ == __key)
            return getRootSlotString__AsObject();
       else if (level4SlotFloat__HashVar__ == __key)
            return getLevel4SlotFloat__AsObject();
       else if (level1SlotString__HashVar__ == __key)
            return getLevel1SlotString__AsObject();
       else if (rootSlotFloat__HashVar__ == __key)
            return getRootSlotFloat__AsObject();
       else if (level2SlotString__HashVar__ == __key)
            return getLevel2SlotString__AsObject();
       else if (level6SlotFloat__HashVar__ == __key)
            return getLevel6SlotFloat__AsObject();
       else if (level2SlotFloat__HashVar__ == __key)
            return getLevel2SlotFloat__AsObject();
       else if (level3SlotString__HashVar__ == __key)
            return getLevel3SlotString__AsObject();
       else if (name__HashVar__ == __key)
            return getName__AsObject();
       else if (level6SlotString__HashVar__ == __key)
            return getLevel6SlotString__AsObject();
       else
           return super.getLocalValue(__slot);
    }


    protected void setLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (level6SlotFloat__HashVar__ == __key)
            setLevel6SlotFloat__AsObject(__value);
       else if (level6SlotString__HashVar__ == __key)
            setLevel6SlotString__AsObject(__value);
       else
            super.setLocalValue(__slot, __value);
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (level6SlotFloat__HashVar__ == __key)
            initializeLevel6SlotFloat__AsObject(__value);
       else if (level6SlotString__HashVar__ == __key)
            initializeLevel6SlotString__AsObject(__value);
       else
            super.initializeLocalValue(__slot, __value);
    }


    public SlotDescription slotMetaData__Level6SlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level6SlotFloat";
        __desc.prototype = "level6";
        __desc.is_writable = true;
        Object __value;
        __value = new Float(level6SlotFloat);
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = new Float(6.0f);
        }
        return __desc;
    }


    public SlotDescription slotMetaData__Level6SlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level6SlotString";
        __desc.prototype = "level6";
        __desc.is_writable = true;
        Object __value;
        __value = level6SlotString;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = "Six";
        }
        return __desc;
    }


    public SlotDescription slotMetaData__Level4SlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level4SlotFloat";
        __desc.prototype = "level4";
        __desc.value = getLevel4SlotFloat__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
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


    public SlotDescription slotMetaData__Level1SlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level1SlotString";
        __desc.prototype = "level1";
        __desc.value = getLevel1SlotString__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Level2SlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level2SlotString";
        __desc.prototype = "level2";
        __desc.value = getLevel2SlotString__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Level5SlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level5SlotFloat";
        __desc.prototype = "level5";
        __desc.value = getLevel5SlotFloat__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Level4SlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level4SlotString";
        __desc.prototype = "level4";
        __desc.value = getLevel4SlotString__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Level2SlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level2SlotFloat";
        __desc.prototype = "level2";
        __desc.value = getLevel2SlotFloat__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Level3SlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level3SlotString";
        __desc.prototype = "level3";
        __desc.value = getLevel3SlotString__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Level5SlotString() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level5SlotString";
        __desc.prototype = "level5";
        __desc.value = getLevel5SlotString__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Level1SlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level1SlotFloat";
        __desc.prototype = "level1";
        __desc.value = getLevel1SlotFloat__AsObject();
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


    public SlotDescription slotMetaData__Level3SlotFloat() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "level3SlotFloat";
        __desc.prototype = "level3";
        __desc.value = getLevel3SlotFloat__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map<String,SlotDescription> map) {
        super.collectSlotDescriptions(map);
        map.put("level6SlotFloat", slotMetaData__Level6SlotFloat());
        map.put("level6SlotString", slotMetaData__Level6SlotString());
        map.put("level4SlotFloat", slotMetaData__Level4SlotFloat());
        map.put("rootSlotFloat", slotMetaData__RootSlotFloat());
        map.put("level1SlotString", slotMetaData__Level1SlotString());
        map.put("level2SlotString", slotMetaData__Level2SlotString());
        map.put("level5SlotFloat", slotMetaData__Level5SlotFloat());
        map.put("level4SlotString", slotMetaData__Level4SlotString());
        map.put("level2SlotFloat", slotMetaData__Level2SlotFloat());
        map.put("level3SlotString", slotMetaData__Level3SlotString());
        map.put("level5SlotString", slotMetaData__Level5SlotString());
        map.put("level1SlotFloat", slotMetaData__Level1SlotFloat());
        map.put("rootSlotString", slotMetaData__RootSlotString());
        map.put("level3SlotFloat", slotMetaData__Level3SlotFloat());
    }
}
