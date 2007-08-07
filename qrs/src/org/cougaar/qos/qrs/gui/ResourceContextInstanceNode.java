/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.cougaar.qos.qrs.DataFormula;
import org.cougaar.qos.qrs.ResourceContext;

public class ResourceContextInstanceNode extends VectorTreeNode {
    private final ResourceContext context;

    public ResourceContextInstanceNode(TreeNode parent, ResourceContext context) {
        super(parent, context);
        this.context = context;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        Object[] parameters = context.getParameters();
        for (Object element : parameters) {
            buf.append(' ');
            buf.append(element.toString());
        }
        return buf.toString();
    }

    public void updateChildren(DefaultTreeModel model) {
        List<String> contexts = context.getContextClasses();
        synchronized (contexts) {
            for (String context_class : contexts) {
                DataTreeNode node = getChild(context_class);
                if (node == null) {
                    node = new ResourceContextClassNode(this, context, context_class);
                    addChild(node, model);
                }
                node.updateChildren(model);
            }
            for (String kind : context.getFormulaKinds()) {
                List<DataFormula >formulas = context.getFormulasForKind(kind);
                synchronized (formulas) {
                    for (DataFormula formula : formulas) {
                        DataTreeNode node = getChild(formula);
                        if (node == null) {
                            node = new DataFormulaNode(this, formula);
                            addChild(node, model);
                        }
                        node.updateChildren(model);
                    }
                }
            }
        }
    }

}
