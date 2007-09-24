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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

public class ResourceContextTree extends JTree implements TreeSelectionListener {
    private Object selectedObject;

    public ResourceContextTree(ResourceContextTreeModel model) {
        super(model);
        setCellRenderer(new Renderer());
        TreeSelectionModel m = getSelectionModel();
        m.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m.addTreeSelectionListener(this);
    }

    // all nodes are DataTreeNodes...
    public void valueChanged(TreeSelectionEvent event) {
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

    private class Renderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object object,
                                                      boolean set,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            Component comp =
                    super.getTreeCellRendererComponent(tree,
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
