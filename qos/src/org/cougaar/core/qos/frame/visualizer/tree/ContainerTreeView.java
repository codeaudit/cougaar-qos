package org.cougaar.core.qos.frame.visualizer.tree;


import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.icons.IconFactory;
import org.cougaar.core.qos.frame.Frame;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.net.URL;
import java.io.IOException;
import java.awt.*;
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
    ShapeTableCellRenderer cellRenderer;
    FrameTableCellRenderer frameCellRenderer;


    public ContainerTreeView() {
        super();
        frameModel = new FrameTableModel();
        shapeModel = new ShapeGraphicTableModel();
        cellRenderer = new ShapeTableCellRenderer(false, shapeModel);
        frameCellRenderer = new FrameTableCellRenderer(false, true, frameModel);

        Icon containerIcon = IconFactory.getIcon(IconFactory.CONTAINER_ICON);
        Icon componentIcon = IconFactory.getIcon(IconFactory.COMPONENT_ICON);
        Icon prototypeIcon = IconFactory.getIcon(IconFactory.CONTAINER_PROTOTYPE_ICON);
        Icon frameIcon     = IconFactory.getIcon(IconFactory.FRAME_ICON);
        tree.setCellRenderer(new ContainerRenderer(containerIcon, componentIcon, prototypeIcon, frameIcon));
    }

    protected void displayShapeGraphicInTable(ShapeGraphic g) {
        shapeModel.clear();
        shapeModel.set(g);
        editTable.setDefaultRenderer(Object.class, cellRenderer);
        editTable.setModel(shapeModel);
    }

    protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
        frameModel.clear();
        frameModel.set(frame);
        editTable.setDefaultRenderer(Object.class, frameCellRenderer);
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



    private class ContainerRenderer extends DefaultTreeCellRenderer {
        Icon containerIcon;
        Icon componentIcon;
        Icon prototypeIcon;
        Icon frameIcon;


        public ContainerRenderer(Icon containerIcon, Icon componentIcon, Icon prototypeIcon, Icon frameIcon) {
            super();
            this.containerIcon = containerIcon;
            this.componentIcon = componentIcon;
            this.prototypeIcon = prototypeIcon;
            this.frameIcon = frameIcon;
        }

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

            if (value instanceof FrameNode) {
                setIcon(frameIcon);
            } else if (value instanceof ShapeGraphicNode) {
                ShapeGraphicNode node = (ShapeGraphicNode) value;
                ShapeGraphic g = node.getShapeGraphic();
                if (g != null) {
                    if (g.isPrototype())
                        setIcon(prototypeIcon);
                    else setIcon(g.isContainer() ? containerIcon : componentIcon);
                }

            } else setIcon(null);

            return this;
        }
    }


    class ShapeTableCellRenderer extends JLabel implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder   = null;
        boolean isBordered = true;
        ShapeGraphicTableModel shapeModel;

        public ShapeTableCellRenderer(boolean isBordered, ShapeGraphicTableModel shapeModel) {
            this.isBordered = isBordered;
            this.shapeModel = shapeModel;
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            setText(value.toString());
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }

}
