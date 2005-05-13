package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.PrototypeFrame;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

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

        public FrameNode() {
            this((String)null);
        }

        public FrameNode(String relationship) {
            super(relationship);
            this.relationship = relationship;
	        this.relationshipNodes = new HashMap();
            this.proxies = new ArrayList();
            this.frame = null;
            this.label = relationship;
        }

        public FrameNode(org.cougaar.core.qos.frame.Frame frame) {
            super(frame);
            this.frame = frame;
            this.relationship = null;
	        this.relationshipNodes = new HashMap();
            this.proxies = new ArrayList();
            this.label = (frame instanceof PrototypeFrame ? ((PrototypeFrame)frame).getName() : (String) frame.getValue("name"));
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

        public String toString() {
             return label;
        }

        public void addRelationshipNode(FrameNode rnode) {
            String label =rnode.toString();
            if (relationshipNodes.get(label)==null) {
                relationshipNodes.put(label, rnode);
                add(rnode);
            }
        }

        public FrameNode getRelationshipNode(String relationship) {
            return (FrameNode) relationshipNodes.get(relationship);
        }

        public org.cougaar.core.qos.frame.Frame getFrame() {
            return frame;
        }
    }
