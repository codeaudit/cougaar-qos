package org.cougaar.core.qos.frame.visualizer.tree;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.Frame;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 8:57:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExplorerView extends JPanel implements TreeSelectionListener {
    protected JTree tree;
    protected DefaultMutableTreeNode root;
    protected JTable editTable;
    //private static String lineStyle = "Horizontal";  //"Angled" (the default), "Horizontal", and "None".
    protected static boolean useSystemLookAndFeel = true;



    public ExplorerView() {
        super(new GridLayout(1,0));
        setOpaque(true);
        root = new ShapeGraphicNode();
        tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        //tree.putClientProperty("JTree.lineStyle", lineStyle);


        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        editTable = new JTable();
        JScrollPane tableView = new JScrollPane(editTable);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(tableView);

        Dimension minimumSize = new Dimension(100, 100);
        tableView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(300);
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


    /** TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        //if (node.isLeaf()) {
        if (nodeInfo instanceof ShapeGraphic) {
            ShapeGraphic graphic = (ShapeGraphic) nodeInfo;
            displayShapeGraphicInTable(graphic);
        } else if (nodeInfo instanceof org.cougaar.core.qos.frame.Frame) {
            displayFrameInTable((org.cougaar.core.qos.frame.Frame)nodeInfo);
        }
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
