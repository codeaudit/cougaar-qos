package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.util.UID;

public class MsgOutIndicator
    extends MsgIndicator {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new MsgOutIndicator(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "msgOutIndicator", __fm);
    }
    private String watchSlot;


    public MsgOutIndicator(UID uid) {
        this(null, uid);
    }


    public MsgOutIndicator(FrameSet frameSet,
                           UID uid) {
        super(frameSet, uid);
        initializeWatchSlot("msgOut");
    }


    public String getKind() {
        return "msgOutIndicator";
    }


    protected void collectSlotValues(java.util.Properties __props) {
        super.collectSlotValues(__props);
        Object __value;
        __value = getWatchSlot__AsObject();
        __props.put("watchSlot", __value != null ? __value : NIL);
    }


    public String getWatchSlot() {
        return watchSlot;
    }


    String getWatchSlot__NoWarn() {
        return watchSlot;
    }


    Object getWatchSlot__AsObject() {
        return watchSlot;
    }


    public void setWatchSlot(String __new_value) {
        String __old_value = watchSlot;
        this.watchSlot = __new_value;
        slotModified("watchSlot", __old_value, __new_value, true, true);
    }


    public void setWatchSlot__AsObject(Object __new_value) {
        Object __old_value = getWatchSlot__AsObject();
        this.watchSlot = force_String(__new_value);
        slotModified("watchSlot", __old_value, __new_value, true, true);
    }


    protected void initializeWatchSlot(String new_value) {
        this.watchSlot = new_value;
        slotInitialized("watchSlot", new_value);
    }


    void initializeWatchSlot__AsObject(Object new_value) {
        this.watchSlot = force_String(new_value);
        slotInitialized("watchSlot", new_value);
    }
    private static final int watchSlot__HashVar__ = "watchSlot".hashCode();


    protected Object getLocalValue(String __slot) {
       int __key = __slot.hashCode();
       if (watchSlot__HashVar__ == __key)
            return getWatchSlot__AsObject();
       else
           return super.getLocalValue(__slot);
    }


    protected void setLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (watchSlot__HashVar__ == __key)
            setWatchSlot__AsObject(__value);
       else
            super.setLocalValue(__slot, __value);
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value) {
       int __key = __slot.hashCode();
       if (watchSlot__HashVar__ == __key)
            initializeWatchSlot__AsObject(__value);
       else
            super.initializeLocalValue(__slot, __value);
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
        slots.add("watchSlot");
    }


    public SlotDescription slotMetaData__WatchSlot() {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "watchSlot";
        __desc.prototype = "msgOutIndicator";
        __desc.is_writable = true;
        Object __value;
        __value = watchSlot;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = "msgOut";
        }
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map<String,SlotDescription> map) {
        super.collectSlotDescriptions(map);
        map.put("watchSlot", slotMetaData__WatchSlot());
    }
}
