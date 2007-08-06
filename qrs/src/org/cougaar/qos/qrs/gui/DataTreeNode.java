/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

interface DataTreeNode extends TreeNode
{
    Object getDatum();
    DataTreeNode getChild(Object datum);
    void updateChildren(DefaultTreeModel model);
}
