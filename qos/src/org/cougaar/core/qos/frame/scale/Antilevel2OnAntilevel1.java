package org.cougaar.core.qos.frame.scale;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class Antilevel2OnAntilevel1
    extends Relationship {
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new Antilevel2OnAntilevel1(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.scale", "antilevel2OnAntilevel1", __fm);
    }


    public Antilevel2OnAntilevel1(UID uid) {
        this(null, uid);
    }


    public Antilevel2OnAntilevel1(FrameSet frameSet,
                                  UID uid) {
        super(frameSet, uid);
    }


    public String getKind() {
        return "antilevel2OnAntilevel1";
    }


    public String getParentPrototype() {
        return "antilevel1";
    }


    public String getChildPrototype() {
        return "antilevel2";
    }


    protected void collectSlotNames(java.util.Set<String> slots) {
        super.collectSlotNames(slots);
    }
}