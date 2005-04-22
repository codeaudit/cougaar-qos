package org.cougaar.core.qos.frame.visualizer.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;

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

        public FrameNode(String relationship) {
            super(relationship);
            this.relationship = relationship;
	    this.relationshipNodes = new HashMap();
            this.frame = null;
            this.label = relationship;
        }
        public FrameNode(org.cougaar.core.qos.frame.Frame frame) {
            super(frame);
            this.frame = frame;
	    this.relationshipNodes = new HashMap();
            this.label = (String) frame.getValue("name");
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
    }
