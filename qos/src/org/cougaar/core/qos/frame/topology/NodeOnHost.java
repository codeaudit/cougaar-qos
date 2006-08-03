/*
 * Generated by Cougaar QoS FrameGen
 *   from /Volumes/Data/Projects/cougaar/HEAD/qos/src/org/cougaar/core/qos/frame/topology/cougaar-topology-protos.xml
 *   at Aug 3, 2006 10:09:21 AM
 *
 * Copyright BBN Technologies 2006
 *
 */
package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class NodeOnHost
    extends Contains {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new NodeOnHost(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "NodeOnHost", __fm);
    }


    public NodeOnHost(UID uid) {
        this(null, uid);
    }


    public NodeOnHost(FrameSet frameSet,
                      UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "NodeOnHost";
    }


    public String getParentPrototype() {
        return "host";
    }


    public String getChildPrototype() {
        return "node";
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
    }
}
