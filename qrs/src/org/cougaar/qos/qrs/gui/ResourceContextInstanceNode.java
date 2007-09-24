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
