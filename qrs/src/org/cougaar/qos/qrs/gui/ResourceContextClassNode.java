/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.cougaar.qos.qrs.ResourceContext;

public class ResourceContextClassNode extends VectorTreeNode {
    private final String context_class;
    private final ResourceContext context;

    public ResourceContextClassNode(TreeNode parent, ResourceContext context, String context_class) {
        super(parent, context_class);
        this.context_class = context_class;
        this.context = context;
    }

    public String toString() {
        return context_class;
    }

    public void updateChildren(DefaultTreeModel model) {
        List<ResourceContext> contexts = context.getContextsForClass(context_class);
        // *** Need to handle deleted children
        synchronized (contexts) {
            for (ResourceContext subcontext : contexts) {
                DataTreeNode node = getChild(subcontext);
                if (node == null) {
                    node = new ResourceContextInstanceNode(this, subcontext);
                    addChild(node, model);
                }
                node.updateChildren(model);
            }
        }
    }

}
