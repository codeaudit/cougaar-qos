package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Level6OnLevel5
    extends Contains {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Level6OnLevel5(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "Level6OnLevel5", __fm);
    }


    public Level6OnLevel5(UID uid) {
        this(null, uid);
    }


    public Level6OnLevel5(FrameSet frameSet,
                          UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "Level6OnLevel5";
    }


    public String getParentPrototype() {
        return "level5";
    }


    public String getChildPrototype() {
        return "level6";
    }
}
