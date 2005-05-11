package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.visualizer.test.ContainsPredicate;
import org.cougaar.core.qos.frame.visualizer.util.SlotChangeListener;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 9:42:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShapeContainer extends ShapeGraphic {
    protected ArrayList children;
    protected HashMap prototypes;//, insertContainers;
    protected HashSet frameChildren;
    protected ShapeLayout shapeLayout;

    //private transient Logger log = Logging.getLogger(getClass().getName());

    public ShapeContainer() {
        this("","");
    }

    public boolean isContainer() {
        return true;
    }

    public ShapeContainer(String id, String label) {
        super(id,label);
        children = new ArrayList();
        frameChildren = new HashSet();
        prototypes = new HashMap();
        //insertContainers = new HashMap();
    }

    public void reshape(double tx, double ty, double w, double h) {
        super.reshape(tx,ty,w,h);
        layoutChildren();
    }

    public void setLayout(ShapeLayout layout) {
        shapeLayout = layout;
        if (shapeLayout != null) {
            //shapeLayout.setContainer(this);
            layoutChildren();
        }
    }

    public void setMargins(double left, double right, double bottom, double top, double hpadding, double vpadding) {
        if (shapeLayout != null) {
            shapeLayout.setMargins(left,right,bottom,top, hpadding, vpadding);
            layoutChildren();
        }
    }

    public ShapeGraphic find(double mx, double my) {
        if (!contains(mx, my))
            return null;
        ShapeGraphic sh, result;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            sh=(ShapeGraphic) ii.next();
            result = sh.find(mx, my);
            if (result != null)
                return result;
        }
        return this;
    }


    public ShapeGraphic find(org.cougaar.core.qos.frame.Frame f) {
        if (frame != null && f == frame)
        //if (frame != null && id != null && id.equals((String) f.getValue("name")))
            return this;
        ShapeGraphic sh, result;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            sh=(ShapeGraphic) ii.next();
            result = sh.find(f);
            if (result != null)
                return result;
        }
        return null;
    }


    protected void layoutChildren() {
        if (shapeLayout != null)
            shapeLayout.doLayout(this);
    }

    public Point2D.Double getNextInsertPosition() {
        return new Point2D.Double(x+(width/2d), y+(height/2d)); //???
    }


    public void draw(Graphics2D g2) {
        super.draw(g2);
        if (children.size() > 0)
            drawChildren(g2);
    }

    protected void drawChildren(Graphics2D g2) {
        ShapeGraphic sh;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            sh=(ShapeGraphic) ii.next();
            sh.draw(g2);
        }
    }

    public boolean hasPrototype(String kind) {
      return  (prototypes.get(kind) != null);
    }

    public Collection getPrototypes() {
        return prototypes.values();
    }

    public boolean hasChild(ShapeGraphic g) {
        ShapeGraphic sh;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            sh=(ShapeGraphic) ii.next();
            if (sh == g)
                return true;
        }
        return false;
    }

    public void add(ShapeGraphic sh) {
        if (log.isDebugEnabled())
            log.debug("====>"+id+".add(ShapeGraphic): "+toString()+"\n\t  adding  "+sh);
        sh.setParent(this);
        if (sh.isPrototype())
            prototypes.put(sh.getFramePredicate().getKind(), sh);
        else {
            if (!hasChild(sh)) {
                children.add(sh);
                if (sh.frame != null && !frameChildren.contains(sh.frame))
                    frameChildren.add(sh.frame);
                layoutChildren();
            }
        }
    }


    public void add(org.cougaar.core.qos.frame.Frame frame) {
        String kind = frame.getKind();
        ShapeGraphic shg;
        FramePredicate fp;

        //if (log.isDebugEnabled())
          //  log.debug("====>maybe-add: "+toString()+"\n\t  adding  frame(kind="+kind+", name="+frame.getValue("name")+")");

        String frameName = (String) frame.getValue("name");
        if (frameChildren.contains(frame)) {
            if (log.isDebugEnabled())
                log.debug("====>rejected: already have a child with name "+frameName);
            return;
        }


        for (Iterator ii=prototypes.values().iterator(); ii.hasNext();) {
            shg = (ShapeGraphic) ii.next();
            fp = shg.getFramePredicate();
            if (fp!=null && frame.isa(fp.getKind())) {
                if (log.isDebugEnabled())
                    log.debug("****>add: "+toString()+"\n\t  adding clone (kind="+kind+", name="+frame.getValue("name")+")");
                add(shg.createInstance(frame));
                //frameChildren.add(frame);
            } else {
                if (log.isDebugEnabled())
                    log.debug("****>add: "+toString()+"\n\t  did not find a prototype match for kind="+kind+", discarding");
            }
        }
    }


    public void remove(ShapeGraphic sh) {
        children.remove(sh);
        sh.setParent(null);
        if (sh.frame != null)
            frameChildren.remove(sh.frame);
        layoutChildren();
    }

    public void remove(String shapeId) {
        ShapeGraphic sh=null;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            sh=(ShapeGraphic) ii.next();
            if (sh.getId().equals(shapeId))
                break;
            sh = null;
        }
        if (sh!=null)
            remove(sh);
    }

    public Collection getChildren() {
        return children;
    }

    public int getNumChildren() {
        return (children != null ? children.size() : 0);
    }


    protected void buildChildren(FrameHelper helper, Display display) {
        // this can be either a container with no associated frame (used for grouping)
        // of a container with a frame and a frame predicate
        //FramePredicate fp = getFramePredicate();
        org.cougaar.core.qos.frame.Frame  f2, f = getFrame();

        ShapeGraphic shg;
        Collection frames;
        org.cougaar.core.qos.frame.Frame fr;
        for (Iterator ii=prototypes.values().iterator(); ii.hasNext();) {
            shg = (ShapeGraphic) ii.next();
            FramePredicate pfp = shg.getFramePredicate();
            frames = (f != null ? frameHelper.getAllChildren(f, pfp.getParentRelationship()) : frameHelper.findFrames(pfp));
            if (frames != null && frames.size() > 0) {
                for (Iterator jj=frames.iterator(); jj.hasNext();)  {
                    fr = (org.cougaar.core.qos.frame.Frame) jj.next();
                    if (display.getGraphic(fr) == null && !frameChildren.contains(frame))
                        add(fr);
                }
            }
        }
    }
    /*
    protected void buildChildren(Collection newframes, Display display) {
        org.cougaar.core.qos.frame.Frame  f2, f = getFrame();
        if (f == null)
            return;
        ShapeGraphic shg;
        Collection frames;
        org.cougaar.core.qos.frame.Frame fr;
        for (Iterator ii=prototypes.values().iterator(); ii.hasNext();) {
            shg = (ShapeGraphic) ii.next();
            FramePredicate pfp = shg.getFramePredicate();
            frames = frameHelper.getAllChildren(f, pfp.getParentRelationship(), newframes);
            if (frames != null && frames.size() > 0) {
                for (Iterator jj=frames.iterator(); jj.hasNext();)  {
                    fr = (org.cougaar.core.qos.frame.Frame) jj.next();
                    if (display.getGraphic(fr) == null && !frameChildren.contains(frame))
                        add(fr);
                }
            }
        }
    }
    */


    public void setFrameHelper(FrameHelper helper, Display display) {
        if (frameHelper == null)
            super.setFrameHelper(helper, display);
        if (log.isDebugEnabled())
            log.debug("setFrameHelper:" +toString());

        buildChildren(helper, display);
        ShapeGraphic ch;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            ch=(ShapeGraphic) ii.next();
            ch.setFrameHelper(frameHelper, display);
        }
    }
   /*
    public void addedFrames(Collection newFrames, Display display) {
        if (frame == null && predicate != null)
            super.addedFrames(newFrames, display);
        buildChildren(newFrames, display);

        ShapeGraphic ch;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            ch=(ShapeGraphic) ii.next();
            ch.addedFrames(newFrames, display);
        }
    }

    public void removedFrames(Collection removedFrames, Display display) {

    }*/


    // clone thyself and assign the given frame
    // make a deep copy
    public ShapeGraphic createInstance(org.cougaar.core.qos.frame.Frame frame) {
        try {
            ShapeContainer cloned = (ShapeContainer) this.clone();
            if (frame != null)
                cloned.setFrame(frame);
            cloned.setPrototype(false);
            cloned.shapePrototype = (shapePrototype != null ? ((RectangularShape) shapePrototype.clone()) : null);
            cloned.shape = cloned.createShape();
            org.cougaar.core.qos.frame.Frame parent = getFrame();
            if (cloned.predicate != null && parent != null)
                cloned.predicate = new FramePredicate(cloned.predicate, (String) parent.getValue("name"));

            cloned.frameChildren = new HashSet();
            cloned.shapeLayout = (shapeLayout != null ? shapeLayout.cloneSelf() : null);
            cloned.children = new ArrayList();

            // clone children
            ShapeGraphic child,clonedChild;
            for (Iterator ii=children.iterator(); ii.hasNext();) {
                child = (ShapeGraphic) ii.next();
                clonedChild = child.createInstance(child.frame);
                clonedChild.setParent(cloned);
                cloned.children.add(clonedChild);
            }
            cloned.layoutChildren();
            cloned.prototypes = new HashMap();
            // clone prototypes
            String key;
            for (Iterator ii=prototypes.keySet().iterator(); ii.hasNext();) {
                key = (String) ii.next();
                child = (ShapeGraphic) prototypes.get(key);
                clonedChild = child.createInstance(child.frame);
                clonedChild.setPrototype(true);
                clonedChild.setParent(cloned);
                cloned.prototypes.put(key, clonedChild);
            }

            // clone slot change listeners
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
