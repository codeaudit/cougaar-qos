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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

abstract class VectorTreeNode implements DataTreeNode {
    static final Enumeration<Object> NULL_ENUM = new NullEnum();

    private final Vector<DataTreeNode> children;
    private final Map<Object, DataTreeNode> children_2;
    private final TreeNode parent;
    private final Object datum;

    public VectorTreeNode(TreeNode parent, Object object) {
        this.parent = parent;
        this.datum = object;
        this.children = new Vector<DataTreeNode>();
        this.children_2 = new HashMap<Object, DataTreeNode>();
    }

    protected void addChild(DataTreeNode child, DefaultTreeModel model) {
        children.add(child);
        children_2.put(child.getDatum(), child);
        if (model != null) {
            model.reload(this);
        }
    }

    // DataTreeNode
    public Object getDatum() {
        return datum;
    }

    public DataTreeNode getChild(Object datum) {
        return children_2.get(datum);
    }

    // TreeNode

    public boolean getAllowsChildren() {
        return true;
    }

    public int getChildCount() {
        return children == null ? 0 : children.size();
    }

    public boolean isLeaf() {
        return false;
    }

    public TreeNode getParent() {
        return parent;
    }

    public Enumeration<?> children() {
        return children == null ? NULL_ENUM : children.elements();
    }

    public TreeNode getChildAt(int index) {
        return children.elementAt(index);
    }

    public int getIndex(TreeNode child) {
        return children.indexOf(child);
    }
    
    static class NullEnum implements Enumeration<Object> {
        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            return null;
        }
    }
}
