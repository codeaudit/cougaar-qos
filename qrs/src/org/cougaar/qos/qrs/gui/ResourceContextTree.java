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
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class ResourceContextTree extends JTree implements TreeSelectionListener {
    private final Renderer renderer;
    
    public ResourceContextTree(ResourceContextTreeModel model) {
        super(model);
        renderer = new Renderer();
        setCellRenderer(renderer);
        TreeSelectionModel m = getSelectionModel();
        m.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m.addTreeSelectionListener(this);
    }

    // all nodes are DataTreeNodes...
    public void valueChanged(TreeSelectionEvent event) {
        boolean on_off = event.isAddedPath();
        if (!on_off) {
            renderer.setSelectedNode(null);
        } else {
            TreePath path = event.getPath();
            renderer.setSelectedNode((DataTreeNode) path.getLastPathComponent());
        }
        repaint();
    }

    private static class Renderer extends DefaultTreeCellRenderer {
        private static final Color SELECTED_COLOR = Color.red;
        private static final Color ANCESTOR_COLOR = new Color(205, 160, 121);
        private DataTreeNode selectedNode;
        
        private void setSelectedNode(DataTreeNode node) {
            this.selectedNode = node;
        }
        
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
            if (selectedNode != null) {
                Object selectedDatum = selectedNode.getDatum();
                if (selectedDatum != null) {
                    // Colorize cells that either have the given datum or
                    // are ancestors of such cells, using different colors
                    // for the two cases.
                    DataTreeNode node = (DataTreeNode) object;
                    if (selectedDatum == node.getDatum()) {
                        comp.setForeground(SELECTED_COLOR);
                    } else if (isDescendantSelected(node, selectedDatum)) {
                        comp.setForeground(ANCESTOR_COLOR);
                    }
                }
            }
            return comp;
        }

        private boolean isDescendantSelected(DataTreeNode node, Object selectedDatum) {
            if (selectedDatum == node.getDatum()) {
                return true;
            } else {
                @SuppressWarnings("unchecked")
                Enumeration<DataTreeNode> e = node.children();
                while (e.hasMoreElements()) {
                    if (isDescendantSelected(e.nextElement(), selectedDatum)) {
                        return true;
                    }
                }
                return false;
            }
        }

    }

}
