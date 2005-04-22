package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.layout.HorizontalBoxLayout;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 1:12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class Queue extends RectangularContainer {
    Paint paint, selectedPaint;
    String coreLabel;

    public Queue() {
        super();
        coreLabel = null;
    }

    public Queue(String id, String label) {
        super(id, label, new HorizontalBoxLayout());
        setMargins(2d, 2d, 2d, 2d, 1d, 1d);
        coreLabel = label;
        setLabel(coreLabel+" ("+0+")");
    }

    public void setLabel(String label) {
        if (coreLabel == null)
            coreLabel = label;
        super.setLabel(label);
    }

    public void reshape(double tx, double ty, double w, double h) {
        super.reshape(tx,ty,w,h);
        paint = new GradientPaint(0,0,Color.blue,(float)width*.50f,(float)height*.75f,Color.green);
        selectedPaint = new GradientPaint(0,0,Color.green,(float)width*.50f,(float)height*.75f,Color.blue);
    }

    public void draw(Graphics2D g2) {
        if (paint != null && selectedPaint != null)
            g2.setPaint((selected ? selectedPaint : paint));
        if (label != null)
            g2.drawString(label, (int)rectShape.x, (int)(rectShape.y-2));
        super.draw(g2);
    }

    public ShapeGraphic getNext() {
        if (children.size() == 0)
            return null;
        ShapeGraphic sg = (ShapeGraphic) children.get(0);
        remove(sg);
        return sg;
    }

    public void add(ShapeGraphic sh) {
        super.add(sh);
        setLabel(coreLabel+" ("+children.size()+")");
    }

    public void remove(ShapeGraphic sh) {
        super.remove(sh);
        setLabel(coreLabel+" ("+children.size()+")");
    }
}
