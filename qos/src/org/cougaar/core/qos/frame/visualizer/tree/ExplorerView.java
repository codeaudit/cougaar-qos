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
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   protected JTable editTable;


    public ExplorerView() {
        super();
        init();
    }

    @Override
   protected Component createOtherComponent() {
        editTable = new JTable();
        return editTable;
    }

    /** TreeSelectionListener interface. */
   @Override
   protected void treeSelected(Object data) {
        if (data instanceof ShapeGraphic) {
            ShapeGraphic graphic = (ShapeGraphic) data;
            displayShapeGraphicInTable(graphic);
        } else if (data instanceof org.cougaar.core.qos.frame.Frame) {
            displayFrameInTable((org.cougaar.core.qos.frame.Frame)data);
        }
    }

    @Override
   protected void displayShapeGraphicInTable(ShapeGraphic g) {
    }

    @Override
   protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
    }
}
