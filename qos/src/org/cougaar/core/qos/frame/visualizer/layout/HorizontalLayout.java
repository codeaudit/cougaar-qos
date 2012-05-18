package org.cougaar.core.qos.frame.visualizer.layout;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 5, 2005
 * Time: 9:13:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class HorizontalLayout extends ShapeLayout {

    public HorizontalLayout() {
        super();
    }

    @Override
   public void doLayout(ShapeContainer container) {
        Rectangle2D.Double bounds = (Rectangle2D.Double) container.getBounds2D();
        Collection children = container.getChildren();
        ShapeGraphic child;
        double totalwidth = bounds.width-(leftMargin+rightMargin);
        double totalheight= bounds.height-(topMargin+bottomMargin);
        int num = children.size();
        double pnWidth, nWidth, i;

        nWidth = totalwidth / num;
        pnWidth= nWidth-(2*hpadding);
        i=0d;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            child = (ShapeGraphic) ii.next();
            child.reshape(bounds.x+leftMargin+(nWidth*i)+vpadding, bounds.y+topMargin+vpadding, pnWidth, totalheight-(2*vpadding));
            //System.out.println("child.setSize()  x="+(nWidth*i)+" y="+(bounds.y+1d)+" width="+nWidth+" height="+(bounds.height-2d));
            i++;
        }
    }
}
