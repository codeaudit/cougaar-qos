package org.cougaar.core.qos.frame.visualizer.tree;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.cougaar.core.qos.frame.PrototypeFrame;
import org.cougaar.core.qos.frame.visualizer.FrameModel;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.icons.IconFactory;

  /**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 8:57:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class FrameInheritenceView extends ExplorerView {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   FrameTableModel frameModel;
    FrameModel frameAppModel;

    public FrameInheritenceView() {
        super();
        frameModel = new FrameTableModel();
        Icon frameIcon = IconFactory.getIcon(IconFactory.FRAME_ICON);
        Icon protoIcon = IconFactory.getIcon(IconFactory.FRAME_PROTOYPE_ICON);
        tree.setCellRenderer(new FrameRenderer(frameIcon, protoIcon));
        editTable.setDefaultRenderer(Object.class, new FrameTableCellRenderer(false, true, frameModel));
        root = new FrameNode("");
        tree.setModel(new DefaultTreeModel(root));
    }

    @Override
   protected void displayShapeGraphicInTable(ShapeGraphic g) {
    }

    @Override
   protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
        frameModel.clear();
        frameModel.set(frame);
        editTable.setModel(frameModel);
    }

    public void setFrame(org.cougaar.core.qos.frame.Frame frame) {
        buildInheritenceTree(frame);
        displayFrameInTable(frame);
    }


    protected void buildInheritenceTree(org.cougaar.core.qos.frame.Frame frame) {
	    ArrayList frames = new ArrayList();
        org.cougaar.core.qos.frame.Frame p = frame;
        while (p != null) {
            frames.add(0, p);
            p = p.getPrototype();
            //p = null;
        }

        root = new FrameNode((org.cougaar.core.qos.frame.Frame)frames.get(0));
        frames.remove(0);
        DefaultMutableTreeNode parent = root, node;
        while (frames.size() > 0) {
            node = new FrameNode((org.cougaar.core.qos.frame.Frame)frames.get(0));
            frames.remove(0);
            parent.add(node);
            parent = node;
        }
	    tree.setModel(new DefaultTreeModel(root));
        TreePath path = (parent != null ? (new TreePath( ((DefaultMutableTreeNode)parent.getParent()).getPath())) : null);
        if (path != null)
           tree.expandPath(path);
    }



    private class FrameRenderer extends DefaultTreeCellRenderer {
        /**
       * 
       */
      private static final long serialVersionUID = 1L;
      Icon frameIcon;
        Icon prototypeIcon;

        public FrameRenderer(Icon frameIcon, Icon prototypeIcon) {
            super();
            this.frameIcon = frameIcon;
            this.prototypeIcon = prototypeIcon;
        }

        @Override
      public Component getTreeCellRendererComponent(
                                                    JTree tree,
                                                    Object value,
                                                    boolean sel,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel,
                                               expanded, leaf, row,
                                               hasFocus);

            //System.out.println(value.getClass().getName());
            if (value instanceof FrameNode) {
                FrameNode node = (FrameNode) value;
                org.cougaar.core.qos.frame.Frame f = node.getFrame();
                if (f instanceof PrototypeFrame)
                    setIcon(prototypeIcon);
                else
                    setIcon(frameIcon);
            } else
                setIcon(null);
            return this;
        }
    }

}
