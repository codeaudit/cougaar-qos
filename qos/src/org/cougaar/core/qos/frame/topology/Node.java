package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.util.UID;

public class Node
    extends Thing
{
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Node(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "node", __fm);
    }
    private transient Metric bytesIn;
    private transient Metric cpuLoadAverage;
    private transient Metric msgIn;
    private String status;
    private transient Metric cpuLoadMJips;
    private transient Metric bytesOut;
    private transient Metric msgOut;
    private transient Metric vmSize;


    public Node(UID uid)
    {
        this(null, uid);
    }


    public Node(FrameSet frameSet,
                UID uid)
    {
        super(frameSet, uid);
        initializeStatus("unknown");
    }


    public String getKind()
    {
        return "node";
    }


    protected void collectSlotValues(java.util.Properties __props)
    {
        super.collectSlotValues(__props);
        Object __value;
        __value = getBytesIn__AsObject();
        __props.put("bytesIn", __value != null ? __value : NIL);
        __value = getCpuLoadAverage__AsObject();
        __props.put("cpuLoadAverage", __value != null ? __value : NIL);
        __value = getMsgIn__AsObject();
        __props.put("msgIn", __value != null ? __value : NIL);
        __value = getStatus__AsObject();
        __props.put("status", __value != null ? __value : NIL);
        __value = getCpuLoadMJips__AsObject();
        __props.put("cpuLoadMJips", __value != null ? __value : NIL);
        __value = getBytesOut__AsObject();
        __props.put("bytesOut", __value != null ? __value : NIL);
        __value = getMsgOut__AsObject();
        __props.put("msgOut", __value != null ? __value : NIL);
        __value = getVmSize__AsObject();
        __props.put("vmSize", __value != null ? __value : NIL);
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


    public String getStatus()
    {
        return status;
    }


    String getStatus__NoWarn()
    {
        return status;
    }


    Object getStatus__AsObject()
    {
        return status;
    }


    public void setStatus(String __new_value)
    {
        String __old_value = status;
        this.status = __new_value;
        slotModified("status", __old_value, __new_value, true, true);
    }


    public void setStatus__AsObject(Object __new_value)
    {
        Object __old_value = getStatus__AsObject();
        this.status = force_String(__new_value);
        slotModified("status", __old_value, __new_value, true, true);
    }


    protected void initializeStatus(String new_value)
    {
        this.status = new_value;
        slotInitialized("status", new_value);
    }


    void initializeStatus__AsObject(Object new_value)
    {
        this.status = force_String(new_value);
        slotInitialized("status", new_value);
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


    public Metric getVmSize()
    {
        return vmSize;
    }


    Metric getVmSize__NoWarn()
    {
        return vmSize;
    }


    Object getVmSize__AsObject()
    {
        return vmSize;
    }


    protected void initializeVmSize(Metric new_value)
    {
        this.vmSize = new_value;
        slotInitialized("vmSize", new_value);
    }


    void initializeVmSize__AsObject(Object new_value)
    {
        this.vmSize = force_Metric(new_value);
        slotInitialized("vmSize", new_value);
    }


    public Metric getLoadAverage()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Host))
            throw new RuntimeException("Bogus container!");
       Host __container = (Host) __raw_container;
       return __container.getLoadAverage();
    }


    Object getLoadAverage__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Host)) {
            getLogger().warn("Container of " +this+ " is not a Host: " + __raw_container);
            return null;
       }
       Host __container = (Host) __raw_container;
       return __container.getLoadAverage__AsObject();
    }


    public Metric getJips()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Host))
            throw new RuntimeException("Bogus container!");
       Host __container = (Host) __raw_container;
       return __container.getJips();
    }


    Object getJips__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Host)) {
            getLogger().warn("Container of " +this+ " is not a Host: " + __raw_container);
            return null;
       }
       Host __container = (Host) __raw_container;
       return __container.getJips__AsObject();
    }


    public Metric getEffectiveMJips()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Host))
            throw new RuntimeException("Bogus container!");
       Host __container = (Host) __raw_container;
       return __container.getEffectiveMJips();
    }


    Object getEffectiveMJips__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Host)) {
            getLogger().warn("Container of " +this+ " is not a Host: " + __raw_container);
            return null;
       }
       Host __container = (Host) __raw_container;
       return __container.getEffectiveMJips__AsObject();
    }


    public Metric getCount()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null)
            throw new RuntimeException("No container!");
       if (!(__raw_container instanceof Host))
            throw new RuntimeException("Bogus container!");
       Host __container = (Host) __raw_container;
       return __container.getCount();
    }


    Object getCount__AsObject()
    {
       Object __raw_container = containerFrame();
       if ( __raw_container == null) return null;
       if (!(__raw_container instanceof Host)) {
            getLogger().warn("Container of " +this+ " is not a Host: " + __raw_container);
            return null;
       }
       Host __container = (Host) __raw_container;
       return __container.getCount__AsObject();
    }


    protected void fireContainerChanges(DataFrame __raw_old, DataFrame __raw_new)
    {
        if (!(__raw_old instanceof Host)) {
            getLogger().warn("Container of " +this+ " is not a Host: " + __raw_old);
            return;
        }
        if (!(__raw_new instanceof Host)) {
            getLogger().warn("Container of " +this+ " is not a Host: " + __raw_new);
            return;
        }
        Host __old_frame = (Host) __raw_old;
        Host __new_frame = (Host) __raw_new;
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
    }


    protected void fireContainerChanges(DataFrame __raw)
    {
        if (!(__raw instanceof Host)) {
            getLogger().warn("Container of " +this+ " is not a Host: " + __raw);
            return;
        }
        Host __new_frame = (Host) __raw;
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
    }
    private static final int bytesIn__HashVar__ = "bytesIn".hashCode();
    private static final int jips__HashVar__ = "jips".hashCode();
    private static final int status__HashVar__ = "status".hashCode();
    private static final int cpuLoadMJips__HashVar__ = "cpuLoadMJips".hashCode();
    private static final int loadAverage__HashVar__ = "loadAverage".hashCode();
    private static final int cpuLoadAverage__HashVar__ = "cpuLoadAverage".hashCode();
    private static final int msgIn__HashVar__ = "msgIn".hashCode();
    private static final int effectiveMJips__HashVar__ = "effectiveMJips".hashCode();
    private static final int count__HashVar__ = "count".hashCode();
    private static final int name__HashVar__ = "name".hashCode();
    private static final int bytesOut__HashVar__ = "bytesOut".hashCode();
    private static final int msgOut__HashVar__ = "msgOut".hashCode();
    private static final int vmSize__HashVar__ = "vmSize".hashCode();


    protected Object getLocalValue(String __slot)
    {
       int __key = __slot.hashCode();
       if (bytesIn__HashVar__ == __key)
            return getBytesIn__AsObject();
       else if (jips__HashVar__ == __key)
            return getJips__AsObject();
       else if (status__HashVar__ == __key)
            return getStatus__AsObject();
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
       else if (count__HashVar__ == __key)
            return getCount__AsObject();
       else if (name__HashVar__ == __key)
            return getName__AsObject();
       else if (bytesOut__HashVar__ == __key)
            return getBytesOut__AsObject();
       else if (msgOut__HashVar__ == __key)
            return getMsgOut__AsObject();
       else if (vmSize__HashVar__ == __key)
            return getVmSize__AsObject();
       else
           return super.getLocalValue(__slot);
    }


    protected void setLocalValue(String __slot,
                                 Object __value)
    {
       int __key = __slot.hashCode();
       if (status__HashVar__ == __key)
            setStatus__AsObject(__value);
       else
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
       else if (msgIn__HashVar__ == __key)
            initializeMsgIn__AsObject(__value);
       else if (status__HashVar__ == __key)
            initializeStatus__AsObject(__value);
       else if (cpuLoadMJips__HashVar__ == __key)
            initializeCpuLoadMJips__AsObject(__value);
       else if (bytesOut__HashVar__ == __key)
            initializeBytesOut__AsObject(__value);
       else if (msgOut__HashVar__ == __key)
            initializeMsgOut__AsObject(__value);
       else if (vmSize__HashVar__ == __key)
            initializeVmSize__AsObject(__value);
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
        bytesIn = getFrameSet().getMetricValue(this, "Node($(name)):BytesIn(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Node($(name)):BytesIn(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = cpuLoadAverage;
                cpuLoadAverage = (Metric) __new;
                slotModified("cpuLoadAverage", __old, __new, true, true);
            }
        };
        cpuLoadAverage = getFrameSet().getMetricValue(this, "Node($(name)):CPULoadAvg(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Node($(name)):CPULoadAvg(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = msgIn;
                msgIn = (Metric) __new;
                slotModified("msgIn", __old, __new, true, true);
            }
        };
        msgIn = getFrameSet().getMetricValue(this, "Node($(name)):MsgIn(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Node($(name)):MsgIn(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = cpuLoadMJips;
                cpuLoadMJips = (Metric) __new;
                slotModified("cpuLoadMJips", __old, __new, true, true);
            }
        };
        cpuLoadMJips = getFrameSet().getMetricValue(this, "Node($(name)):CPULoadMJips(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Node($(name)):CPULoadMJips(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = bytesOut;
                bytesOut = (Metric) __new;
                slotModified("bytesOut", __old, __new, true, true);
            }
        };
        bytesOut = getFrameSet().getMetricValue(this, "Node($(name)):BytesOut(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Node($(name)):BytesOut(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = msgOut;
                msgOut = (Metric) __new;
                slotModified("msgOut", __old, __new, true, true);
            }
        };
        msgOut = getFrameSet().getMetricValue(this, "Node($(name)):MsgOut(10)");
        getFrameSet().subscribeToMetric(this, __observer, "Node($(name)):MsgOut(10)");
        __observer = new java.util.Observer() {
            public void update(java.util.Observable __xxx, Object __new) {
                Object __old = vmSize;
                vmSize = (Metric) __new;
                slotModified("vmSize", __old, __new, true, true);
            }
        };
        vmSize = getFrameSet().getMetricValue(this, "Node($(name)):VMSize()");
        getFrameSet().subscribeToMetric(this, __observer, "Node($(name)):VMSize()");
    }


    public SlotDescription slotMetaData__BytesIn()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "bytesIn";
        __desc.prototype = "node";
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
        __desc.prototype = "node";
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


    public SlotDescription slotMetaData__MsgIn()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "msgIn";
        __desc.prototype = "node";
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


    public SlotDescription slotMetaData__Status()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "status";
        __desc.prototype = "node";
        __desc.is_writable = true;
        Object __value;
        __value = status;
        if (__value != null) {
            __desc.is_overridden = true;
            __desc.value = __value;
        } else {
            __desc.is_overridden = false;
            __desc.value = "unknown";
        }
        return __desc;
    }


    public SlotDescription slotMetaData__CpuLoadMJips()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "cpuLoadMJips";
        __desc.prototype = "node";
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
        __desc.prototype = "node";
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
        __desc.prototype = "node";
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


    public SlotDescription slotMetaData__VmSize()
    {
        SlotDescription __desc = new SlotDescription();
        __desc.name = "vmSize";
        __desc.prototype = "node";
        __desc.is_writable = false;
        Object __value;
        __value = vmSize;
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


    protected void collectSlotDescriptions(java.util.Map map)
    {
        super.collectSlotDescriptions(map);
        map.put("bytesIn", slotMetaData__BytesIn());
        map.put("cpuLoadAverage", slotMetaData__CpuLoadAverage());
        map.put("msgIn", slotMetaData__MsgIn());
        map.put("status", slotMetaData__Status());
        map.put("cpuLoadMJips", slotMetaData__CpuLoadMJips());
        map.put("bytesOut", slotMetaData__BytesOut());
        map.put("msgOut", slotMetaData__MsgOut());
        map.put("vmSize", slotMetaData__VmSize());
        map.put("loadAverage", slotMetaData__LoadAverage());
        map.put("jips", slotMetaData__Jips());
        map.put("effectiveMJips", slotMetaData__EffectiveMJips());
        map.put("count", slotMetaData__Count());
    }
}
