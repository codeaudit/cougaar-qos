package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.FrameHelper;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.Frame;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 8:57:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class FrameTreeView extends ExplorerView {
    HashMap frameMap;
    FrameTableModel frameModel;
    ShapeGraphicTableModel shapeModel;
    FrameHelper frameHelper;


    public FrameTreeView() {
        super();
        frameMap = new HashMap();
        frameModel = new FrameTableModel();
        shapeModel = new ShapeGraphicTableModel();
    }

    protected void displayShapeGraphicInTable(ShapeGraphic g) {
        shapeModel.clear();
        shapeModel.set(g);
        editTable.setModel(shapeModel);
    }

    protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
        frameModel.clear();
        frameModel.set(frame);
        editTable.setModel(frameModel);
    }


    public void buildFrameTree(FrameHelper frameHelper) { //Collection frames, String framesetName) { 
	this.frameHelper = frameHelper;
        root = new DefaultMutableTreeNode("frameset '"+frameHelper.getFrameSetName()+"'");
        frameMap.clear();
        Collection relationshipFrames = process(frameHelper.getAllFrames());
        processRelationships(relationshipFrames);
        Collection rootNodes = findRootLevelNodes();
        for (Iterator ii=rootNodes.iterator(); ii.hasNext();) {
            root.add((DefaultMutableTreeNode) ii.next());
        }
	tree.setModel(new DefaultTreeModel(root));
    }

    protected Collection process(Collection frames) {
        ArrayList relationships = new ArrayList();
        org.cougaar.core.qos.frame.Frame f;
        String name;

        for (Iterator ii=frames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.Frame) ii.next();
            if (f.isa("relationship")) {
                relationships.add(f);
                continue;
            }
            name = (String) f.getValue("name");
            if (frameMap.get(name) != null)
                continue; //throw new IllegalArgumentException("frame '"+name+"' already exists in the frame map"); // temp
            else
                frameMap.put(name, new FrameNode(f));
        }
        return relationships;
    }

    protected void processRelationships(Collection relationshipFrames) {
        org.cougaar.core.qos.frame.Frame f;
        FrameNode parent, child;
        String relationship, parentName, childName;
        for (Iterator ii=relationshipFrames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.Frame) ii.next();
            parentName = (String) f.getValue("parent-value");
            childName  = (String) f.getValue("child-value");
            relationship = (String) f.getKind();
            parent = (FrameNode) frameMap.get(parentName);
            child  = (FrameNode) frameMap.get(childName);
            if (parent != null && child != null) {
                FrameNode relationNode = parent.getRelationshipNode(relationship);
                if (relationNode == null) {
                    relationNode = new FrameNode(relationship);
                    parent.addRelationshipNode(relationNode);
                }
                relationNode.add(child);
            } else {
                ;// print something here
            }
       }
    }

    protected Collection findRootLevelNodes() {
        ArrayList rootLevelNodes = new ArrayList();
        FrameNode node;
        for (Iterator ii=frameMap.values().iterator(); ii.hasNext();) {
            node = (FrameNode) ii.next();
            if (node.getParent() == null)
                rootLevelNodes.add(node);
        }
        return rootLevelNodes;
    }

    protected Collection findFrames(Collection frames, FramePredicate predicate) {
       org.cougaar.core.qos.frame.Frame f;
       ArrayList flist = new ArrayList();
       for (Iterator ii=frames.iterator(); ii.hasNext();) {
           f = (org.cougaar.core.qos.frame.Frame) ii.next();
           if (predicate.execute(f))
               flist.add(f);
       }
       return flist;
   }

}
