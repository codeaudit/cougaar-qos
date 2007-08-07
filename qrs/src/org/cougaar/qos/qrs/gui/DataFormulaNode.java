/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.cougaar.qos.qrs.DataFormula;
import org.cougaar.qos.qrs.DataValue;

public class DataFormulaNode extends VectorTreeNode {
    private final DataFormula formula;
    private SubscribersNode subscribers;
    private DependsOnNode dependsOn;

    public DataFormulaNode(TreeNode parent, DataFormula formula) {
        super(parent, formula);
        this.formula = formula;
    }

    public void updateChildren(DefaultTreeModel model) {
        // force a repaint ?
        if (subscribers == null) {
            subscribers = new SubscribersNode();
            addChild(subscribers, model);
        }
        subscribers.updateChildren(model);
        if (dependsOn == null) {
            dependsOn = new DependsOnNode();
            addChild(dependsOn, model);
        }
        dependsOn.updateChildren(model);
    }

    public String toString() {
        DataValue value = formula.query();
        // String name = formula.getName();
        // String[] args = formula.getArgs();
        // String prefix = name + "(";
        // if (args != null) {
        // if (args.length > 0) prefix += args[0];
        // for (int i=1; i<args.length; i++) {
        // prefix += ", "+args[i];
        // }
        // }
        // prefix += ")";

        String prefix = formula.toString();

        if (value == null) {
            return prefix + ":<no value>";
        } else {
            return prefix + ":" + value.toString();
        }
    }

    class SubscribersNode extends VectorTreeNode {
        private SubscribersNode() {
            super(DataFormulaNode.this, null);
        }

        public String toString() {
            return "Subscribers";
        }

        public void updateChildren(DefaultTreeModel model) {
            ArrayList subscribers = formula.getSubscribers();
            Object subscriber;
            DataTreeNode node;
            synchronized (subscribers) {
                Iterator itr = subscribers.iterator();
                // *** We need to deal with dropped children
                while (itr.hasNext()) {
                    subscriber = itr.next();
                    node = getChild(subscriber);
                    if (node == null) {
                        node = new LeafNode(this, subscriber);
                        addChild(node, model);
                    }
                }
            }
        }

    }

    class DependsOnNode extends VectorTreeNode {
        private DependsOnNode() {
            super(DataFormulaNode.this, null);
        }

        public String toString() {
            return "Depends On";
        }

        public void updateChildren(DefaultTreeModel model) {
            ArrayList subscribers = formula.getDependencies();
            Object dependency;
            DataTreeNode node;
            synchronized (subscribers) {
                Iterator itr = subscribers.iterator();
                // *** We need to deal with dropped children
                while (itr.hasNext()) {
                    dependency = itr.next();
                    node = getChild(dependency);
                    if (node == null) {
                        node = new LeafNode(this, dependency);
                        addChild(node, model);
                    }
                }
            }
        }

    }

}
