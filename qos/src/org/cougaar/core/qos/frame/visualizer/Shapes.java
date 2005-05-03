package org.cougaar.core.qos.frame.visualizer;

import java.util.HashMap;
import java.awt.geom.RectangularShape;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 2, 2005
 * Time: 2:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Shapes {
    private static HashMap shapes = new HashMap();
    public static void add(String name, RectangularShape shape) {
        shapes.put(name, shape);
    }
    public static RectangularShape get(String name) {
        return getCopy(name);//(RectangularShape) shapes.get(name);
    }
    private static RectangularShape getCopy(String name) {
        RectangularShape s = (RectangularShape)shapes.get(name);
        if (s!= null)
            return (RectangularShape) s.clone();
        return null;
    }

    private Shapes(){}
}
