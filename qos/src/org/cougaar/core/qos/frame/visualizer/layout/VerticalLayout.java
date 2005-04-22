package org.cougaar.core.qos.frame.visualizer.layout;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 5, 2005
 * Time: 9:14:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class VerticalLayout extends ShapeLayout {

    public VerticalLayout() {
        super();
    }

    public void doLayout(ShapeContainer container) {
        Rectangle2D.Double bounds = (Rectangle2D.Double) container.getBounds2D();
        Collection children = container.getChildren();
        ShapeGraphic child;
        int num = children.size();
        double totalheight = bounds.height - (topMargin+bottomMargin);
        double totalwidth = bounds.width - (leftMargin+rightMargin);
        double x,y, pnHeight, nHeight, i;

        nHeight = totalheight / (double)num;
        pnHeight= nHeight - (2*vpadding);
        i=0d;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            child = (ShapeGraphic) ii.next();
            child.reshape(bounds.x+leftMargin+hpadding, bounds.y+topMargin+((nHeight*i)+vpadding), totalwidth-(2*hpadding), pnHeight);
            i++;
        }
    }
}
