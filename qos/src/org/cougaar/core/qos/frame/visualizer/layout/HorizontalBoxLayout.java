package org.cougaar.core.qos.frame.visualizer.layout;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 11:09:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class HorizontalBoxLayout extends ShapeLayout {

    public HorizontalBoxLayout() {
        super();
    }

    public void doLayout(ShapeContainer container) {
        Rectangle2D.Double bounds = (Rectangle2D.Double) container.getBounds2D();
        Collection children = container.getChildren();
        ShapeGraphic child;
        double totalwidth = bounds.width-(leftMargin+rightMargin);
        double totalheight= bounds.height-(topMargin+bottomMargin);
        double widthSum=0;
        int num = children.size();
        double x,y, nWidth, i;

        Rectangle2D.Double chBounds;
        i=0d;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            child = (ShapeGraphic) ii.next();
            chBounds = (Rectangle2D.Double) child.getBounds2D();
            nWidth = chBounds.getWidth();
            child.reshape(bounds.x+leftMargin+widthSum, bounds.y+topMargin, nWidth, Math.min(chBounds.getHeight(), bounds.getHeight()-(leftMargin+rightMargin)));
            widthSum += (nWidth+hpadding);
            child.setVisible( (widthSum >= totalwidth ? false : true));
            //System.out.println("child.setSize()  x="+(nWidth*i)+" y="+(bounds.y+1d)+" width="+nWidth+" height="+(bounds.height-2d));
            i++;
        }
    }
}
