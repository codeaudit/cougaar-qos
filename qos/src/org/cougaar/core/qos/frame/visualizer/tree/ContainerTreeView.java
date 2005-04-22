package org.cougaar.core.qos.frame.visualizer.tree;


import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.Frame;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
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
public class ContainerTreeView extends ExplorerView {
    FrameTableModel frameModel;
    ShapeGraphicTableModel shapeModel;

    public ContainerTreeView() {
        super();
        frameModel = new FrameTableModel();
        shapeModel = new ShapeGraphicTableModel();
    }

    protected void displayShapeGraphicInTable(ShapeGraphic g) {
        shapeModel.clear();
        shapeModel.set(g);
        editTable.setModel(shapeModel);
    }

    protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
        frameModel.clear();
        frameModel.set(frame);
        editTable.setModel(frameModel);
    }

     public void buildContainerTree(ShapeContainer rootContainer) {
        root = buildTree(null, rootContainer);
	tree.setModel(new DefaultTreeModel(root));
     }

     public DefaultMutableTreeNode buildTree(DefaultMutableTreeNode parent, Object userObject) {
        DefaultMutableTreeNode newNode = null;

        if (userObject instanceof ShapeGraphic) {
            ShapeGraphic graphic = (ShapeGraphic)userObject;
            newNode = new ShapeGraphicNode(graphic);

            if (graphic.hasFrame())
                buildTree(newNode, graphic.getFrame());
            if (graphic.isContainer()) {
                ShapeContainer sc = (ShapeContainer) graphic;
                ShapeGraphic g;
                for (Iterator ii=sc.getPrototypes().iterator(); ii.hasNext();) {
                    g = (ShapeGraphic) ii.next();
                    buildTree(newNode, g);
                }
		for (Iterator ii=sc.getChildren().iterator(); ii.hasNext();) {
                    g = (ShapeGraphic) ii.next();
                    buildTree(newNode, g);
                }
		
            }

        }  else if (userObject instanceof org.cougaar.core.qos.frame.Frame) {
            org.cougaar.core.qos.frame.Frame frame = (org.cougaar.core.qos.frame.Frame)userObject;
            newNode = new FrameNode(frame);
        }
        if (parent != null && newNode != null)
            parent.add(newNode);
         return newNode;
     }











    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
   /*
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("TreeDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        TreeDemo newContentPane = new TreeDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    } */
}
