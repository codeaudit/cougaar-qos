/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;

class LeafNode implements DataTreeNode {
    private final TreeNode parent;
    private final Object datum;

    public LeafNode(TreeNode parent, Object datum) {
        this.parent = parent;
        this.datum = datum;
    }

    public String toString() {
        return datum.toString();
    }

    // DataTreeNode
    public Object getDatum() {
        return datum;
    }

    public DataTreeNode getChild(Object child) {
        return null;
    }

    public void updateChildren(DefaultTreeModel model) {
    }

    // TreeNode
    public boolean getAllowsChildren() {
        return false;
    }

    public int getChildCount() {
        return 0;
    }

    public boolean isLeaf() {
        return true;
    }

    public TreeNode getParent() {
        return parent;
    }

    public java.util.Enumeration<?> children() {
        return VectorTreeNode.NULL_ENUM;
    }

    public TreeNode getChildAt(int index) {
        return null;
    }

    public int getIndex(TreeNode child) {
        return -1;
    }

}
