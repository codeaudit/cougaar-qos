package org.cougaar.core.qos.frame.visualizer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 2, 2005
 * Time: 1:08:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContainerLabelRenderer extends LabelRenderer {

    public ContainerLabelRenderer(String rendererName, double xoff, double yoff) {
       this(rendererName, xoff, yoff, null, null);
    }
    public ContainerLabelRenderer(String rendererName, double xoff, double yoff, Font font) {
        this(rendererName, xoff, yoff, font, null);
    }
    public ContainerLabelRenderer(String rendererName, double xoff, double yoff, Paint paint) {
        this(rendererName, xoff, yoff, null, paint);
    }
    public ContainerLabelRenderer(String rendererName, double xoff, double yoff, Font font, Paint paint) {
        super(rendererName, xoff, yoff, font, paint);
    }

    @Override
   public void drawLabel(Graphics2D g2, ShapeGraphic shapeGraphic) {
        if (shapeGraphic == null || shapeGraphic.getLabel()==null)
            return;
        Shape shape = shapeGraphic.getShape();
        if (shape == null)
            return;
        if (paint != null)
            g2.setPaint(paint);
        if (font != null)
            g2.setFont(font);

        int size= ((ShapeContainer)shapeGraphic).getNumChildren();
        Rectangle r = shape.getBounds();
        g2.drawString(shapeGraphic.getLabel()+" ("+size+")", (int) (r.x+xoffset), (int) (r.y+yoffset));
    }
}
