package org.cougaar.core.qos.frame.visualizer.tree;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 28, 2005
 * Time: 5:48:11 PM
 * To change this template use File | Settings | File Templates.
 */
// this class a big hack to be able to have multiple instances of the *same* node in the tree
public class FrameNodeProxy extends FrameNode {
        /**
    * 
    */
   private static final long serialVersionUID = 1L;
      FrameNode node;

        public FrameNodeProxy(FrameNode node) {
            super( node.getFrame());
            this.node = node;
        }

        @Override
      public boolean isRelationNode() {
            return node.isRelationNode();
        }

        @Override
      public String getRelationshipName() {
            return node.getRelationshipName();
        }

        @Override
      public String toString() {
             return node.toString();
        }

        @Override
      public void addRelationshipNode(DefaultTreeModel treeModel,FrameNode rnode) {
            node.addRelationshipNode(treeModel, rnode);
        }

        @Override
      public FrameNode getRelationshipNode(String relationship) {
            return node.getRelationshipNode(relationship);
        }

        @Override
      public org.cougaar.core.qos.frame.Frame getFrame() {
            return node.getFrame();
        }

        @Override
      public void insert(MutableTreeNode newChild, int childIndex) {
            node.insert(newChild, childIndex);
        }

        @Override
      public void remove(int childIndex) {
            node.remove(childIndex);
        }

        @Override
      public void setParent(MutableTreeNode newParent) {
           parent = newParent;
        }

         @Override
         public TreeNode getParent() {
            return parent;
         }

        @Override
      public TreeNode getChildAt(int index) {
            return node.getChildAt(index);
        }

       @Override
      public int getChildCount() {
            return node.getChildCount();
       }

       @Override
      public int getIndex(TreeNode aChild) {
            return node.getIndex(aChild);
       }

       @Override
      public Enumeration children() {
            return node.children();
       }

       @Override
      public void setAllowsChildren(boolean allows) {
           node.setAllowsChildren(allows);
       }

       @Override
      public boolean getAllowsChildren() {
            return node.getAllowsChildren();
       }
        /*
       public void setUserObject(Object userObject) {
            this.userObject = userObject;
       }

       public Object getUserObject() {
            return userObject;
       }

       public void removeFromParent() {
           MutableTreeNode parent = (MutableTreeNode)getParent();
           if (parent != null) {
               parent.remove(this);
           }
       }  */


       @Override
      public void remove(MutableTreeNode aChild) {
           node.remove(aChild);
       }

       @Override
      public void removeAllChildren() {
            node.removeAllChildren();
       }

       @Override
      public void add(MutableTreeNode newChild) {
           node.add(newChild);
       }


   }

