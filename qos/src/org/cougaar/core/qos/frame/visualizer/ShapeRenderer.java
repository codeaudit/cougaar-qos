package org.cougaar.core.qos.frame.visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 2, 2005
 * Time: 10:58:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeRenderer {


    public final static Paint DEFAULT_PAINT = Color.blue;
    public final static Paint DEFAULT_SELECTED_PAINT = Color.magenta;
    public final static Paint DEFAULT_FILL_PAINT = Color.blue;
    public final static Paint DEFAULT_SELECTED_FILL_PAINt = Color.blue.brighter();
    public final static int   DEFAULT_LINE_WIDTH = 2;

    String rendererName;
    Paint  paint = DEFAULT_PAINT, selectedPaint = DEFAULT_SELECTED_PAINT;
    Paint  fillPaint=DEFAULT_FILL_PAINT, selectedFillPaint = DEFAULT_SELECTED_FILL_PAINt;
    BasicStroke lineStroke = new BasicStroke(DEFAULT_LINE_WIDTH);
    boolean drawBorder = true, drawFilled=false;


    public ShapeRenderer() {
    }
    public ShapeRenderer(String rendererName, Paint paint, Paint selectedPaint, Paint fillPaint, Paint selectedFillPaint,
                         int lineWidth, boolean bordered, boolean filled) {
        this.rendererName = rendererName;
        this.paint = paint;
        this.selectedPaint = selectedPaint;
        this.fillPaint = fillPaint;
        this.selectedFillPaint = selectedFillPaint;
        this.lineStroke = new BasicStroke(lineWidth);
        this.drawBorder = bordered;
        this.drawFilled = filled;
        //if (paint == null || selectedPaint == null)
          //  throw new NullPointerException("paint or selecrtedPaint is null");
    }

    public void setName(String name) {
        this.rendererName = name;
    }

    public String getName() {
        return rendererName;
    }

    public void drawShape(Graphics2D g2, ShapeGraphic shapeGraphic) {
        if (shapeGraphic == null || shapeGraphic.getShape()==null)
            return;
        Paint drawP = (shapeGraphic.isSelected() ? selectedPaint : paint);
        Paint fillP = (shapeGraphic.isSelected() ? selectedFillPaint : fillPaint);
        Shape shape = shapeGraphic.getShape();
        if (drawBorder && drawP != null) {
            g2.setPaint(drawP);
            g2.setStroke(lineStroke);
            g2.draw(shape);
        }
        if (drawFilled) {
            g2.setPaint((fillP != null ? fillP : drawP));
            g2.fill(shape);
        }
    }
}
