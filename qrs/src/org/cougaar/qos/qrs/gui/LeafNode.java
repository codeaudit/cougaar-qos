/*
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright> 
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
