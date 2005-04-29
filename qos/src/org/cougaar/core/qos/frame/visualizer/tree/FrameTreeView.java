package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.FrameHelper;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.icons.IconFactory;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.Frame;
import org.cougaar.core.qos.frame.RelationFrame;
import org.cougaar.core.qos.frame.PrototypeFrame;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.net.URL;
import java.io.IOException;
import java.awt.*;
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
public class FrameTreeView extends TreeView {
    HashMap frameMap;
    FrameHelper frameHelper;
    FrameInheritenceView frameInheritenceView;
    JLabel selectedLabel, selectedFrameLabel;

    private transient Logger log = Logging.getLogger(getClass().getName());


    public FrameTreeView() {
        super();
        init();
        frameMap = new HashMap();
        //frameModel = new FrameTableModel();
        Icon frameIcon = IconFactory.getIcon(IconFactory.FRAME_ICON);
        Icon relationIcon = IconFactory.getIcon(IconFactory.RELATION_ICON);
        tree.setCellRenderer(new FrameRenderer(frameIcon, relationIcon));
    }

    public Component createOtherComponent() {
        frameInheritenceView =  new FrameInheritenceView();

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(frameInheritenceView, BorderLayout.CENTER);

        selectedLabel = new JLabel("selected:");
        selectedFrameLabel = new JLabel("");
        JLabel slotDef = newLegendLabel("Slot Definition", ColorDefs.slotDefinitionColor, true);
        JLabel inheritedSlot = newLegendLabel("Inherited Slot", ColorDefs.inheritedSlotColor, true);
        JLabel localSlot = newLegendLabel("Local Slot", ColorDefs.localSlotColor, true);
        Box legend = Box.createHorizontalBox();
        legend.add(slotDef);
        legend.add(inheritedSlot);
        legend.add(localSlot);
        legend.setBorder(BorderFactory.createEtchedBorder());

        Box labelBox = Box.createHorizontalBox();
        labelBox.add(selectedLabel);
        labelBox.add(Box.createHorizontalStrut(10));
        labelBox.add(selectedFrameLabel);
        labelBox.add(Box.createHorizontalGlue());
        labelBox.add(legend);
        rightPanel.add(labelBox, BorderLayout.NORTH);
        return rightPanel;
    }

    JLabel newLegendLabel(String label, Color color, boolean hasBorder) {
        JLabel lbl = new JLabel(label);
        lbl.setOpaque(true);
        lbl.setBackground(color);
        if (hasBorder)
            lbl.setBorder(BorderFactory.createEtchedBorder());
        return lbl;
    }

    protected void treeSelected(Object data) {
        if (data instanceof org.cougaar.core.qos.frame.Frame) {
            displayFrameInTable((org.cougaar.core.qos.frame.Frame)data);
        }
    }

    protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
        //frameInheritenceView.clear();
        frameInheritenceView.setFrame(frame);
        selectedFrameLabel.setText( (frame instanceof PrototypeFrame ? ((PrototypeFrame)frame).getName() : (String)frame.getValue("name")));
    }


    public void buildFrameTree(FrameHelper frameHelper) {
	this.frameHelper = frameHelper;
        root = new DefaultMutableTreeNode("frameset '"+frameHelper.getFrameSetName()+"'");
        frameMap.clear();
	
	if (log.isDebugEnabled())
	    log.debug("buildFrameTree frameHelper="+frameHelper); 

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
            if (f instanceof RelationFrame) {
                relationships.add(f);
                continue;
            }
            name = (String) f.getValue("name");
            if (frameMap.get(name) != null)
                continue;
            else {
		if (log.isDebugEnabled())
		    log.debug("creating FrameNode for frame '"+name+"'");
                frameMap.put(name, new FrameNode(f));
	    }
        }
	if (log.isDebugEnabled())
	    log.debug("FrameTreeView: found "+ relationships.size() + " relation frames and "+frameMap.values().size()+" framenodes");
        return relationships;
    }


    protected void processRelationships(Collection relationshipFrames) {
        org.cougaar.core.qos.frame.RelationFrame f;
        FrameNode parent, child;
        String relationship, parentName, childName;
        for (Iterator ii=relationshipFrames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.RelationFrame) ii.next();
            parentName = (String) f.getValue("parent-value");
            childName  = (String) f.getValue("child-value");
            relationship = (String) f.getKind();
            parent = (FrameNode) frameMap.get(parentName);
            child  = (FrameNode) frameMap.get(childName);
            if (child.getParent() != null) {
                FrameNode tmp = child;
                child = new FrameNodeProxy(child);
                tmp.addProxy((FrameNodeProxy)child);
		if (log.isDebugEnabled())
		    log.debug("creating *FrameNodeProxy for frame '"+childName+"'  relation="+relationship);
            }
            if (parent != null && child != null) {
                FrameNode relationNode = parent.getRelationshipNode(relationship);
                if (relationNode == null) {
		    if (log.isDebugEnabled())
			log.debug("creating RelationFrameNode:  "+parentName+"=>"+relationship+"==>"+childName);
                    relationNode = new FrameNode(relationship);
                    parent.addRelationshipNode(relationNode);
                }
                relationNode.add(child);
            } else {
		if (log.isDebugEnabled())
		    log.debug("can't create node: parent='"+parentName+"'("+(parent!=null?"found":"not found")+") childName='"+childName+"'("+(child!=null?"found":"not found")+") relationship='"+relationship+"'");
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


    private class FrameRenderer extends DefaultTreeCellRenderer {
        Icon relationIcon;
        Icon frameIcon;

        public FrameRenderer(Icon frameIcon, Icon relationIcon) {
            super();
            this.relationIcon = relationIcon;
            this.frameIcon = frameIcon;
        }

        public Component getTreeCellRendererComponent(
                                                    JTree tree,
                                                    Object value,
                                                    boolean sel,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel,
                                               expanded, leaf, row,
                                               hasFocus);

            //System.out.println(value.getClass().getName());
            if (value instanceof FrameNode) {
                FrameNode node = (FrameNode) value;
                setIcon(node.isRelationNode() ? relationIcon : frameIcon);
            } else if (value instanceof ShapeGraphicNode) {
                ;//ShapeGraphicNode node = (ShapeGraphicNode) value;

            } else setIcon(null);

            return this;
        }
    }


}
