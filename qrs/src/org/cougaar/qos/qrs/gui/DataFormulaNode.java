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

import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.cougaar.qos.qrs.DataFormula;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.DataValueChangedCallbackListener;

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

    @Override
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

        @Override
      public String toString() {
            return "Subscribers";
        }

        public void updateChildren(DefaultTreeModel model) {
            List<DataValueChangedCallbackListener> subscribers = formula.getSubscribers();
            synchronized (subscribers) {
                for (DataValueChangedCallbackListener subscriber : subscribers) {
                    DataTreeNode node = getChild(subscriber);
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

        @Override
      public String toString() {
            return "Depends On";
        }

        public void updateChildren(DefaultTreeModel model) {
            List<DataFormula> subscribers = formula.getDependencies();
            synchronized (subscribers) {
                for (DataFormula dependency : subscribers) {
                    DataTreeNode node = getChild(dependency);
                    if (node == null) {
                        node = new LeafNode(this, dependency);
                        addChild(node, model);
                    }
                    
                }
            }
        }

    }

}
