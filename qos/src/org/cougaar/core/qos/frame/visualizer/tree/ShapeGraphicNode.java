package org.cougaar.core.qos.frame.visualizer.tree;

import java.util.HashSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:30:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeGraphicNode extends DefaultMutableTreeNode {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
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

    @Override
   public String toString() {
	    return (graphic != null ? graphic.getId() : "");
    }

    public ShapeGraphic getShapeGraphic() {
        return graphic;
    }


    public boolean hasChild(TreeNode child) {
        return (cachedChildren.contains(child));
    }

    @Override
   public void insert(MutableTreeNode child, int index) {
        cachedChildren.add(child);
        super.insert(child, index);
    }

    @Override
   public void add(MutableTreeNode newChild) {
        cachedChildren.add(newChild);
        super.add(newChild);
    }

    @Override
   public void remove(int childIndex) {
        TreeNode child = getChildAt(childIndex);
        cachedChildren.remove(child);
        super.remove(childIndex);
    }
    @Override
   public void remove(MutableTreeNode aChild) {
        cachedChildren.remove(aChild);
        super.remove(aChild);
    }

    @Override
   public void removeAllChildren() {
        cachedChildren.clear();
        super.removeAllChildren();
    }

}
