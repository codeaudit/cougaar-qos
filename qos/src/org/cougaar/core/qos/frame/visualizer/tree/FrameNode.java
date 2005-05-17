package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.PrototypeFrame;

import javax.swing.tree.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:30:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class FrameNode extends DefaultMutableTreeNode {
    org.cougaar.core.qos.frame.Frame frame;
    HashMap relationshipNodes;
    String relationship;
    String label;
    //a little hack -- this is a list of Proxy nodes that reference this node  (to allow multiple occurances of a
    // frame in the tree
    ArrayList proxies;
    HashSet cachedChildren;

    public FrameNode() {
        this("");
    }

    public FrameNode(String relationship) {
        super(relationship);
        this.relationship = (relationship != null && relationship.length() > 0 ? relationship : null);
        this.relationshipNodes = new HashMap();
        this.cachedChildren = new HashSet();
        this.proxies = new ArrayList();
        this.frame = null;
        this.label = relationship;
    }

    public FrameNode(org.cougaar.core.qos.frame.Frame frame) {
        super(frame);
        this.frame = frame;
        this.relationship = null;
        this.relationshipNodes = new HashMap();
        this.cachedChildren = new HashSet();
        this.proxies = new ArrayList();
        this.label = (frame == null ? "" : (frame instanceof PrototypeFrame ? ((PrototypeFrame)frame).getName() : (String) frame.getValue("name")));
    }

    // hack
    public void addProxy(FrameNodeProxy p) {
        proxies.add(p);
    }
    // hack
    public Collection getProxies() {
        return proxies;
    }

    public boolean isRelationNode() {
        return relationship != null;
    }

    public String getRelationshipName() {
        return relationship;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public String toString() {
        return label;
    }

    public void addRelationshipNode(DefaultTreeModel treeModel, FrameNode rnode) {
        String label =rnode.toString();
        if (relationshipNodes.get(label)==null) {
            relationshipNodes.put(label, rnode);
            //add(rnode);
            treeModel.insertNodeInto(rnode, this, 0);//children.size());
        }
    }

    public FrameNode getRelationshipNode(String relationship) {
        return (FrameNode) relationshipNodes.get(relationship);
    }

    public org.cougaar.core.qos.frame.Frame getFrame() {
        return frame;
    }


    public boolean hasChild(TreeNode child) {
        return (cachedChildren.contains(child));
    }

    public void add(MutableTreeNode newChild) {
        cachedChildren.add(newChild);
        super.add(newChild);
    }

    public void remove(int childIndex) {
        TreeNode child = getChildAt(childIndex);
        cachedChildren.remove(child);
        super.remove(childIndex);
    }
    public void remove(MutableTreeNode aChild) {
        cachedChildren.remove(aChild);
        super.remove(aChild);
    }

    public void removeAllChildren() {
        cachedChildren.clear();
        super.removeAllChildren();
    }
}
