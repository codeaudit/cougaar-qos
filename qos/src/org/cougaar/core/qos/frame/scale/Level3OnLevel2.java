package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Level3OnLevel2
    extends Contains {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level3OnLevel2(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "Level3OnLevel2", __fm);
    }


    public Level3OnLevel2(UID uid) {
        this(null, uid);
    }


    public Level3OnLevel2(FrameSet frameSet,
                          UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "Level3OnLevel2";
    }


    public String getParentPrototype() {
        return "level2";
    }


    public String getChildPrototype() {
        return "level3";
    }
}
