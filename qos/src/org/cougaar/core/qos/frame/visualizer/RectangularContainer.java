package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 12:53:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RectangularContainer extends ShapeContainer {
    Rectangle2D.Double rectShape;
    BasicStroke bs = new BasicStroke(2);
    Paint paint, selectedPaint;

    public RectangularContainer() {
        super();
        createShape();
    }

    public RectangularContainer(String id, String label, ShapeLayout layout) {
        super(id,label);
        shapeLayout = layout;
	createShape();
    }

    protected Shape createShape() {
	bs = new BasicStroke(2);
        rectShape = new Rectangle2D.Double(x,y,width,height);
        return rectShape;
    }

    public void reshape(double tx, double ty, double w, double h) {
        super.reshape(tx,ty,w,h);
        paint = new GradientPaint(0,0,Color.blue,(float)width*.35f,(float)height*.35f,Color.blue);
        selectedPaint = new  GradientPaint(0,0,Color.magenta,(float)width*.35f,(float)height*.35f,Color.magenta);
        rectShape.x = tx;
        rectShape.y = ty;
        rectShape.width = w;
        rectShape.height= h;
        layoutChildren();
    }

    public Point2D.Double getNextInsertPosition() {
        return new Point2D.Double(x+(width/2d), y+(height/2d)); //???
    }

    public void draw(Graphics2D g2) {
        if (paint != null && selectedPaint != null)
            g2.setPaint((selected ? selectedPaint : paint));

        if (id != null)
           g2.drawString(id, (int)rectShape.x+15, (int)(rectShape.y+15));

        g2.setStroke(bs);
        g2.draw(rectShape);
        drawChildren(g2);
    }

    // clone thyself and assign the given frame
    //public ShapeGraphic createInstance(org.cougaar.core.qos.frame.Frame frame) {
    //RectangularContainer c = super.createInstance(frame);
    //if (c != null) {
    //    c.shape = 
    //}
    //}
}
