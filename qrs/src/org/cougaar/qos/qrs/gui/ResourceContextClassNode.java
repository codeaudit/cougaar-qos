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
