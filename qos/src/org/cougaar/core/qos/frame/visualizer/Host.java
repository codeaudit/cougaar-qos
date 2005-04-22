package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.layout.VerticalBoxLayout;
import org.cougaar.core.qos.frame.visualizer.layout.HorizontalBoxLayout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 12:45:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Host extends RectangularContainer  {
    //private static BasicStroke bs = new BasicStroke(2);
    //Paint paint, selectedPaint;

    public Host() {
        super();
        createShape();
    }

    public Host(String id, String label) {
        super(id,label, new VerticalBoxLayout());
        createShape();
    }

    public void reshape(double tx, double ty, double w, double h) {
        super.reshape(tx,ty,w,h);
        paint = new GradientPaint(0,0,Color.blue,(float)width*.35f,(float)height*.35f,Color.blue);
        selectedPaint = new  GradientPaint(0,0,Color.magenta,(float)width*.35f,(float)height*.35f,Color.magenta);
    }

    public Point2D.Double getNextInsertPosition() {
        return getPosition();
    }

    public void draw(Graphics2D g2) {
        if (paint != null && selectedPaint != null)
            g2.setPaint((selected ? selectedPaint : paint));
        if (label != null)
            g2.drawString(label, (int)rectShape.x, (int)(rectShape.y-2));
        g2.setStroke(bs);
        super.draw(g2);
    }
}