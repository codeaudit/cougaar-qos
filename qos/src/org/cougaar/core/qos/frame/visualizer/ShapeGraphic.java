package org.cougaar.core.qos.frame.visualizer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.visualizer.util.SlotChangeListener;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class ShapeGraphic implements Cloneable {
    protected String id, label;
    protected LabelRenderer labelRenderer;
    protected ShapeRenderer renderer;
    protected RectangularShape shape, shapePrototype;
    protected double x,y,width,height;
    protected double originalWidth, originalHeight; // remember original size (some layout mgrs do resizeing)
    protected boolean visible, selected, mouseOver, isPrototype;
    protected ShapeContainer parent;

    // data
    protected org.cougaar.core.qos.frame.Frame frame;
    protected String frameidSlotName = "";
    protected FramePredicate predicate;
    protected FrameModel frameModel;

    // slot value triggers
    protected ArrayList slotListeners;
    // logging
    protected Logger log = Logging.getLogger(getClass().getName());
    // for the container tree display
    //protected ShapeGraphicNode treeNode;


    public ShapeGraphic() {
        this(null, null);
    }

    protected ShapeGraphic(String id, String label) {
        this.id = id;
        this.label = label;
        x=y=0d;
        width=height=10d;
        visible = true;
        selected = mouseOver =false;
        slotListeners = new ArrayList();
        //treeNode = null;
    }

   /*
    public ShapeGraphicNode getTreeNode() {
        if (treeNode == null)
            treeNode = new ShapeGraphicNode(this, (parent != null ? parent.getTreeNode() : null));
        return treeNode;
    } */

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    public void addSlotListener(SlotChangeListener l) {
        l.setShapeGraphic(this);
        slotListeners.add(l);
    }

    public Collection getSlotListeners() {
        return slotListeners;
    }
    
    public void validateListeners() {
        SlotChangeListener sl;
        for (Iterator ii=slotListeners.iterator(); ii.hasNext();) {
            sl = (SlotChangeListener) ii.next();
            sl.validate();
        }
    }

    public void processFrameChange(org.cougaar.core.qos.frame.Frame f, org.cougaar.core.qos.frame.Frame.Change change) {
        if (f == frame && slotListeners.size()>0) {
            for (Iterator ii=slotListeners.iterator(); ii.hasNext();)
                ((SlotChangeListener)ii.next()).slotChanged(f, change);
        }
    }

    public void setLabelRenderer(LabelRenderer lbl) {
        this.labelRenderer = lbl;
    }

    public LabelRenderer getLabelRenderer() {
        return labelRenderer;
    }
    public void setRenderer(ShapeRenderer shapeRenderer) {
        this.renderer = shapeRenderer;
    }

    public String toString() {
        return "["+(isContainer()?"Container":"Component")+" id="+id+" name="+label+" proto="+isPrototype+" "+predicate+"]";
    }

    public boolean isContainer() {
        return false;
    }

    public boolean isPrototype() {
        return isPrototype;
    }

    public void setPrototype(boolean isPrototype) {
        this.isPrototype = isPrototype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String lbl) {
        label = lbl;
    }

    public void setFrameIdSlotName(String slotName) {
        frameidSlotName = slotName;
    }
    public String getFrameIdSlotName() {
        return frameidSlotName;
    }

    public void setFrame(org.cougaar.core.qos.frame.Frame frame) {
        this.frame = frame;
        if (frame != null) {
            this.id = (String) frame.getValue("name"); //frameidSlotName);
            this.label = id;
            if (log.isDebugEnabled())
                log.debug(label+".setFrame():  id="+this.id+":  "+this.toString());
            frameModel.registerGraphic(frame, this);
        } else {
            this.id = "";
            this.label = "";
        }
    }

    public void unregister() {
        if (frame != null && frameModel != null) {
            frameModel.unregisterGraphic(frame, this);
        }
    }

    public void update(FrameModel frameModel,
                        HashSet addedDataFrames, HashSet removedDataFrames,
                        HashSet addedRelations) {
        this.frameModel = frameModel;
        if (frame == null && predicate != null) {
            if (log.isDebugEnabled())
                log.debug("ShapeGraphic.update");
            setFrame(frameModel.findFrame(predicate)); //(addedDataFrames!=null ? frameModel.findFrame(addedDataFrames, predicate) : frameModel.findFrame(predicate)));
        }
    }
   /*
    public void addedFrames(Collection newFrames) {
        if (frame == null && predicate != null) {
            setFrame(frameModel.findFrame(newFrames, predicate));
        }
    }*/

    public boolean hasFrame() {
        return frame != null;
    }

    public org.cougaar.core.qos.frame.Frame getFrame() {
        if (frame != null)
            return frame;
        return getParentFrame();
    }

    public void setFramePredicate(FramePredicate p) {
        predicate = p;
    }

    public FramePredicate getFramePredicate() {
        return predicate;
    }

    public void setParent(ShapeContainer container) {
        this.parent = container;
        /*
        treeNode = getTreeNode();
        if (treeNode != null) {
            ShapeGraphicNode oldParentNode = (ShapeGraphicNode) treeNode.getParent();
            ShapeGraphicNode newParentNode =  parent.getTreeNode();
            if (oldParentNode != null && oldParentNode != newParentNode) {
                newParentNode.add(treeNode);
                //if (frameModel != null)
                  //  frameModel.containerTreeNodeChanged(newParentNode, treeNode);
            }
        } */
    }

    public ShapeContainer getParent() {
        return parent;
    }

    public ShapeContainer getParentWithFrame() {
        ShapeContainer p = parent;
        while (p!=null) {
            if (p.frame != null)
                return p;
            p = p.getParent();
        }
        return null;
    }

    public org.cougaar.core.qos.frame.Frame getParentFrame() {
        ShapeContainer p = parent;
        org.cougaar.core.qos.frame.Frame f;
        while (p!=null) {
            f = (p != null ? p.getFrame() : null);
            if (f!=null)
                return f;
            p = p.getParent();
        }
        return null;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isVisible() {
        return visible;
    }

    public Shape getShape() {
        if (shape == null)
            shape = createShape();
        return shape;
    }

    public void setShapePrototype(RectangularShape shape) {
        this.shapePrototype = shape;
        if (shape != null) {
            Rectangle2D r = shape.getFrame();
            originalWidth = (r.getWidth() > 0 ? r.getWidth() : originalWidth);
            originalHeight = (r.getHeight() > 0 ? r.getHeight() : originalHeight);
        }
    }

    public RectangularShape createShape() {
        if (shapePrototype != null) {
            RectangularShape tmp = shape;
            shape = (RectangularShape) shapePrototype.clone();
            if (tmp != null)
                shape.setFrame(tmp.getFrame()); // this is a Java2D frame
        }
        return shape;
    }

    public boolean contains(double mx, double my) {
        Shape sh = getShape();
        return (sh != null ? sh.contains(mx,my) : false);
    }

    public ShapeGraphic find(double mx, double my) {
        return (contains(mx,my) ? this : null);
    }

    public ShapeGraphic find(org.cougaar.core.qos.frame.Frame f) {
        if (frame != null && frame == f) //id != null && id.equals((String) f.getValue("name")))
            return this;
        return null;
    }

    public void reshape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height= height;
        if (shape != null)
            shape.setFrame(x,y,width,height);
    }
    public void resetSize() {
        this.width = originalWidth;
        this.height = originalHeight;
        if (shape != null)
            shape.setFrame(x,y,width,height);
    }

    public Point2D.Double getPosition() {
        return new Point2D.Double(x,y);
    }

    public void draw(Graphics2D g2) {
        if (renderer != null && visible)
            renderer.drawShape(g2, this);
    }
    public void drawLabel(Graphics2D g2) {
        if (labelRenderer != null && visible)
            labelRenderer.drawLabel(g2, this);
    }

    public Dimension getSize() {
        Rectangle r = getBounds();
        return new Dimension(r.width,  r.height);
    }

    public Rectangle getBounds() {
        return getShape().getBounds();
    }

    public Rectangle2D getBounds2D() {
        return getShape().getBounds2D();
    }

    // clone thyself and assign the given frame
    public ShapeGraphic createInstance(org.cougaar.core.qos.frame.Frame frame, FrameModel fmodel) {
        try {
            ShapeGraphic cloned = (ShapeGraphic) this.clone();
            cloned.frameModel = fmodel;
            if (frame != null)
                cloned.setFrame(frame);
            cloned.setPrototype(false);
            cloned.shapePrototype = (shapePrototype != null ? ((RectangularShape) shapePrototype.clone()) : null);
            cloned.shape = cloned.createShape();
            org.cougaar.core.qos.frame.Frame parent = getFrame();
            if (cloned.predicate != null && parent != null)
                cloned.predicate = new FramePredicate(cloned.predicate, (String) parent.getValue("name"));

            if (slotListeners.size() > 0) {
                cloned.slotListeners = new ArrayList();
                for (Iterator ii=slotListeners.iterator(); ii.hasNext();) {
                    cloned.addSlotListener(((SlotChangeListener)ii.next()).cloneInstance());
                }
            }
            cloned.validateListeners();
            return cloned;
        } catch (CloneNotSupportedException ee) {
            ee.printStackTrace();
        }
        return null;
    }
}
