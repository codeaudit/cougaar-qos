package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 11:43:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeGroup extends RectangularContainer  {

    public ShapeGroup() {
        super();   
    }
    
    public ShapeGroup(String id, String label, ShapeLayout layout) {
        super(id,label, layout);
    }

    public void draw(Graphics2D g2) {
        if (paint != null && selectedPaint != null)
            g2.setPaint((selected ? selectedPaint : paint));
        //if (id != null)
	//  g2.drawString(id, (int)rectShape.x, (int)(rectShape.y-2));
        //g2.draw(shape);
        drawChildren(g2);
    }
}
