package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Level5OnLevel4
    extends Contains {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level5OnLevel4(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "level5OnLevel4", __fm);
    }


    public Level5OnLevel4(UID uid) {
        this(null, uid);
    }


    public Level5OnLevel4(FrameSet frameSet,
                          UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "level5OnLevel4";
    }


    public String getParentPrototype() {
        return "level4";
    }


    public String getChildPrototype() {
        return "level5";
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
    }
}
