package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Level4OnLevel3
    extends Contains {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level4OnLevel3(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "Level4OnLevel3", __fm);
    }


    public Level4OnLevel3(UID uid) {
        this(null, uid);
    }


    public Level4OnLevel3(FrameSet frameSet,
                          UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "Level4OnLevel3";
    }


    public String getParentPrototype() {
        return "level3";
    }


    public String getChildPrototype() {
        return "level4";
    }
}
