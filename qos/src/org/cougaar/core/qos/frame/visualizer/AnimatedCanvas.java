package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.*;



/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 2:10:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnimatedCanvas extends AnimatingSurface implements MouseListener, MouseMotionListener {
    protected HashMap shapes;
    protected boolean mouseMoveFlag =false;
    protected Point   mousePoint=null;
    protected Point   mouseStartDragPoint=null;
    protected ShapeGraphic selectedShape;
    protected Dimension oldSize;
    private transient Logger log = Logging.getLogger(getClass().getName());

    public AnimatedCanvas() {
        super();
        shapes = new HashMap();
        selectedShape = null;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

     public void reset(int w, int h) {
        /*ShapeGraphic shape;
        for (Iterator ii=shapes.values().iterator(); ii.hasNext();) {
            shape = (ShapeGraphic) ii.next();
            shape.reset(w, h);
        }
        */
        oldSize = getSize();
    }

    public void step(int w, int h) {
        /*ShapeGraphic shape;
        for (Iterator ii=shapes.values().iterator(); ii.hasNext();) {
            shape = (ShapeGraphic) ii.next();
            shape.step(w, h);
        }
        */
    }

    public ShapeGraphic get(String shapeId) {
        return (ShapeGraphic) shapes.get(shapeId);
    }

    public void register(ShapeGraphic shape) {
       if (shapes.get(shape.getId())== null)
           shapes.put(shape.getId(), shape);
    }

    public void unregister(ShapeGraphic shape) {
        if (shapes.get(shape.getId())!= null)
           shapes.remove(shape.getId());
    }

    public void render(int w, int h, Graphics2D g2) {
        //System.out.println("SassiAnimatedSurface.render w="+w+" h="+h);
        Dimension d = getSize();
        if (oldSize.width != d.width || oldSize.height != d.height)
           reset(w,h);
	}
    
    public ShapeGraphic findShape(double x, double y) {
        //ShapeGraphic shapes[] = findShapes(x,y);
        //return (shapes != null && shapes.length > 0 ? shapes[0] : null);
        return null;
    }


    //////////////////////////////////////////////////////////////////////////////////
    // mouse event handlers
    //////////////////////////////////////////////////////////////////////////////////
    public void mouseEntered(MouseEvent evt) {}
    public void mouseExited(MouseEvent evt) {}
    public void mouseClicked(MouseEvent evt) {}

    public void mousePressed(MouseEvent evt) {
        //System.out.println("AnimatedCanvas.mousePressed");
        if(mouseMoveFlag==false) {
          // start drag
          mouseMoveFlag=true;
          mousePoint=evt.getPoint();
          mouseStartDragPoint = mousePoint;
          if (selectedShape != null)
              selectedShape.setSelected(false);
          selectedShape = findShape(mousePoint.x,  mousePoint.y);
          if (selectedShape != null)  {
              if (log.isDebugEnabled())
                log.debug("selecting = "+selectedShape);

            if (selectedShape instanceof ShapeContainer) {
              String childString="";
              ShapeGraphic ch;
              for (Iterator ii=((ShapeContainer)selectedShape).getChildren().iterator(); ii.hasNext();) {
                  ch = (ShapeGraphic)ii.next();
                  childString=childString+ ", "+ch.getId();
              }
              if (log.isDebugEnabled())
                log.debug("------>children:"+childString);
	      childString = "";
	      for (Iterator ii=((ShapeContainer)selectedShape).getPrototypes().iterator(); ii.hasNext();) {
		  ch = (ShapeGraphic) ii.next();
		  childString=childString+ ", "+ch.getId();
	      }
	      if (log.isDebugEnabled())
                log.debug("------>prototypes:"+childString);
            }
            selectedShape.setSelected(true);
          }
        }
    }

    public void mouseReleased( MouseEvent evt ){
        mouseMoveFlag=false;
        mousePoint = null;
        mouseStartDragPoint = null;
    }

    // MouseMotionListener interface
    public void mouseDragged(MouseEvent e) {
        if(mouseMoveFlag==true)
          mousePoint=e.getPoint();
    }

    public void mouseMoved(MouseEvent e) {}
}
