package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.util.UID;

public class MsgIndicator
    extends Indicator
{
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new MsgIndicator(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "msgIndicator", __fm);
    }
    private double calmThreshold;
    private double idleThreshold;
    private double normalThreshold;
    private double franticThreshold;
    private double busyThreshold;


    public MsgIndicator(UID uid)
    {
        this(null, uid);
    }


    public MsgIndicator(FrameSet frameSet,
                        UID uid)
    {
        super(frameSet, uid);
        initializeCalmThreshold(1.0);
        initializeIdleThreshold(0.0);
        initializeNormalThreshold(10.0);
        initializeFranticThreshold(1000.0);
        initializeBusyThreshold(100.0);
    }


    public String getKind()
    {
        return "msgIndicator";
    }


    protected void collectSlotValues(java.util.Properties __props)
    {
        super.collectSlotValues(__props);
        Object __value;
        __value = getCalmThreshold__AsObject();
        __props.put("calmThreshold", __value != null ? __value : NIL);
        __value = getIdleThreshold__AsObject();
        __props.put("idleThreshold", __value != null ? __value : NIL);
        __value = getNormalThreshold__AsObject();
        __props.put("normalThreshold", __value != null ? __value : NIL);
        __value = getFranticThreshold__AsObject();
        __props.put("franticThreshold", __value != null ? __value : NIL);
        __value = getBusyThreshold__AsObject();
        __props.put("busyThreshold", __value != null ? __value : NIL);
    }


    public double getCalmThreshold()
    {
        return calmThreshold;
    }


    double getCalmThreshold__NoWarn()
    {
        return calmThreshold;
    }


    Object getCalmThreshold__AsObject()
    {
        return new Double(calmThreshold);
    }


    public void setCalmThreshold(double __new_value)
    {
        double __old_value = calmThreshold;
        this.calmThreshold = __new_value;
        slotModified("calmThreshold", new Double(__old_value), new Double(__new_value), true, true);
    }


    public void setCalmThreshold__AsObject(Object __new_value)
    {
        Object __old_value = getCalmThreshold__AsObject();
        this.calmThreshold = force_double(__new_value);
        slotModified("calmThreshold", __old_value, __new_value, true, true);
    }


    protected void initializeCalmThreshold(double new_value)
    {
        this.calmThreshold = new_value;
        slotInitialized("calmThreshold", new Double(new_value));
    }


    void initializeCalmThreshold__AsObject(Object new_value)
    {
        this.calmThreshold = force_double(new_value);
        slotInitialized("calmThreshold", new_value);
    }


    public double getIdleThreshold()
    {
        return idleThreshold;
    }


    double getIdleThreshold__NoWarn()
    {
        return idleThreshold;
    }


    Object getIdleThreshold__AsObject()
    {
        return new Double(idleThreshold);
    }


    public void setIdleThreshold(double __new_value)
    {
        double __old_value = idleThreshold;
        this.idleThreshold = __new_value;
        slotModified("idleThreshold", new Double(__old_value), new Double(__new_value), true, true);
    }


    public void setIdleThreshold__AsObject(Object __new_value)
    {
        Object __old_value = getIdleThreshold__AsObject();
        this.idleThreshold = force_double(__new_value);
        slotModified("idleThreshold", __old_value, __new_value, true, true);
    }


    protected void initializeIdleThreshold(double new_value)
    {
        this.idleThreshold = new_value;
        slotInitialized("idleThreshold", new Double(new_value));
    }


    void initializeIdleThreshold__AsObject(Object new_value)
    {
        this.idleThreshold = force_double(new_value);
        slotInitialized("idleThreshold", new_value);
    }


    public double getNormalThreshold()
    {
        return normalThreshold;
    }


    double getNormalThreshold__NoWarn()
    {
        return normalThreshold;
    }


    Object getNormalThreshold__AsObject()
    {
        return new Double(normalThreshold);
    }


    public void setNormalThreshold(double __new_value)
    {
        double __old_value = normalThreshold;
        this.normalThreshold = __new_value;
        slotModified("normalThreshold", new Double(__old_value), new Double(__new_value), true, true);
    }


    public void setNormalThreshold__AsObject(Object __new_value)
    {
        Object __old_value = getNormalThreshold__AsObject();
        this.normalThreshold = force_double(__new_value);
        slotModified("normalThreshold", __old_value, __new_value, true, true);
    }


    protected void initializeNormalThreshold(double new_value)
    {
        this.normalThreshold = new_value;
        slotInitialized("normalThreshold", new Double(new_value));
    }


    void initializeNormalThreshold__AsObject(Object new_value)
    {
        this.normalThreshold = force_double(new_value);
        slotInitialized("normalThreshold", new_value);
    }


    public double getFranticThreshold()
    {
        return franticThreshold;
    }


    double getFranticThreshold__NoWarn()
    {
        return franticThreshold;
    }


    Object getFranticThreshold__AsObject()
    {
        return new Double(franticThreshold);
    }


    public void setFranticThreshold(double __new_value)
    {
        double __old_value = franticThreshold;
        this.franticThreshold = __new_value;
        slotModified("franticThreshold", new Double(__old_value), new Double(__new_value), true, true);
    }


    public void setFranticThreshold__AsObject(Object __new_value)
    {
        Object __old_value = getFranticThreshold__AsObject();
        this.franticThreshold = force_double(__new_value);
        slotModified("franticThreshold", __old_value, __new_value, true, true);
    }


    protected void initializeFranticThreshold(double new_value)
    {
        this.franticThreshold = new_value;
        slotInitialized("franticThreshold", new Double(new_value));
    }


    void initializeFranticThreshold__AsObject(Object new_value)
    {
        this.franticThreshold = force_double(new_value);
        slotInitialized("franticThreshold", new_value);
    }


    public double getBusyThreshold()
    {
        return busyThreshold;
    }


    double getBusyThreshold__NoWarn()
    {
        return busyThreshold;
    }


    Object getBusyThreshold__AsObject()
    {
        return new Double(busyThreshold);
    }


    public void setBusyThreshold(double __new_value)
    {
        double __old_value = busyThreshold;
        this.busyThreshold = __new_value;
        slotModified("busyThreshold", new Double(__old_value), new Double(__new_value), true, true);
    }


    public void setBusyThreshold__AsObject(Object __new_value)
    {
        Object __old_value = getBusyThreshold__AsObject();
        this.busyThreshold = force_double(__new_value);
        slotModified("busyThreshold", __old_value, __new_value, true, true);
    }


    protected void initializeBusyThreshold(double new_value)
    {
        this.busyThreshold = new_value;
        slotInitialized("busyThreshold", new Double(new_value));
    }


    void initializeBusyThreshold__AsObject(Object new_value)
    {
        this.busyThreshold = force_double(new_value);
        slotInitialized("busyThreshold", new_value);
    }
    private static final int calmThreshold__HashVar__ = "calmThreshold".hashCode();
    private static final int idleThreshold__HashVar__ = "idleThreshold".hashCode();
    private static final int normalThreshold__HashVar__ = "normalThreshold".hashCode();
    private static final int franticThreshold__HashVar__ = "franticThreshold".hashCode();
    private static final int busyThreshold__HashVar__ = "busyThreshold".hashCode();


    protected Object getLocalValue(String __slot)
    {
       int __key = __slot.hashCode();
       if (calmThreshold__HashVar__ == __key)
            return getCalmThreshold__AsObject();
       else if (idleThreshold__HashVar__ == __key)
            return getIdleThreshold__AsObject();
       else if (normalThreshold__HashVar__ == __key)
            return getNormalThreshold__AsObject();
       else if (franticThreshold__HashVar__ == __key)
            return getFranticThreshold__AsObject();
       else if (busyThreshold__HashVar__ == __key)
            return getBusyThreshold__AsObject();
       else
           return super.getLocalValue(__slot);
    }


    protected void setLocalValue(String __slot,
                                 Object __value)
    {
       int __key = __slot.hashCode();
       if (calmThreshold__HashVar__ == __key)
            setCalmThreshold__AsObject(__value);
       else if (idleThreshold__HashVar__ == __key)
            setIdleThreshold__AsObject(__value);
       else if (normalThreshold__HashVar__ == __key)
            setNormalThreshold__AsObject(__value);
       else if (franticThreshold__HashVar__ == __key)
            setFranticThreshold__AsObject(__value);
       else if (busyThreshold__HashVar__ == __key)
            setBusyThreshold__AsObject(__value);
       else
            super.setLocalValue(__slot, __value);
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value)
    {
       int __key = __slot.hashCode();
       if (calmThreshold__HashVar__ == __key)
            initializeCalmThreshold__AsObject(__value);
       else if (idleThreshold__HashVar__ == __key)
            initializeIdleThreshold__AsObject(__value);
       else if (normalThreshold__HashVar__ == __key)
            initializeNormalThreshold__AsObject(__value);
       else if (franticThreshold__HashVar__ == __key)
            initializeFranticThreshold__AsObject(__value);
       else if (busyThreshold__HashVar__ == __key)
            initializeBusyThreshold__AsObject(__value);
       else
            super.initializeLocalValue(__slot, __value);
    }


    public SlotDescription slotMetaData__CalmThreshold()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "calmThreshold";
        __desc.prototype = "msgIndicator";
        __desc.is_writable = true;
        Object __value;
        __value = new Double(calmThreshold);
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = new Double(1.0);
        }
        return __desc;
    }


    public SlotDescription slotMetaData__IdleThreshold()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "idleThreshold";
        __desc.prototype = "msgIndicator";
        __desc.is_writable = true;
        Object __value;
        __value = new Double(idleThreshold);
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = new Double(0.0);
        }
        return __desc;
    }


    public SlotDescription slotMetaData__NormalThreshold()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "normalThreshold";
        __desc.prototype = "msgIndicator";
        __desc.is_writable = true;
        Object __value;
        __value = new Double(normalThreshold);
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = new Double(10.0);
        }
        return __desc;
    }


    public SlotDescription slotMetaData__FranticThreshold()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "franticThreshold";
        __desc.prototype = "msgIndicator";
        __desc.is_writable = true;
        Object __value;
        __value = new Double(franticThreshold);
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = new Double(1000.0);
        }
        return __desc;
    }


    public SlotDescription slotMetaData__BusyThreshold()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "busyThreshold";
        __desc.prototype = "msgIndicator";
        __desc.is_writable = true;
        Object __value;
        __value = new Double(busyThreshold);
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = new Double(100.0);
        }
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map map)
    {
        super.collectSlotDescriptions(map);
        map.put("calmThreshold", slotMetaData__CalmThreshold());
        map.put("idleThreshold", slotMetaData__IdleThreshold());
        map.put("normalThreshold", slotMetaData__NormalThreshold());
        map.put("franticThreshold", slotMetaData__FranticThreshold());
        map.put("busyThreshold", slotMetaData__BusyThreshold());
    }
}
