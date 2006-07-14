package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.util.UID;

public class Thing
    extends DataFrame {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Thing(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "thing", __fm);
    }
    private String name;


    public Thing(UID uid) {
        this(null, uid);
    }


    public Thing(FrameSet frameSet,
                 UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "thing";
    }


    protected void collectSlotValues(java.util.Properties __props) {
        super.collectSlotValues(__props);
        Object __value;
        __value = getName__AsObject();
        __props.put("name", __value != null ? __value : NIL);
    }


    public String getName() {
        return name;
    }


    String getName__NoWarn() {
        return name;
    }


    Object getName__AsObject() {
        return name;
    }


    protected void initializeName(String new_value) {
        this.name = new_value;
        slotInitialized("name", new_value);
    }


    void initializeName__AsObject(Object new_value) {
        this.name = force_String(new_value);
        slotInitialized("name", new_value);
    }
    private static final int name__HashVar__ = "name".hashCode();


    protected Object getLocalValue(String __slot) {
       int __key = __slot.hashCode();
       if (name__HashVar__ == __key)
            return getName__AsObject();
       else
           return null;
    }


    protected void setLocalValue(String __slot,
                                 Object __value) {
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (name__HashVar__ == __key)
            initializeName__AsObject(__value);
    }


    public SlotDescription slotMetaData__Name() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "name";
        __desc.prototype = "thing";
        __desc.is_writable = false;
        Object __value;
        __value = name;
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
        map.put("name", slotMetaData__Name());
    }
}
