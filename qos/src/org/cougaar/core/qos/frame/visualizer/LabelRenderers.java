package org.cougaar.core.qos.frame.visualizer;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 15, 2005
 * Time: 3:43:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class LabelRenderers {
    private HashMap labelRenderers;


    public LabelRenderers() {
        labelRenderers = new HashMap();
    }
    public void add(org.cougaar.core.qos.frame.visualizer.LabelRenderer r) {
        if (labelRenderers.get(r.getName()) == null)
            labelRenderers.put(r.getName(), r);
    }
    public LabelRenderer get(String name) {
        return (LabelRenderer) labelRenderers.get(name);
    }


    public String[] toXML() {
        String labelRendererName;
        LabelRenderer lblr;
        String lblStr[] = new String[labelRenderers.size()];
        int i=0;
        String tag=null;
        Color c;
        //<labelrenderer name="taskLabelRenderer" xoff="0" yoff="+20" font="default" color="Color.blue" mouseover="true"/>
        //<labelrenderer name="hostLabelRenderer" xoff="+8" yoff="+12" font="default" color="Color.blue"/>
        //<containerlabelrenderer name="queueLabelRenderer" xoff="+10" yoff="+15" font="default" color="Color.blue"/>
        for (Iterator ii=labelRenderers.keySet().iterator(); ii.hasNext(); i++) {
            labelRendererName = (String) ii.next();
            lblr = (LabelRenderer) labelRenderers.get(labelRendererName);
            c = (Color) lblr.getPaint();
            tag = (lblr instanceof ContainerLabelRenderer ? "containerlabelrenderer" : "labelrenderer");
            lblStr[i] = "<"+tag+" name=\""+labelRendererName+"\" xoff=\""+lblr.xoffset+"\" yoff=\""+lblr.yoffset+"\" font=\"default\" color=\"("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")\" mouseover=\""+lblr.onMouseOverOnly+"\"/>";
        }
        return lblStr;
    }

}
