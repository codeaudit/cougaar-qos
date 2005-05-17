package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.util.UID;

public class Host
    extends Thing
{
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Host(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "host", __fm);
    }
    private transient Metric loadAverage;
    private transient Metric jips;
    private transient Metric effectiveMJips;
    private transient Metric count;


    public Host(UID uid)
    {
        this(null, uid);
    }


    public Host(FrameSet frameSet,
                UID uid)
    {
        super(frameSet, uid);
    }


    public String getKind()
    {
        return "host";
    }


    protected void collectSlotValues(java.util.Properties __props)
    {
        super.collectSlotValues(__props);
        Object __value;
        __value = getLoadAverage__AsObject();
        __props.put("loadAverage", __value != null ? __value : NIL);
        __value = getJips__AsObject();
        __props.put("jips", __value != null ? __value : NIL);
        __value = getEffectiveMJips__AsObject();
        __props.put("effectiveMJips", __value != null ? __value : NIL);
        __value = getCount__AsObject();
        __props.put("count", __value != null ? __value : NIL);
    }


    public Metric getLoadAverage()
    {
        return loadAverage;
    }


    Metric getLoadAverage__NoWarn()
    {
        return loadAverage;
    }


    Object getLoadAverage__AsObject()
    {
        return loadAverage;
    }


    protected void initializeLoadAverage(Metric new_value)
    {
        this.loadAverage = new_value;
        slotInitialized("loadAverage", new_value);
    }


    void initializeLoadAverage__AsObject(Object new_value)
    {
        this.loadAverage = force_Metric(new_value);
        slotInitialized("loadAverage", new_value);
    }


    public Metric getJips()
    {
        return jips;
    }


    Metric getJips__NoWarn()
    {
        return jips;
    }


    Object getJips__AsObject()
    {
        return jips;
    }


    protected void initializeJips(Metric new_value)
    {
        this.jips = new_value;
        slotInitialized("jips", new_value);
    }


    void initializeJips__AsObject(Object new_value)
    {
        this.jips = force_Metric(new_value);
        slotInitialized("jips", new_value);
    }


    public Metric getEffectiveMJips()
    {
        return effectiveMJips;
    }


    Metric getEffectiveMJips__NoWarn()
    {
        return effectiveMJips;
    }


    Object getEffectiveMJips__AsObject()
    {
        return effectiveMJips;
    }


    protected void initializeEffectiveMJips(Metric new_value)
    {
        this.effectiveMJips = new_value;
        slotInitialized("effectiveMJips", new_value);
    }


    void initializeEffectiveMJips__AsObject(Object new_value)
    {
        this.effectiveMJips = force_Metric(new_value);
        slotInitialized("effectiveMJips", new_value);
    }


    public Metric getCount()
    {
        return count;
    }


    Metric getCount__NoWarn()
    {
        return count;
    }


    Object getCount__AsObject()
    {
        return count;
    }


    protected void initializeCount(Metric new_value)
    {
        this.count = new_value;
        slotInitialized("count", new_value);
    }


    void initializeCount__AsObject(Object new_value)
    {
        this.count = force_Metric(new_value);
        slotInitialized("count", new_value);
    }
    private static final int loadAverage__HashVar__ = "loadAverage".hashCode();
    private static final int jips__HashVar__ = "jips".hashCode();
    private static final int effectiveMJips__HashVar__ = "effectiveMJips".hashCode();
    private static final int count__HashVar__ = "count".hashCode();


    protected Object getLocalValue(String __slot)
    {
       int __key = __slot.hashCode();
       if (loadAverage__HashVar__ == __key)
            return getLoadAverage__AsObject();
       else if (jips__HashVar__ == __key)
            return getJips__AsObject();
       else if (effectiveMJips__HashVar__ == __key)
            return getEffectiveMJips__AsObject();
       else if (count__HashVar__ == __key)
            return getCount__AsObject();
       else
           return super.getLocalValue(__slot);
    }


    protected void setLocalValue(String __slot,
                                 Object __value)
    {
       int __key = __slot.hashCode();
       super.setLocalValue(__slot, __value);
    }


    protected void initializeLocalValue(String __slot,
                                 Object __value)
    {
       int __key = __slot.hashCode();
       if (loadAverage__HashVar__ == __key)
            initializeLoadAverage__AsObject(__value);
       else if (jips__HashVar__ == __key)
            initializeJips__AsObject(__value);
       else if (effectiveMJips__HashVar__ == __key)
            initializeEffectiveMJips__AsObject(__value);
       else if (count__HashVar__ == __key)
            initializeCount__AsObject(__value);
       else
            super.initializeLocalValue(__slot, __value);
    }


    protected void postInitialize()
    {
        super.postInitialize();
        java.util.Observer __observer;
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = loadAverage;
                loadAverage = (Metric) __new;
                slotModified("loadAverage", __old, __new, true, true);
            }
        };
        loadAverage = getFrameSet().getMetricValue(this, "Host($(name)):LoadAverage()");
        getFrameSet().subscribeToMetric(this, __observer, "Host($(name)):LoadAverage()");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = jips;
                jips = (Metric) __new;
                slotModified("jips", __old, __new, true, true);
            }
        };
        jips = getFrameSet().getMetricValue(this, "Host($(name)):Jips()");
        getFrameSet().subscribeToMetric(this, __observer, "Host($(name)):Jips()");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = effectiveMJips;
                effectiveMJips = (Metric) __new;
                slotModified("effectiveMJips", __old, __new, true, true);
            }
        };
        effectiveMJips = getFrameSet().getMetricValue(this, "Host($(name)):EffectiveMJips()");
        getFrameSet().subscribeToMetric(this, __observer, "Host($(name)):EffectiveMJips()");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = count;
                count = (Metric) __new;
                slotModified("count", __old, __new, true, true);
            }
        };
        count = getFrameSet().getMetricValue(this, "Host($(name)):Count()");
        getFrameSet().subscribeToMetric(this, __observer, "Host($(name)):Count()");
    }


    public SlotDescription slotMetaData__LoadAverage()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "loadAverage";
        __desc.prototype = "host";
        __desc.is_writable = false;
        Object __value;
        __value = loadAverage;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__Jips()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "jips";
        __desc.prototype = "host";
        __desc.is_writable = false;
        Object __value;
        __value = jips;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__EffectiveMJips()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "effectiveMJips";
        __desc.prototype = "host";
        __desc.is_writable = false;
        Object __value;
        __value = effectiveMJips;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__Count()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "count";
        __desc.prototype = "host";
        __desc.is_writable = false;
        Object __value;
        __value = count;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map map)
    {
        super.collectSlotDescriptions(map);
        map.put("loadAverage", slotMetaData__LoadAverage());
        map.put("jips", slotMetaData__Jips());
        map.put("effectiveMJips", slotMetaData__EffectiveMJips());
        map.put("count", slotMetaData__Count());
    }
}