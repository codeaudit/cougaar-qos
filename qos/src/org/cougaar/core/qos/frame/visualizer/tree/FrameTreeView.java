package org.cougaar.core.qos.frame.visualizer.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.PrototypeFrame;
import org.cougaar.core.qos.frame.RelationFrame;
import org.cougaar.core.qos.frame.visualizer.FrameModel;
import org.cougaar.core.qos.frame.visualizer.icons.IconFactory;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 8:57:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class FrameTreeView extends TreeView implements ChangeListener {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   HashMap frameMap;
    HashMap relationshipMap;
    HashSet relationMappedFrameNodes;
    ArrayList treePaths;

    FrameModel frameModel;
    FrameInheritenceView frameInheritenceView;
    JLabel selectedLabel, selectedFrameLabel;
    DefaultTreeModel treeModel;
    boolean rootNameNotSet = true, initialBuildDone = false;
    private transient Logger log = Logging.getLogger(getClass().getName());


    public FrameTreeView(FrameModel frameModel) {
        super();
        init();
        frameMap = new HashMap();
        relationshipMap = new HashMap();
        relationMappedFrameNodes = new HashSet();
        treePaths = new ArrayList();
        this.frameModel = frameModel;
        root = new FrameNode();//DefaultMutableTreeNode("frameset");
        root.setUserObject("frameset");
        treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);


        Icon frameIcon = IconFactory.getIcon(IconFactory.FRAME_ICON);
        Icon relationIcon = IconFactory.getIcon(IconFactory.RELATION_ICON);
        tree.setCellRenderer(new FrameRenderer(frameIcon, relationIcon));

        frameModel.addAddedFramesListener(this);
        //frameModel.addChangedFramesListener(this);
        //frameModel.addRemovedFramesListener(this);
    }

    public TreeNode getRootNode() {
        return root;
    }

    @Override
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
        labelBox.add(new JButton(new RefreshFrameTreeAction()));
        labelBox.add(legend);
        rightPanel.add(labelBox, BorderLayout.NORTH);
        return rightPanel;
    }

    class RefreshFrameTreeAction extends AbstractAction {
        /**
       * 
       */
      private static final long serialVersionUID = 1L;
      public RefreshFrameTreeAction() {
            super("Refresh Tree");
        }
        public void actionPerformed(ActionEvent e) {
            rebuildTree();
        }
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

    @Override
   protected void treeSelected(Object data) {
        if (data instanceof org.cougaar.core.qos.frame.Frame) {
            displayFrameInTable((org.cougaar.core.qos.frame.Frame)data);
        }
    }

    @Override
   protected void displayFrameInTable(org.cougaar.core.qos.frame.Frame frame) {
        //frameInheritenceView.clear();
        frameInheritenceView.setFrame(frame);
        selectedFrameLabel.setText( (frame instanceof PrototypeFrame ? ((PrototypeFrame)frame).getName() : FrameModel.getName(frame)));
    }


     public void stateChanged(ChangeEvent e) {
         if (!initialBuildDone)  {
            MyFrameEventHelper helper = new MyFrameEventHelper(e);
            if (SwingUtilities.isEventDispatchThread())
                helper.run();
            else SwingUtilities.invokeLater(helper);
            initialBuildDone = true;
         }
    }



    class MyFrameEventHelper implements Runnable {
        ChangeEvent e;
        public MyFrameEventHelper(ChangeEvent che) { e = che;}
        public void run() {
            rebuildTree();
            /*
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
                ((FrameNode)root).setLabel("frameset '"+frameModel.getFrameSetName()+"'");
                //treeModel.nodeChanged(root);
                rootNameNotSet = false;
                TreePath p = new TreePath(root);
                if (!tree.isExpanded(p))
                    tree.expandPath(p);
            }
            //TreeWriter.write(root, 5, 5);
            */
        }
    }






    public void rebuildTree() {
        root = new FrameNode();
        ((FrameNode)root).setLabel("frameset '"+frameModel.getFrameSetName()+"'");
        frameMap = new HashMap(11);
        relationshipMap = new HashMap(11);
        relationMappedFrameNodes = new HashSet(0);
        treePaths = new ArrayList();

        Collection dataFrames         = frameModel.getDataFrames();
        Collection relationshipFrames = frameModel.getRelationshipFrames();
        createNodes(dataFrames);

        processRelationships(relationshipFrames);
        //Collection rootNodes = findRootLevelNodes();
        //for (Iterator ii=rootNodes.iterator(); ii.hasNext();) {
        //    root.add((DefaultMutableTreeNode) ii.next());
        //}
        tree.setModel(new DefaultTreeModel(root));
        // expand all paths
        expandTree((FrameNode)root);
    }

    void expandTree(FrameNode root) {
        walkTree(root);
        TreePath tp;
        for (Iterator ii=treePaths.iterator(); ii.hasNext();) {
           tp = (TreePath) ii.next();
           tree.expandPath(tp);
           //System.out.println("expanding path "+tp);
        }
    }

    void walkTree(FrameNode node) {
        if (node.isLeaf()) {
            treePaths.add(new TreePath( ((FrameNode)node.getParent()).getPath()));
            return;
        }
        FrameNode tn;
        for (Enumeration ee=node.children(); ee.hasMoreElements();) {
            tn = (FrameNode) ee.nextElement();
            walkTree(tn);
        }
    }


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
        org.cougaar.core.qos.frame.DataFrame pf, cf;
        FrameNode parentNode, childNode;
        String relationship;

        for (Iterator ii=relationshipFrames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.RelationFrame) ii.next();

            pf = f.relationshipParent();
            cf = f.relationshipChild();
            relationship = f.getKind();
            if (pf == null || cf  == null) { // we got ourselves an invalid relation frame (if parent || child has not arrived yet)
                if (log.isDebugEnabled())
                    log.debug("processRelationships:  invalid relation frame, ignoring"+f.getParentValue()+"=>"+relationship+"==>"+f.getChildValue());
                return;
            }

            parentNode = createNode(pf);
            childNode  = createNodeFromRelation(f, cf);
            FrameNode relationNode = parentNode.getRelationshipNode(relationship);

            if (relationNode == null) {
                if (log.isDebugEnabled())
                    log.debug("creating RelationFrameNode:  "+f.getParentValue()+"=>"+relationship+"==>"+f.getChildValue());
                relationNode = new FrameNode(relationship);
                parentNode.addRelationshipNode(treeModel, relationNode);
            } 
            if (!relationNode.hasChild(childNode)) {
                treePaths.add(new TreePath(relationNode.getPath()));
                insertNode(childNode, relationNode);
            }
        }

        Collection rootNodes = findRootLevelNodes();
        for (Iterator ii=rootNodes.iterator(); ii.hasNext();)
            insertNode((FrameNode)ii.next(), (FrameNode)root);
            //treeModel.insertNodeInto((DefaultMutableTreeNode) ii.next(), root, 0);
        //treeModel.nodeStructureChanged(root);
    }

    protected void insertNode(FrameNode childNode, FrameNode parentNode) {
         if (!parentNode.hasChild(childNode))
            treeModel.insertNodeInto(childNode, parentNode, 0);
    }


    protected FrameNode createNodeFromRelation(RelationFrame rf, DataFrame childFrame) {
        FrameNode childNode = (FrameNode) relationshipMap.get(rf);
        if (childNode == null) {
            // check if this node exists
            childNode = (FrameNode) frameMap.get(childFrame);
            if (childNode != null) { // it exists, now check if there is any relation frame mapped to it
                if (relationMappedFrameNodes.contains(childNode)) {
                    // some relation node is already mapped to this childNode, create a proxy
                    FrameNode tmp = childNode;
                    childNode = new FrameNodeProxy(childNode);
                    tmp.addProxy((FrameNodeProxy)childNode);
                }
                relationMappedFrameNodes.add(childNode);
                relationshipMap.put(rf, childNode);

            }  else { // childNode is has not been created yet
                childNode = createNode(childFrame);
                relationMappedFrameNodes.add(childNode);
                relationshipMap.put(rf, childNode);
            }
        }
        return childNode;
    }

    protected FrameNode createNode(org.cougaar.core.qos.frame.Frame frame) {
        if (frame == null)
            return null;
        FrameNode newNode = (FrameNode) frameMap.get(frame);
        if (newNode == null) {
            if (log.isDebugEnabled())
                    log.debug("creating FrameNode for frame '"+FrameModel.getName(frame)+"'   frame="+frame);
            newNode = new FrameNode(frame);
            frameMap.put(frame, newNode);
        } else {
             if (log.isDebugEnabled())
                    log.debug("retrieving existing FrameNode for frame '"+FrameModel.getName(frame)+"'   frame="+frame);
        }
        return newNode;
    }

    protected void createNodes(Collection dataFrames) {
        org.cougaar.core.qos.frame.Frame  f;
        for (Iterator ii=dataFrames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.DataFrame) ii.next();
            //if (f instanceof DataFrame)
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
        /**
       * 
       */
      private static final long serialVersionUID = 1L;
      Icon relationIcon;
        Icon frameIcon;

        public FrameRenderer(Icon frameIcon, Icon relationIcon) {
            super();
            this.relationIcon = relationIcon;
            this.frameIcon = frameIcon;
        }

        @Override
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
