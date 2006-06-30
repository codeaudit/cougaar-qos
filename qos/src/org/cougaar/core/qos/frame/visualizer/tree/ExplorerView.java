package org.cougaar.core.qos.frame.visualizer.tree;
import java.awt.Component;

import javax.swing.JTable;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 8:57:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExplorerView extends TreeView {
    protected JTable editTable;


    public ExplorerView() {
        super();
        init();
    }

    protected Component createOtherComponent() {
        editTable = new JTable();
        return editTable;
    }

    /** TreeSelectionListener interface. */
   protected void treeSelected(Object data) {
        if (data instanceof ShapeGraphic) {
            ShapeGraphic graphic = (ShapeGraphic) data;
            displayShapeGraphicInTable(graphic);
        } else if (data instanceof org.cougaar.core.qos.frame.Frame) {
            displayFrameInTable((org.cougaar.core.qos.frame.Frame)data);
        }
    }

    protected void displayShapeGraphicInTable(ShapeGraphic g) {
    }

    protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
    }
}
