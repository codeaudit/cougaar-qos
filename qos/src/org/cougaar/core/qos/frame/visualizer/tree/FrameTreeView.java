package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.FrameModel;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.event.*;
import org.cougaar.core.qos.frame.visualizer.icons.IconFactory;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.Frame;
import org.cougaar.core.qos.frame.RelationFrame;
import org.cougaar.core.qos.frame.PrototypeFrame;
import org.cougaar.core.qos.frame.DataFrame;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.net.URL;
import java.io.IOException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 8:57:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class FrameTreeView extends TreeView implements ChangeListener {
    HashMap frameMap;
    FrameModel frameModel;
    FrameInheritenceView frameInheritenceView;
    JLabel selectedLabel, selectedFrameLabel;
    DefaultTreeModel treeModel;
    boolean rootNameNotSet = true;
    private transient Logger log = Logging.getLogger(getClass().getName());


    public FrameTreeView(FrameModel frameModel) {
        super();
        init();
        frameMap = new HashMap();
        this.frameModel = frameModel;
        root = new DefaultMutableTreeNode("frameset");
        treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);


        Icon frameIcon = IconFactory.getIcon(IconFactory.FRAME_ICON);
        Icon relationIcon = IconFactory.getIcon(IconFactory.RELATION_ICON);
        tree.setCellRenderer(new FrameRenderer(frameIcon, relationIcon));

        frameModel.addAddedFramesListener(this);
        frameModel.addChangedFramesListener(this);
        frameModel.addRemovedFramesListener(this);
    }

    public Component createOtherComponent() {
        frameInheritenceView =  new FrameInheritenceView();

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(frameInheritenceView, BorderLayout.CENTER);

        selectedLabel = new JLabel("selected:");
        selectedFrameLabel = new JLabel("");      
        Box legend = createHorizontalFrameLegend();

        Box labelBox = Box.createHorizontalBox();
        labelBox.add(selectedLabel);
        labelBox.add(Box.createHorizontalStrut(10));
        labelBox.add(selectedFrameLabel);
        labelBox.add(Box.createHorizontalGlue());
        labelBox.add(legend);
        rightPanel.add(labelBox, BorderLayout.NORTH);
        return rightPanel;
    }

    public static Box createHorizontalFrameLegend() {
        JLabel slotDef = newLegendLabel("Slot Definition", ColorDefs.slotDefinitionColor, true);
        JLabel inheritedSlot = newLegendLabel("Container Slot", ColorDefs.inheritedSlotColor, true);
        JLabel localSlot = newLegendLabel("Prototype Slot", ColorDefs.localSlotColor, true);
        Box legend = Box.createHorizontalBox();
        legend.add(slotDef);
        legend.add(inheritedSlot);
        legend.add(localSlot);
        legend.setBorder(BorderFactory.createEtchedBorder());
        return legend;
    }
    public static JLabel newLegendLabel(String label, Color color, boolean hasBorder) {
        JLabel lbl = new JLabel(label);
        lbl.setOpaque(true);
        lbl.setBackground(color);
        if (hasBorder) {
            CompoundBorder cb = new CompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(0,5,0,5));
            lbl.setBorder(cb);//BorderFactory.createEtchedBorder());
        }
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
        selectedFrameLabel.setText( (frame instanceof PrototypeFrame ? ((PrototypeFrame)frame).getName() : FrameModel.getName(frame)));
    }


     public void stateChanged(ChangeEvent e) {
        MyFrameEventHelper helper = new MyFrameEventHelper(e);
        if (SwingUtilities.isEventDispatchThread())
            helper.run();
        else SwingUtilities.invokeLater(helper);
    }



    class MyFrameEventHelper implements Runnable {
        ChangeEvent e;
        public MyFrameEventHelper(ChangeEvent che) { e = che;}
        public void run() {
            if (e instanceof AddedFramesEvent) {
                // process added frames
                AddedFramesEvent ee = (AddedFramesEvent)e;
                HashSet addedDataFrames = ee.getAddedDataFrames();
                if (addedDataFrames != null)
                    createNodes(addedDataFrames);

                HashSet addedRelationFrames = ee.getAddedRelationFrames();
                if (addedRelationFrames != null)
                    processRelationships(addedRelationFrames);

            }  else if (e instanceof ChangedFramesEvent) {
                ChangedFramesEvent ee = (ChangedFramesEvent) e;
                //HashMap dataFrames = ee.getChangedDataFrames();

                HashMap relationFrames = ee.getChangedRelationFrames();
                if (relationFrames != null)
                    processRelationships(relationFrames.keySet());

            }  else if (e instanceof RemovedFramesEvent) {
                 ;
            }
            if (rootNameNotSet) {
                root.setUserObject("frameset '"+frameModel.getFrameSetName()+"'");
                rootNameNotSet = false;
                TreePath p = new TreePath(root);
                if (!tree.isExpanded(p))
                    tree.expandPath(p);
            }   
        }
    }


 /*

    public void updateTree() {
        Collection relationshipFrames = process(frameModel.getAllFrames());
        processRelationships(relationshipFrames);
        Collection rootNodes = findRootLevelNodes();

        for (Iterator ii=rootNodes.iterator(); ii.hasNext();) {
            root.add((DefaultMutableTreeNode) ii.next());
        }
        tree.setModel(new DefaultTreeModel(root));
    }
    */
