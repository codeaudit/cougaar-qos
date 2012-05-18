package org.cougaar.core.qos.frame.visualizer.layout;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 11:26:52 AM
 * To change this template use File | Settings | File Templates.
 */

public class VerticalBoxLayout extends ShapeLayout {

    public VerticalBoxLayout() {
        super();
    }

    @Override
   public void doLayout(ShapeContainer container) {
        Rectangle2D.Double bounds = (Rectangle2D.Double) container.getBounds2D();
        Collection children = container.getChildren();
        ShapeGraphic child;
        double totalheight = bounds.height - (topMargin+bottomMargin);
        double heightSum=0;
        double nHeight, i;
        Rectangle2D.Double chBounds;

        i=0d;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            child = (ShapeGraphic) ii.next();
            chBounds = (Rectangle2D.Double) child.getBounds2D();
            nHeight = chBounds.getHeight();
            child.setVisible( (heightSum >= totalheight ? false : true));
            child.reshape(bounds.x+leftMargin, bounds.y+topMargin+heightSum, Math.min(chBounds.getWidth(), bounds.getWidth()-(leftMargin+rightMargin)), nHeight);
            heightSum += (nHeight+vpadding);
            i++;
        }
    }
}
