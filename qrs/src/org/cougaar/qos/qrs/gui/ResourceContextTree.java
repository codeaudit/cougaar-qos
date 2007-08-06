/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

public class ResourceContextTree 
    extends JTree 
    implements TreeSelectionListener
{
    private Object selectedObject;

    public ResourceContextTree(ResourceContextTreeModel model) 
    {
	super(model);
	setCellRenderer(new Renderer());
	TreeSelectionModel m = getSelectionModel();
	m.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	m.addTreeSelectionListener(this);
    }

    // all nodes are DataTreeNodes...
    public void valueChanged(TreeSelectionEvent event)
    {
	boolean on_off = event.isAddedPath();
	if (!on_off) {
	    selectedObject = null;
	} else {
	    TreePath path = event.getPath();
	    DataTreeNode node = (DataTreeNode) path.getLastPathComponent();
	    selectedObject = node.getDatum();
	}
	repaint();
    }


    private class Renderer extends DefaultTreeCellRenderer 
    {
	public Component getTreeCellRendererComponent(JTree tree,
						      Object object,
						      boolean set,
						      boolean expanded,
						      boolean leaf,
						      int row,
						      boolean hasFocus)
	{
	    Component comp = super.getTreeCellRendererComponent(tree,
								object,
								set,
								expanded,
								leaf,
								row,
								hasFocus);
	    DataTreeNode node = (DataTreeNode) object;
	    if (selectedObject != null && node.getDatum() == selectedObject) {
		comp.setForeground(Color.red);
	    } 
	    return comp;
	}
						      
						      
    }

}
