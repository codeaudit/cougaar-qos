package org.cougaar.core.qos.frame.visualizer;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 15, 2005
 * Time: 4:14:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeRenderers {
    private HashMap shapeRenderers;

    public ShapeRenderers() {
        shapeRenderers = new HashMap();
    }
    public void add(ShapeRenderer r) {
        if (shapeRenderers.get(r.getName()) == null)
            shapeRenderers.put(r.getName(), r);
    }
    public ShapeRenderer get(String name) {
        return (ShapeRenderer) shapeRenderers.get(name);
    }

    /*
    <shaperenderer name="defaultRenderer" paint="Color.green" selectedpaint="Color.yellow" fillpaint="Color.white" selfillpaint="Color.green" linewidth="1" bordered="true" filled="false"/>
    <shaperenderer name="rootRenderer" paint="#FFFFFF" selectedpaint="#FFFFFF" fillpaint="#FFFFFF" selfillpaint="#FFFFFF" linewidth="1" bordered="true" filled="false"/>
    <shaperenderer name="waitingJobRenderer" paint="(123,200,100)" selectedpaint="Color.magenta" fillpaint="(123,200,100)" selfillpaint="Color.magenta" linewidth="2" bordered="true" filled="true" />
    */
     public String toString(Paint p) {
        Color c = (Color)p;
        return "\"("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")\"";
    }

     public String[] toXML() {
           String shapeRendererName;
           ShapeRenderer s;
           String shpStr[] = new String[shapeRenderers.size()];
           int i=0;
           
           // boolean drawBorder = true, drawFilled=false;
           for (Iterator ii=shapeRenderers.keySet().iterator(); ii.hasNext(); i++) {
               shapeRendererName = (String) ii.next();
               s = (ShapeRenderer) shapeRenderers.get(shapeRendererName);


               shpStr[i] = "<shaperenderer name=\""+shapeRendererName+"\" paint="+toString(s.paint)+" selectedpaint="+toString(s.selectedPaint)+
                       " fillpaint="+toString(s.fillPaint)+" selfillpaint="+toString(s.selectedFillPaint)+
                       " linewidth=\""+s.lineStroke.getLineWidth()+"\" bordered=\""+s.drawBorder+"\" filled=\""+s.drawFilled+"\" />";
           }
           return shpStr;
       }

}

