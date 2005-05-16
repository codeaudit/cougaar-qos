package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:30:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeGraphicNode extends DefaultMutableTreeNode {
    ShapeGraphic graphic;
    HashSet cachedChildren;

    public ShapeGraphicNode() {
        super("");
        graphic = null;
        cachedChildren = new HashSet();
    }

    public ShapeGraphicNode(ShapeGraphic graphic) {
        this(graphic, null);
    }

    public ShapeGraphicNode(ShapeGraphic graphic, ShapeGraphicNode parent) {
        super(graphic);
        this.graphic = graphic;
        cachedChildren = new HashSet();
        if (parent != null)
            parent.add(this);
    }

    public String toString() {
	    return (graphic != null ? graphic.getId() : "");
    }

    public ShapeGraphic getShapeGraphic() {
        return graphic;
    }


    public boolean hasChild(TreeNode child) {
        return (cachedChildren.contains(child));
    }

    public void add(MutableTreeNode newChild) {
        cachedChildren.add(newChild);
        super.add(newChild);
    }

    public void remove(int childIndex) {
        TreeNode child = getChildAt(childIndex);
        cachedChildren.remove(child);
        super.remove(childIndex);
    }
    public void remove(MutableTreeNode aChild) {
        cachedChildren.remove(aChild);
        super.remove(aChild);
    }

    public void removeAllChildren() {
        cachedChildren.clear();
        super.removeAllChildren();
    }

}
