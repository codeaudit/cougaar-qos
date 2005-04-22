package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:30:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeGraphicNode extends DefaultMutableTreeNode {
        ShapeGraphic graphic;

        public ShapeGraphicNode() {
            super();
        }

        public ShapeGraphicNode(ShapeGraphic graphic) {
            super(graphic);
            this.graphic = graphic;
        }
    
    public String toString() {
	return (graphic != null ? graphic.getId() : "null");
    }

    }
