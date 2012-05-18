package org.cougaar.core.qos.frame.visualizer;

import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 2, 2005
 * Time: 2:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Shapes {
    private HashMap shapes;


    public Shapes(){
        shapes = new HashMap();
    }

    public void add(String name, RectangularShape shape) {
        shapes.put(name, shape);
    }
    public RectangularShape get(String name) {
        return getCopy(name);//(RectangularShape) shapes.get(name);
    }
    private RectangularShape getCopy(String name) {
        RectangularShape s = (RectangularShape)shapes.get(name);
        if (s!= null)
            return (RectangularShape) s.clone();
        return null;
    }

    public String[] toXML() {
        String shapeName;
        RectangularShape sh;
        String shapeStr[] = new String[shapes.size()];
        int i=0;
        Rectangle r;
        String append = "";

        for (Iterator ii=shapes.keySet().iterator(); ii.hasNext(); i++) {
            shapeName = (String) ii.next();
            sh = (RectangularShape) shapes.get(shapeName);
            r = sh.getBounds();
            if (sh instanceof RoundRectangle2D)
                append = "  arcw=\""+((RoundRectangle2D)sh).getArcWidth()+"\"  arch=\""+((RoundRectangle2D)sh).getArcHeight()+"\" ";

            shapeStr[i] = "<shape name="+shapeName+" class=\""+sh.getClass().getName()+"\" x=\"0\" y=\"0\" w=\""+r.width+"\" h=\""+r.height+"\" "+append+"/>";
        }
        return shapeStr;
    }

}
