package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Level2OnLevel1
    extends Contains {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level2OnLevel1(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "level2OnLevel1", __fm);
    }


    public Level2OnLevel1(UID uid) {
        this(null, uid);
    }


    public Level2OnLevel1(FrameSet frameSet,
                          UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "level2OnLevel1";
    }


    public String getParentPrototype() {
        return "level1";
    }


    public String getChildPrototype() {
        return "level2";
    }
}
