package org.cougaar.core.qos.frame.visualizer;


import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.cougaar.core.qos.frame.visualizer.util.Vec2d;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 3:26:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Transition {
    protected Point2D.Double fromPos;
    protected Point2D.Double toPos;
    protected ShapeGraphic shape;
    protected ShapeContainer fromContainer, toContainer;
    protected boolean firstStep, lastStep;
    //double increment = 30d;
    protected double angle, speed = 30d, tLength, xoff,yoff;

    protected Vec2d startVec, endVec, transVec;

    protected  transient Logger log = Logging.getLogger(getClass().getName());


    public Transition(ShapeGraphic shape, ShapeContainer fromContainer, ShapeContainer toContainer) {
        this.shape = shape;
        this.fromContainer = fromContainer;
        this.toContainer = toContainer;
        this.fromPos = (fromContainer != null ? fromContainer.getNextInsertPosition(): null);
        this.toPos   = (toContainer != null ? toContainer.getNextInsertPosition() : null);
        firstStep = true;
        lastStep = false;
    }

    public boolean isFinished() {
        return lastStep;
    }

    // radians to degrees
    public static float r2d(float radians) {
        return ((radians /(float)(2.0 * Math.PI))* 360.0f)%360 ;
    }

    // degree to radians
    public static float d2r(float angle)  {
        return ((float)(angle * 2.0 * Math.PI)) / 360.0f ;
    }

    public boolean step() {
        Rectangle2D r = shape.getBounds2D();
        double tx=r.getX(),ty=r.getY();
        this.fromPos = fromContainer.getNextInsertPosition();
        this.toPos   = toContainer.getNextInsertPosition();

        //double xoff=0d, yoff=0d;

        if (firstStep) {
            if (fromContainer != null)  {
                fromContainer.remove(shape);
                shape.resetSize();
            }

            if (!Display.ENABLE_ANIMATION || fromPos == null) {
                lastStep = true;
                toContainer.add(shape);
                return true;
            }


            firstStep = false;
            shape.reshape(fromPos.x, fromPos.y, r.getWidth(), r.getHeight());

            startVec = new Vec2d(fromPos.x, fromPos.y);
            endVec   = new Vec2d(toPos.x, toPos.y);

            transVec = endVec.minus(startVec);
            tLength = transVec.length();

            startVec.normalize();
            endVec.normalize();
            transVec.normalize();

            double d= (transVec.getY() / (transVec.getX() == 0d ? 0.01 : transVec.getX()));
            angle = Math.atan(d);
            xoff = (transVec.getX() < 0 ? -1d : 1d) * speed*Math.cos(angle);
            yoff = (transVec.getX() < 0 ? -1d : 1d) * speed*Math.sin(angle);

            if (log.isDebugEnabled())
                log.debug("starting transition, shape='"+shape.getId()+"' from '"+fromContainer.getId()+"' to '"+toContainer.getId()+"'start x="+fromPos.x+" y="+fromPos.y+"  end x="+toPos.x+" y="+toPos.y+"  length="+tLength+" angle = "+angle);
        }

        //Rectangle2D bounds = shape.getShape().getBounds2D();
        if (((int)tx == (int)toPos.x && (int)ty == (int)toPos.y) || toContainer.contains(tx,ty))  {
            lastStep = true;
        } else {
            Vec2d t = new Vec2d((tx-fromPos.x), (ty-fromPos.y));
            double length = t.length();
            if (log.isDebugEnabled())
                log.debug("current vector length="+length+" target length="+tLength);

            if (length >= tLength)
                lastStep = true;
        }
        if (lastStep)  {
            toContainer.add(shape);
            // temp hack to get the slot listeners to update the values
            //shape.validateListeners();
            return true;
        }

        tx += xoff;
        ty += yoff;


        //if (log.isDebugEnabled())
        //  log.debug("transition= tx"+tx+" ty="+ty);

        /*
        if ((int) tx < (int) toPos.x) tx+=increment;//tx++;
        else if ((int) tx > (int) toPos.x) tx-=increment;//tx--;
        else if ((int) tx == (int) toPos.x || toContainer.contains(tx,ty)) tx = toPos.x;

        if ((int) ty < (int) toPos.y) ty+=increment;//ty++;
        else if ((int) ty > (int) toPos.y) ty-=increment;//ty--;
        else if ((int) ty == (int) toPos.y || toContainer.contains(tx,ty)) ty = toPos.y;
        */
        shape.reshape(tx, ty, r.getWidth(), r.getHeight());
        return false;
    }

    public void draw(Graphics2D g2) {
        shape.draw(g2);
        shape.drawLabel(g2);
    }

    public String toString() {
        String from = (fromContainer != null ? fromContainer.id : "null");
        String to = (toContainer != null ? toContainer.id : "null");
        return "Transition:  move '"+shape.id+"'  from '"+from+"' to '"+to+"'";
    }
}
