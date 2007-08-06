/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;


import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.cougaar.qos.qrs.DataFormula;
import org.cougaar.qos.qrs.ResourceContext;

public class ResourceContextInstanceNode extends VectorTreeNode
{
    private ResourceContext context;

    public ResourceContextInstanceNode(TreeNode parent, ResourceContext context)
    {
	super(parent, context);
	this.context = context;
    }

    public String toString() 
    {
	StringBuffer buf = new StringBuffer();
	Object[] parameters = context.getParameters();
	for (int i=0; i<parameters.length; i++) {
	    buf.append(' ');
	    buf.append(parameters[i].toString());
	}
	return buf.toString();
    }

    public void updateChildren(DefaultTreeModel model) 
    {
	ArrayList contexts = context.getContextClasses();
	ArrayList formulas;
	Iterator itr, formula_itr;
	String kind;
	DataFormula formula;
	DataTreeNode node;
	synchronized (contexts) {
	    itr = contexts.iterator();
	    while (itr.hasNext()) {
		String context_class = (String) itr.next();
		node = getChild(context_class);
		if (node == null) {
		    node = new ResourceContextClassNode(this, context, context_class);
		    addChild(node, model);
		}
		node.updateChildren(model);
	    }
	    itr = context.getFormulaKinds().iterator();
	    while (itr.hasNext()) {
		kind = (String) itr.next();
		formulas = context.getFormulasForKind(kind);
		synchronized (formulas) {
		    formula_itr = formulas.iterator();
		    while (formula_itr.hasNext()) {
			formula = (DataFormula) formula_itr.next();
			node = getChild(formula);
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
    
    
