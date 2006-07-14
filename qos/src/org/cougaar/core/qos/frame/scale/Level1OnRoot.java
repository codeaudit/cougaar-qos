package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Level1OnRoot
    extends Contains {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level1OnRoot(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "Level1OnRoot", __fm);
    }


    public Level1OnRoot(UID uid) {
        this(null, uid);
    }


    public Level1OnRoot(FrameSet frameSet,
                        UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "Level1OnRoot";
    }


    public String getParentPrototype() {
        return "root";
    }


    public String getChildPrototype() {
        return "level1";
    }
}
