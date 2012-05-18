package org.cougaar.core.qos.frame.visualizer.shapes;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 3, 2005
 * Time: 12:05:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Ellipse extends java.awt.geom.Ellipse2D.Double {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   public Ellipse() {
        super();
    }
    public Ellipse(double x, double y, double w, double h) {
        super(x,y,w,h);
    }
}
