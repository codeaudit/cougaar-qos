package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Contains
    extends Relationship {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Contains(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "contains", __fm);
    }


    public Contains(UID uid) {
        this(null, uid);
    }


    public Contains(FrameSet frameSet,
                    UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "contains";
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
    }
}
