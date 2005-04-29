package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.visualizer.test.ContainsPredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 9:42:49 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ShapeContainer extends ShapeGraphic {
    protected ArrayList children;
    protected HashMap prototypes;//, insertContainers;
    protected HashMap frameChildren;
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
	    frameChildren = new HashMap();
        prototypes = new HashMap();
        //insertContainers = new HashMap();
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

    public boolean contains(double mx, double my) {
        return (getShape().contains(mx,my));
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

    public abstract Point2D.Double getNextInsertPosition();

    protected void drawChildren(Graphics2D g2) {
        ShapeGraphic sh;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            sh=(ShapeGraphic) ii.next();
            if (sh.isVisible())
                sh.draw(g2);
        }
    }

    //public boolean hasPrototype(String kind) {
    //  return  (prototypes.get(kind) != null);
    // }

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
            log.debug("====>add: "+toString()+"\n\t  adding  "+sh);
        sh.setParent(this);
        if (sh.isPrototype())
            prototypes.put(sh.getFramePredicate().getKind(), sh);
        else {
	    if (!hasChild(sh)) {
		children.add(sh);
		layoutChildren();
	    }
        }
    }

    
    public void add(org.cougaar.core.qos.frame.Frame frame) {
        String kind = frame.getKind();
        ShapeGraphic shg;
        FramePredicate fp;

        if (log.isDebugEnabled())
                log.debug("====>maybe-add: "+toString()+"\n\t  adding  frame(kind="+kind+", name="+frame.getValue("name")+")");


        //for (Iterator ii=children.iterator(); ii.hasNext();) {
        //  shg = (ShapeGraphic) ii.next();
            // we already have a child of with this name so return
            //if (shg.frame != null && ((String)shg.frame.getValue("name")).equals((String)frame.getValue("name")))
            //return;
        //}

        String frameName = (String) frame.getValue("name");
        if (frameChildren.get(frameName) != null) {
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
                frameChildren.put(frameName, frame);
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
            frameChildren.remove((String)sh.frame.getValue("name"));
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

    /*
    public Collection processChildFrames(Collection containFrames, Collection frames) {
        org.cougaar.core.qos.frame.Frame  frame, f;
        String fname;
        ArrayList alist = new ArrayList();
        for (Iterator ii=containFrames.iterator(); ii.hasNext(); ) {
            fname = (String) ((org.cougaar.core.qos.frame.Frame)ii.next()).getValue("child-value");
            f= findFrame(frames, fname);
            if (f!=null)
                alist.add(f);
        }
        return alist;
    }
    */

//    public String findPrototype(String kind) {
//  }



    public void setFrameHelper(FrameHelper helper) {
        super.setFrameHelper(helper);
        if (log.isDebugEnabled())
            log.debug("setFrameHelper:" +toString());

        FramePredicate fp = getFramePredicate();
        org.cougaar.core.qos.frame.Frame  f2, f = getFrame();


        if (f != null) {
            String frameName = (String) f.getValue("name");
            Collection childFrames = frameHelper.getAllChildren(f, (fp!=null?fp.getParentRelationship():"contains"));


            for (Iterator ii=childFrames.iterator(); ii.hasNext(); ) {
            // add if the frame matches any of the prototypes
            add((org.cougaar.core.qos.frame.Frame) ii.next());

            }
            /*String kind;
            ShapeContainer sh;
            ShapeGraphic shg;


            for (Iterator ii=prototypes.values().iterator(); ii.hasNext();) {
            shg = (ShapeGraphic) ii.next();
            // now find out which of the child frames can be created by this ShapeContainer
            // --> check which childrent match any of the prototypes
            Collection c = frameHelper.findFrames(childFrames, new FramePredicate(shg.predicate, frameName));

            for (Iterator jj=c.iterator(); jj.hasNext();) {
                f2=(org.cougaar.core.qos.frame.Frame) jj.next();
                if (log.isDebugEnabled())
                log.debug("\t frame:"+frameName+" adding "+f2.getValue("name"));
                add( f2 );
            }
            }*/

        }
        ShapeGraphic ch;
        for (Iterator ii=children.iterator(); ii.hasNext();) {
            ch=(ShapeGraphic) ii.next();
            ch.setFrameHelper(frameHelper);
        }
    }

    

    // clone thyself and assign the given frame
    // make a deep copy
    public ShapeGraphic createInstance(org.cougaar.core.qos.frame.Frame frame) {
        try {
            ShapeContainer cloned = (ShapeContainer) this.clone();
            if (frame != null)
                cloned.setFrame(frame);
            cloned.setPrototype(false);
            cloned.shape = cloned.createShape();
            org.cougaar.core.qos.frame.Frame parent = getFrame();
            if (cloned.predicate != null && parent != null)
            cloned.predicate = new FramePredicate(cloned.predicate, (String) parent.getValue("name"));

            cloned.frameChildren = new HashMap();
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
	    
	        return cloned;
        } catch (CloneNotSupportedException ee) {
            ee.printStackTrace();
        }
        return null;
    }
    





    /*
  public void setFrames(Collection frames) {
    if (log.isDebugEnabled())
	    log.debug("setFrames:" +toString());
    super.setFrames(frames);

    org.cougaar.core.qos.frame.Frame  f2, f = getFrame();
    FramePredicate fp = getFramePredicate();
    if (f != null) {
        String frameName = (String) f.getValue("name");
	Set containedFrames = f.getFrameSet().findRelations(f, "parent", (fp!=null?fp.getParentRelationship():"contains"));

        //Collection containFrames = findFrames(frames, new ContainsPredicate((fp!=null?fp.getParentRelationship():"contains"), frameName));
        //Collection containedFrames= processChildFrames(containFrames, frames);

        String kind;
        ShapeContainer sh;
        ShapeGraphic shg;

        for (Iterator ii=prototypes.values().iterator(); ii.hasNext();) {
            shg = (ShapeGraphic) ii.next();
	    //Frame child = findFrame(child_proto, child_slot, child_value);
            Collection c = findFrames(containedFrames, new FramePredicate(shg.predicate, frameName));

            for (Iterator jj=c.iterator(); jj.hasNext();) {
                f2=(org.cougaar.core.qos.frame.Frame) jj.next();
                if (log.isDebugEnabled())
                    log.debug("\t frame:"+frameName+" adding "+f2.getValue("name"));
                add( f2 );
            }
        }
    
    }
    ShapeGraphic ch;
    for (Iterator ii=children.iterator(); ii.hasNext();) {
	ch=(ShapeGraphic) ii.next();
	ch.setFrames(frames);
    }
  }
    */



  /*
    protected Collection getFrameChildren(Collection frames, org.cougaar.core.qos.frame.Frame frame) {
        ArrayList ch = new ArrayList();
        org.cougaar.core.qos.frame.Frame f;
        for (Iterator ii=frames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.Frame) ii.next();
            if (f.isa("contains") && f.getValue("parent-value").equals(frame.getValue("name") ))
                ch.add(f);
        }
        System.out.println("getFrameChildren -- found "+ch.size()+" children");
        return ch;
    }
   */


    // clone thyself and assign the given frame
    //public ShapeGraphic createInstance(org.cougaar.core.qos.frame.Frame frame) {
    //ShapeGraphic cloned = super.createInstance(frame);
    //  return cloned;
    //}

}
