package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

public abstract class ShapeGraphic implements Cloneable {
    protected String id, label;
    protected Shape shape;
    protected double x,y,width,height;
    protected boolean visible, selected, isPrototype;
    protected org.cougaar.core.qos.frame.Frame frame;
    protected String frameidSlotName = "";
    protected FramePredicate predicate;
    protected FrameHelper frameHelper;
    protected ShapeContainer parent;
    protected Logger log = Logging.getLogger(getClass().getName());


    protected ShapeGraphic() {
        this.id = null;
        this.label = null;
        x=y=0d;
        width=height=10d; // default?
        visible = true;
        selected = false;
        isPrototype = false;
    }

    protected ShapeGraphic(String id, String label) {
        this.id = id;
        this.label = label;
        x=y=0d;
        width=height=0d;
        visible = true;
        selected = false;
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

    public void setFrame(org.cougaar.core.qos.frame.Frame frame) {
        this.frame = frame;
	if (frame != null) {
	    this.id = (String) frame.getValue("name"); //frameidSlotName);
	    this.label = id;
	    if (log.isDebugEnabled())
		log.debug(label+".setFrame():  id="+this.id+":  "+this.toString());
	} else {
	    this.id = "";
	    this.label = "";
	}
    }

    
    public void setFrameHelper(FrameHelper helper) {
	frameHelper = helper;
	if (frame == null && predicate != null) {
	    setFrame(frameHelper.findFrame(predicate));
	}
    }

    /*
    protected org.cougaar.core.qos.frame.Frame findFrame(Collection frames, String frameName) {
        org.cougaar.core.qos.frame.Frame f;
        String fname;
        for (Iterator ii=frames.iterator(); ii.hasNext();) {
           f = (org.cougaar.core.qos.frame.Frame) ii.next();
           fname = ((String)f.getValue("name"));
           if (fname != null && fname.equals(frameName)) {
               return f;
           }
       }
       return null;
   }

    protected org.cougaar.core.qos.frame.Frame findFrame(Collection frames, FramePredicate predicate) {
       org.cougaar.core.qos.frame.Frame f;
       for (Iterator ii=frames.iterator(); ii.hasNext();) {
           f = (org.cougaar.core.qos.frame.Frame) ii.next();
           if (predicate.execute(f))
               return f;
       }
       return null;
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

    public void setFrames(Collection frames) {
        if (frame == null && predicate != null)
            setFrame(findFrame(frames, predicate));
    }

    */

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
    }

    public ShapeContainer getParent() {
        return parent;
    }

    public ShapeContainer getParentWithFrame() {
        ShapeContainer p = parent;
        org.cougaar.core.qos.frame.Frame f;
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

    protected abstract Shape createShape();

    public abstract boolean contains(double mx, double my);

    public abstract ShapeGraphic find(double mx, double my);

    public ShapeGraphic find(org.cougaar.core.qos.frame.Frame f) {
	if (frame != null && id != null && id.equals((String) f.getValue("name")))
	    return this;
	return null;
    }

    public void reshape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height= height;
    }

    public Point2D.Double getPosition() {
        return new Point2D.Double(x,y);
    }

    public abstract void draw(Graphics2D g2);

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
    public ShapeGraphic createInstance(org.cougaar.core.qos.frame.Frame frame) {
        try {
            ShapeGraphic cloned = (ShapeGraphic) this.clone();
	    if (frame != null)
		cloned.setFrame(frame);
            cloned.setPrototype(false);
	    cloned.shape = cloned.createShape();
	    org.cougaar.core.qos.frame.Frame parent = getFrame();
	    if (cloned.predicate != null && parent != null) 
		cloned.predicate = new FramePredicate(cloned.predicate, (String) parent.getValue("name"));
	    
	    return cloned;
        } catch (CloneNotSupportedException ee) {
            ee.printStackTrace();
        }
        return null;
    }
}
