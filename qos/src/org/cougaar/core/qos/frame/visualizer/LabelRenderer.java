package org.cougaar.core.qos.frame.visualizer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 29, 2005
 * Time: 1:24:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class LabelRenderer {
    /*private static HashMap labelRenderers = new HashMap();
    public static void add(LabelRenderer r) {
        if (labelRenderers.get(r.getName()) == null)
            labelRenderers.put(r.getName(), r);
    }
    public static LabelRenderer get(String name) {
        return (LabelRenderer) labelRenderers.get(name);
    }   */

    protected String rendererName;
    protected double xoffset, yoffset;
    protected Paint paint = null;
    protected Font font = null;
    boolean onMouseOverOnly = false;
    protected boolean mouseIsOverShape=false;

    public LabelRenderer(String rendererName, double xoff, double yoff) {
       this(rendererName, xoff, yoff, null, null);
    }
    public LabelRenderer(String rendererName, double xoff, double yoff, Font font) {
        this(rendererName, xoff, yoff, font, null);
    }
    public LabelRenderer(String rendererName, double xoff, double yoff, Paint paint) {
        this(rendererName, xoff, yoff, null, paint);
    }
    public LabelRenderer(String rendererName, double xoff, double yoff, Font font, Paint paint) {
        this.rendererName = rendererName;
        this.xoffset = xoff;
        this.yoffset = yoff;
        this.font = font;
        this.paint = paint;
    }
    public void setMouseOverOnly(boolean onmouseover) {
        onMouseOverOnly = onmouseover;
    }

    public Paint getPaint() {
        return paint;    
    }

    public String getName() {
        return rendererName;
    }
    public void drawLabel(Graphics2D g2, ShapeGraphic shapeGraphic) {
        if (shapeGraphic == null || shapeGraphic.getLabel()==null)
            return;
        if (onMouseOverOnly && !shapeGraphic.isMouseOver())
            return;
        Shape shape = shapeGraphic.getShape();
        if (shape == null)
            return;
        if (paint != null)
            g2.setPaint(paint);
        //int sw = getFontMetrics(f).stringWidth(f.getName());
        if (font != null)
            g2.setFont(font);

        Rectangle r = shape.getBounds();
        g2.drawString(shapeGraphic.getLabel(), (int) (r.x+xoffset), (int) (r.y+yoffset));
    }
}
