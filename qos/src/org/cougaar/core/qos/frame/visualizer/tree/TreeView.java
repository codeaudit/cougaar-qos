package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 27, 2005
 * Time: 9:59:49 AM
 * To change this template use File | Settings | File Templates.
 */

public abstract class TreeView extends JPanel implements TreeSelectionListener {
    protected JTree tree;
    protected Component otherComponent;
    protected DefaultMutableTreeNode root;
    //private static String lineStyle = "Angled";//"Horizontal";  //"Angled" (the default), "Horizontal", and "None".
    protected static boolean useSystemLookAndFeel = true;



    public TreeView() {
        super(new GridLayout(1,0));
        setOpaque(true);
    }
    protected void init() {
        root = new ShapeGraphicNode();
        tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        //tree.putClientProperty("JTree.lineStyle", lineStyle);

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);
        otherComponent = createOtherComponent();
        JScrollPane compView = new JScrollPane(otherComponent);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(compView);

        Dimension minimumSize = new Dimension(100, 100);
        compView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(200);
        splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this panel.
        add(splitPane);


        MouseListener ml = new MouseAdapter() {
             public void mousePressed(MouseEvent e) {
                 int selRow = tree.getRowForLocation(e.getX(), e.getY());
                 TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                 if(selRow != -1) {
                     if(e.getClickCount() == 1) {
                         singleClick(selRow, selPath);
                     }
                     else if(e.getClickCount() == 2) {
                         doubleClick(selRow, selPath);
                     }
                 }
             }
         };
        tree.addMouseListener(ml);
    }

    protected abstract Component createOtherComponent();

    protected abstract void treeSelected(Object data);

    /** TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        treeSelected(nodeInfo);
        //if (node.isLeaf()) {
    }


    protected void singleClick(int selRow, TreePath selPath) {
    }

    protected void doubleClick(int setRow, TreePath selPath) {
    }

    protected void displayShapeGraphicInTable(ShapeGraphic g) {
    }

    protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
    }
}

