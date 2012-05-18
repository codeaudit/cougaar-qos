package org.cougaar.core.qos.frame.visualizer.shapes;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 3, 2005
 * Time: 12:08:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class RoundRectangle extends java.awt.geom.RoundRectangle2D.Double {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   public RoundRectangle() {
        super();
    }
    public RoundRectangle(double x, double y, double w, double h, double arcw, double arch) {
        super(x,y,w,h,arcw,arch);
    }
}