/*
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
            if (frameMap.get(f) != null)
                continue;
            else {
                if (log.isDebugEnabled())
                    log.debug("creating FrameNode for frame '"+f.getValue("name")+"'");
                frameMap.put(f, new FrameNode(f));
            }
        }
        if (log.isDebugEnabled())
            log.debug("FrameTreeView: found "+ relationships.size() + " relation frames and "+frameMap.values().size()+" framenodes");
        return relationships;
    }
*/

    protected void processRelationships(Collection relationshipFrames) {
        org.cougaar.core.qos.frame.RelationFrame f;
        org.cougaar.core.qos.frame.Frame pf, cf;
        FrameNode parent, child;
        String relationship, parentName, childName, parentProto, childProto;
        for (Iterator ii=relationshipFrames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.RelationFrame) ii.next();
            //parentName = (String) f.getParentValue();
            //childName  = (String) f.getChildValue();
            //parentProto = (String) f.getParentPrototype();
            //childProto  = (String) f.getChildPrototype();
            pf = f.relationshipParent();
            cf = f.relationshipChild();
            relationship = (String) f.getKind();
            if (pf == null || cf  == null) { // we got ourselves an invalid relation frame (if parent || child has not arrived yet)
                if (log.isDebugEnabled())
                        log.debug("processRelationships:  invalid relation frame, ignoring"+f.getParentValue()+"=>"+relationship+"==>"+f.getChildValue());
                return;
            }
            parent = createNode(pf);//(FrameNode) frameMap.get(pf);
            child  = createNode(cf);//(FrameNode) frameMap.get(cf);
            if (parent != null && child != null) {
                if (child.getParent() != null) {
                    FrameNode tmp = child;
                    child = new FrameNodeProxy(child);
                    tmp.addProxy((FrameNodeProxy)child);
                    if (log.isDebugEnabled())
                        log.debug("creating *FrameNodeProxy for frame '"+f.getChildValue()+"'  relation="+relationship);
                }
                FrameNode relationNode = parent.getRelationshipNode(relationship);
                if (relationNode == null) {
                    if (log.isDebugEnabled())
                        log.debug("creating RelationFrameNode:  "+f.getParentValue()+"=>"+relationship+"==>"+f.getChildValue());
                    relationNode = new FrameNode(relationship);
                    parent.addRelationshipNode(treeModel, relationNode);
                }
                //relationNode.add(child);
                treeModel.insertNodeInto(child, relationNode, 0);
            } else {
                if (log.isDebugEnabled())
                    log.debug("++error: can't create node,  parent='"+f.getParentValue()+"'("+(parent!=null?"found":"not found")+") childName='"+f.getChildValue()+"'("+(child!=null?"found":"not found")+") relationship='"+relationship+"'");
                ;// print something here
            }
        }

        Collection rootNodes = findRootLevelNodes();
        for (Iterator ii=rootNodes.iterator(); ii.hasNext();)
            treeModel.insertNodeInto((DefaultMutableTreeNode) ii.next(), root, 0);

    }

    protected FrameNode createNode(org.cougaar.core.qos.frame.Frame frame) {
        if (frame == null)
            return null;
        FrameNode newNode = (FrameNode) frameMap.get(frame);
        if (newNode == null) {
            if (log.isDebugEnabled())
                    log.debug("creating FrameNode for frame '"+FrameModel.getName(frame)+"'");
            newNode = new FrameNode(frame);
            frameMap.put(frame, newNode);
        }
        return newNode;
    }

    protected void createNodes(Collection dataFrames) {
        org.cougaar.core.qos.frame.Frame  f;
        for (Iterator ii=dataFrames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.Frame) ii.next();
            if (f instanceof DataFrame)
                  createNode(f);
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
  /*
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
     */

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
