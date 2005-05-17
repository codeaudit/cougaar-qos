package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.util.UID;

public class Agent
    extends Thing
{
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Agent(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "agent", __fm);
    }
    private transient Metric bytesIn;
    private transient Metric cpuLoadAverage;
    private transient Metric persistSizeLast;
    private transient Metric msgIn;
    private transient Metric cpuLoadMJips;
    private transient Metric bytesOut;
    private transient Metric msgOut;


    public Agent(UID uid)
    {
        this(null, uid);
    }


    public Agent(FrameSet frameSet,
                 UID uid)
    {
        super(frameSet, uid);
    }


    public String getKind()
    {
        return "agent";
    }


    protected void collectSlotValues(java.util.Properties __props)
    {
        super.collectSlotValues(__props);
        Object __value;
        __value = getBytesIn__AsObject();
        __props.put("bytesIn", __value != null ? __value : NIL);
        __value = getCpuLoadAverage__AsObject();
        __props.put("cpuLoadAverage", __value != null ? __value : NIL);
        __value = getPersistSizeLast__AsObject();
        __props.put("persistSizeLast", __value != null ? __value : NIL);
        __value = getMsgIn__AsObject();
        __props.put("msgIn", __value != null ? __value : NIL);
        __value = getCpuLoadMJips__AsObject();
        __props.put("cpuLoadMJips", __value != null ? __value : NIL);
        __value = getBytesOut__AsObject();
        __props.put("bytesOut", __value != null ? __value : NIL);
        __value = getMsgOut__AsObject();
        __props.put("msgOut", __value != null ? __value : NIL);
    }


    protected void collectContainerSlotValues(java.util.Properties __props)
    {
        super.collectContainerSlotValues(__props);
        Object __value;
        __value = getLoadAverage__AsObject();
        __props.put("loadAverage", __value != null ? __value : NIL);
        __value = getJips__AsObject();
        __props.put("jips", __value != null ? __value : NIL);
        __value = getEffectiveMJips__AsObject();
        __props.put("effectiveMJips", __value != null ? __value : NIL);
        __value = getCount__AsObject();
        __props.put("count", __value != null ? __value : NIL);
        __value = getVmSize__AsObject();
        __props.put("vmSize", __value != null ? __value : NIL);
    }


    public Metric getBytesIn()
    {
        return bytesIn;
    }


    Metric getBytesIn__NoWarn()
    {
        return bytesIn;
    }


    Object getBytesIn__AsObject()
    {
        return bytesIn;
    }


    protected void initializeBytesIn(Metric new_value)
    {
        this.bytesIn = new_value;
        slotInitialized("bytesIn", new_value);
    }


    void initializeBytesIn__AsObject(Object new_value)
    {
        this.bytesIn = force_Metric(new_value);
        slotInitialized("bytesIn", new_value);
    }


    public Metric getCpuLoadAverage()
    {
        return cpuLoadAverage;
    }


    Metric getCpuLoadAverage__NoWarn()
    {
        return cpuLoadAverage;
    }


    Object getCpuLoadAverage__AsObject()
    {
        return cpuLoadAverage;
    }


    protected void initializeCpuLoadAverage(Metric new_value)
    {
        this.cpuLoadAverage = new_value;
        slotInitialized("cpuLoadAverage", new_value);
    }


    void initializeCpuLoadAverage__AsObject(Object new_value)
    {
        this.cpuLoadAverage = force_Metric(new_value);
        slotInitialized("cpuLoadAverage", new_value);
    }


    public Metric getPersistSizeLast()
    {
        return persistSizeLast;
    }


    Metric getPersistSizeLast__NoWarn()
    {
        return persistSizeLast;
    }


    Object getPersistSizeLast__AsObject()
    {
        return persistSizeLast;
    }


    protected void initializePersistSizeLast(Metric new_value)
    {
        this.persistSizeLast = new_value;
        slotInitialized("persistSizeLast", new_value);
    }


    void initializePersistSizeLast__AsObject(Object new_value)
    {
        this.persistSizeLast = force_Metric(new_value);
        slotInitialized("persistSizeLast", new_value);
    }


    public Metric getMsgIn()
    {
        return msgIn;
    }


    Metric getMsgIn__NoWarn()
    {
        return msgIn;
    }


    Object getMsgIn__AsObject()
    {
        return msgIn;
    }


    protected void initializeMsgIn(Metric new_value)
    {
        this.msgIn = new_value;
        slotInitialized("msgIn", new_value);
    }


    void initializeMsgIn__AsObject(Object new_value)
    {
        this.msgIn = force_Metric(new_value);
        slotInitialized("msgIn", new_value);
    }


    public Metric getCpuLoadMJips()
    {
        return cpuLoadMJips;
    }


    Metric getCpuLoadMJips__NoWarn()
    {
        return cpuLoadMJips;
    }


    Object getCpuLoadMJips__AsObject()
    {
        return cpuLoadMJips;
    }


    protected void initializeCpuLoadMJips(Metric new_value)
    {
        this.cpuLoadMJips = new_value;
        slotInitialized("cpuLoadMJips", new_value);
    }


    void initializeCpuLoadMJips__AsObject(Object new_value)
    {
        this.cpuLoadMJips = force_Metric(new_value);
        slotInitialized("cpuLoadMJips", new_value);
    }


    public Metric getBytesOut()
    {
        return bytesOut;
    }


    Metric getBytesOut__NoWarn()
    {
        return bytesOut;
    }


    Object getBytesOut__AsObject()
    {
        return bytesOut;
    }


    protected void initializeBytesOut(Metric new_value)
    {
        this.bytesOut = new_value;
        slotInitialized("bytesOut", new_value);
    }


    void initializeBytesOut__AsObject(Object new_value)
    {
        this.bytesOut = force_Metric(new_value);
        slotInitialized("bytesOut", new_value);
    }


    public Metric getMsgOut()
    {
        return msgOut;
    }


    Metric getMsgOut__NoWarn()
    {
        return msgOut;
    }


    Object getMsgOut__AsObject()
    {
        return msgOut;
    }


    protected void initializeMsgOut(Metric new_value)
    {
        this.msgOut = new_value;
        slotInitialized("msgOut", new_value);
    }


    void initializeMsgOut__AsObject(Object new_value)
    {
        this.msgOut = force_Metric(new_value);
        slotInitialized("msgOut", new_value);
    }


    public Metric getLoadAverage()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Node))
            throw new RuntimeException("Bogus container!");
       Node __container = (Node) __raw_container;
       return __container.getLoadAverage();
    }


    Object getLoadAverage__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw_container);
            return null;
       }
       Node __container = (Node) __raw_container;
       return __container.getLoadAverage__AsObject();
    }


    public Metric getJips()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Node))
            throw new RuntimeException("Bogus container!");
       Node __container = (Node) __raw_container;
       return __container.getJips();
    }


    Object getJips__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw_container);
            return null;
       }
       Node __container = (Node) __raw_container;
       return __container.getJips__AsObject();
    }


    public Metric getEffectiveMJips()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Node))
            throw new RuntimeException("Bogus container!");
       Node __container = (Node) __raw_container;
       return __container.getEffectiveMJips();
    }


    Object getEffectiveMJips__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw_container);
            return null;
       }
       Node __container = (Node) __raw_container;
       return __container.getEffectiveMJips__AsObject();
    }


    public Metric getCount()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Node))
            throw new RuntimeException("Bogus container!");
       Node __container = (Node) __raw_container;
       return __container.getCount();
    }


    Object getCount__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw_container);
            return null;
       }
       Node __container = (Node) __raw_container;
       return __container.getCount__AsObject();
    }


    public Metric getVmSize()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Node))
            throw new RuntimeException("Bogus container!");
       Node __container = (Node) __raw_container;
       return __container.getVmSize();
    }


    Object getVmSize__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw_container);
            return null;
       }
       Node __container = (Node) __raw_container;
       return __container.getVmSize__AsObject();
    }


    protected void fireContainerChanges(DataFrame __raw_old, DataFrame __raw_new)
    {
        if (!(__raw_old instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw_old);
            return;
        }
        if (!(__raw_new instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw_new);
            return;
        }
        Node __old_frame = (Node) __raw_old;
        Node __new_frame = (Node) __raw_new;
        Object __old;
        Object __new;
        __old = __old_frame.getLoadAverage__AsObject();
        __new = __new_frame.getLoadAverage__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("loadAverage", __old, __new);
            }
        }
        __old = __old_frame.getJips__AsObject();
        __new = __new_frame.getJips__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("jips", __old, __new);
            }
        }
        __old = __old_frame.getEffectiveMJips__AsObject();
        __new = __new_frame.getEffectiveMJips__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("effectiveMJips", __old, __new);
            }
        }
        __old = __old_frame.getCount__AsObject();
        __new = __new_frame.getCount__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("count", __old, __new);
            }
        }
        __old = __old_frame.getVmSize__AsObject();
        __new = __new_frame.getVmSize__AsObject();
        if (__new != null) {
            if (__old == null || !__old.equals(__new)) {
                fireChange("vmSize", __old, __new);
            }
        }
    }


    protected void fireContainerChanges(DataFrame __raw)
    {
        if (!(__raw instanceof Node)) {
            getLogger().warn("Container of " +this+ " is not a Node: " + __raw);
            return;
        }
        Node __new_frame = (Node) __raw;
        Object __new;
        __new = __new_frame.getLoadAverage__AsObject();
        if (__new != null) {
            fireChange("loadAverage", null, __new);
        }
        __new = __new_frame.getJips__AsObject();
        if (__new != null) {
            fireChange("jips", null, __new);
        }
        __new = __new_frame.getEffectiveMJips__AsObject();
        if (__new != null) {
            fireChange("effectiveMJips", null, __new);
        }
        __new = __new_frame.getCount__AsObject();
        if (__new != null) {
            fireChange("count", null, __new);
        }
        __new = __new_frame.getVmSize__AsObject();
        if (__new != null) {
            fireChange("vmSize", null, __new);
        }
    }
    private static final int bytesIn__HashVar__ = "bytesIn".hashCode();
    private static final int jips__HashVar__ = "jips".hashCode();
    private static final int persistSizeLast__HashVar__ = "persistSizeLast".hashCode();
    private static final int cpuLoadMJips__HashVar__ = "cpuLoadMJips".hashCode();
    private static final int loadAverage__HashVar__ = "loadAverage".hashCode();
    private static final int cpuLoadAverage__HashVar__ = "cpuLoadAverage".hashCode();
    private static final int msgIn__HashVar__ = "msgIn".hashCode();
    private static final int effectiveMJips__HashVar__ = "effectiveMJips".hashCode();
    private static final int bytesOut__HashVar__ = "bytesOut".hashCode();
    private static final int name__HashVar__ = "name".hashCode();
    private static final int count__HashVar__ = "count".hashCode();
    private static final int vmSize__HashVar__ = "vmSize".hashCode();
    private static final int msgOut__HashVar__ = "msgOut".hashCode();


    protected Object getLocalValue(String __slot)
    {
       int __key = __slot.hashCode();
       if (bytesIn__HashVar__ == __key)
            return getBytesIn__AsObject();
       else if (jips__HashVar__ == __key)
            return getJips__AsObject();
       else if (persistSizeLast__HashVar__ == __key)
            return getPersistSizeLast__AsObject();
       else if (cpuLoadMJips__HashVar__ == __key)
            return getCpuLoadMJips__AsObject();
       else if (loadAverage__HashVar__ == __key)
            return getLoadAverage__AsObject();
       else if (cpuLoadAverage__HashVar__ == __key)
            return getCpuLoadAverage__AsObject();
       else if (msgIn__HashVar__ == __key)
            return getMsgIn__AsObject();
       else if (effectiveMJips__HashVar__ == __key)
            return getEffectiveMJips__AsObject();
       else if (bytesOut__HashVar__ == __key)
            return getBytesOut__AsObject();
       else if (name__HashVar__ == __key)
            return getName__AsObject();
       else if (count__HashVar__ == __key)
            return getCount__AsObject();
       else if (vmSize__HashVar__ == __key)
            return getVmSize__AsObject();
       else if (msgOut__HashVar__ == __key)
            return getMsgOut__AsObject();
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
       if (bytesIn__HashVar__ == __key)
            initializeBytesIn__AsObject(__value);
       else if (cpuLoadAverage__HashVar__ == __key)
            initializeCpuLoadAverage__AsObject(__value);
       else if (persistSizeLast__HashVar__ == __key)
            initializePersistSizeLast__AsObject(__value);
       else if (msgIn__HashVar__ == __key)
            initializeMsgIn__AsObject(__value);
       else if (cpuLoadMJips__HashVar__ == __key)
            initializeCpuLoadMJips__AsObject(__value);
       else if (bytesOut__HashVar__ == __key)
            initializeBytesOut__AsObject(__value);
       else if (msgOut__HashVar__ == __key)
            initializeMsgOut__AsObject(__value);
       else
            super.initializeLocalValue(__slot, __value);
    }


    protected void postInitialize()
    {
        super.postInitialize();
        java.util.Observer __observer;
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = bytesIn;
                bytesIn = (Metric) __new;
                slotModified("bytesIn", __old, __new, true, true);
            }
        };
        bytesIn = getFrameSet().getMetricValue(this, "Agent($(name)):BytesIn(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Agent($(name)):BytesIn(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = cpuLoadAverage;
                cpuLoadAverage = (Metric) __new;
                slotModified("cpuLoadAverage", __old, __new, true, true);
            }
        };
        cpuLoadAverage = getFrameSet().getMetricValue(this, "Agent($(name)):CPULoadAvg(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Agent($(name)):CPULoadAvg(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = persistSizeLast;
                persistSizeLast = (Metric) __new;
                slotModified("persistSizeLast", __old, __new, true, true);
            }
        };
        persistSizeLast = getFrameSet().getMetricValue(this, "Agent($(name)):PersistSizeLast()");
        getFrameSet().subscribeToMetric(this, __observer, "Agent($(name)):PersistSizeLast()");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = msgIn;
                msgIn = (Metric) __new;
                slotModified("msgIn", __old, __new, true, true);
            }
        };
        msgIn = getFrameSet().getMetricValue(this, "Agent($(name)):MsgIn(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Agent($(name)):MsgIn(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = cpuLoadMJips;
                cpuLoadMJips = (Metric) __new;
                slotModified("cpuLoadMJips", __old, __new, true, true);
            }
        };
        cpuLoadMJips = getFrameSet().getMetricValue(this, "Agent($(name)):CPULoadMJips(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Agent($(name)):CPULoadMJips(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = bytesOut;
                bytesOut = (Metric) __new;
                slotModified("bytesOut", __old, __new, true, true);
            }
        };
        bytesOut = getFrameSet().getMetricValue(this, "Agent($(name)):BytesOut(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Agent($(name)):BytesOut(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = msgOut;
                msgOut = (Metric) __new;
                slotModified("msgOut", __old, __new, true, true);
            }
        };
        msgOut = getFrameSet().getMetricValue(this, "Agent($(name)):MsgOut(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Agent($(name)):MsgOut(10)");
    }


    public SlotDescription slotMetaData__BytesIn()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "bytesIn";
        __desc.prototype = "agent";
        __desc.is_writable = false;
        Object __value;
        __value = bytesIn;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__CpuLoadAverage()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "cpuLoadAverage";
        __desc.prototype = "agent";
        __desc.is_writable = false;
        Object __value;
        __value = cpuLoadAverage;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__PersistSizeLast()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "persistSizeLast";
        __desc.prototype = "agent";
        __desc.is_writable = false;
        Object __value;
        __value = persistSizeLast;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__MsgIn()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "msgIn";
        __desc.prototype = "agent";
        __desc.is_writable = false;
        Object __value;
        __value = msgIn;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__CpuLoadMJips()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "cpuLoadMJips";
        __desc.prototype = "agent";
        __desc.is_writable = false;
        Object __value;
        __value = cpuLoadMJips;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__BytesOut()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "bytesOut";
        __desc.prototype = "agent";
        __desc.is_writable = false;
        Object __value;
        __value = bytesOut;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__MsgOut()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "msgOut";
        __desc.prototype = "agent";
        __desc.is_writable = false;
        Object __value;
        __value = msgOut;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
        }
        return __desc;
    }


    public SlotDescription slotMetaData__LoadAverage()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "loadAverage";
        __desc.prototype = "host";
        __desc.value = getLoadAverage__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Jips()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "jips";
        __desc.prototype = "host";
        __desc.value = getJips__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__EffectiveMJips()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "effectiveMJips";
        __desc.prototype = "host";
        __desc.value = getEffectiveMJips__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__Count()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "count";
        __desc.prototype = "host";
        __desc.value = getCount__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    public SlotDescription slotMetaData__VmSize()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "vmSize";
        __desc.prototype = "node";
        __desc.value = getVmSize__AsObject();
        __desc.is_overridden = false;
        __desc.is_writable = false;
        return __desc;
    }


    protected void collectSlotDescriptions(java.util.Map map)
    {
        super.collectSlotDescriptions(map);
        map.put("bytesIn", slotMetaData__BytesIn());
        map.put("cpuLoadAverage", slotMetaData__CpuLoadAverage());
        map.put("persistSizeLast", slotMetaData__PersistSizeLast());
        map.put("msgIn", slotMetaData__MsgIn());
        map.put("cpuLoadMJips", slotMetaData__CpuLoadMJips());
        map.put("bytesOut", slotMetaData__BytesOut());
        map.put("msgOut", slotMetaData__MsgOut());
        map.put("loadAverage", slotMetaData__LoadAverage());
        map.put("jips", slotMetaData__Jips());
        map.put("effectiveMJips", slotMetaData__EffectiveMJips());
        map.put("count", slotMetaData__Count());
        map.put("vmSize", slotMetaData__VmSize());
    }
}
