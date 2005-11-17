package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.SlotDescription;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.util.UID;

public class IndicatorOnAgent
    extends Contains
{
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new IndicatorOnAgent(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "IndicatorOnAgent", __fm);
    }


    public IndicatorOnAgent(UID uid)
    {
        this(null, uid);
    }


    public IndicatorOnAgent(FrameSet frameSet,
                            UID uid)
    {
        super(frameSet, uid);
    }


    public String getKind()
    {
        return "IndicatorOnAgent";
    }


    public String getParentPrototype()
    {
        return "agent";
    }


    public String getChildPrototype()
    {
        return "indicator";
    }
}
