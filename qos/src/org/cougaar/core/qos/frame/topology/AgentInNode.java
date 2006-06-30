package org.cougaar.core.qos.frame.topology;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.util.UID;

public class AgentInNode
    extends Contains
{
    static {
        org.cougaar.core.qos.frame.FrameMaker __fm = 
            new org.cougaar.core.qos.frame.FrameMaker() {
                public DataFrame makeFrame(FrameSet frameSet, UID uid) {
                     return new AgentInNode(frameSet, uid);
                }
            };
            DataFrame.registerFrameMaker("org.cougaar.core.qos.frame.topology", "AgentInNode", __fm);
    }


    public AgentInNode(UID uid)
    {
        this(null, uid);
    }


    public AgentInNode(FrameSet frameSet,
                       UID uid)
    {
        super(frameSet, uid);
    }


    public String getKind()
    {
        return "AgentInNode";
    }


    public String getParentPrototype()
    {
        return "node";
    }


    public String getChildPrototype()
    {
        return "agent";
    }
}
