package org.cougaar.core.qos.frame.visualizer.test;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.Transition;
import org.cougaar.core.qos.frame.visualizer.Task;
import org.cougaar.core.qos.frame.visualizer.util.Vec2d;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 3:26:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestTransition extends Transition {

    public static ShapeGraphic SHAPE = new Task("task","task", "job1");

    public TestTransition(Point2D.Double fromP, Point2D.Double toP) {
        super(SHAPE, null, null);
        this.fromPos = fromP;
        this.toPos   = toP;
        Rectangle2D r = shape.getBounds2D();
        shape.reshape(fromPos.x, fromPos.y, r.getWidth(), r.getHeight());
    }

    public boolean step() {
        Rectangle2D r = shape.getBounds2D();
        double tx=r.getX(),ty=r.getY();


	if (firstStep) {
        //fromContainer.remove(shape);
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

	    //if (log.isDebugEnabled())
		//log.debug("starting transition, shape='"+shape.getId()+"' from '"+fromContainer.getId()+"' to '"+toContainer.getId()+"'start x="+fromPos.x+" y="+fromPos.y+"  end x="+toPos.x+" y="+toPos.y+"  length="+tLength+" angle = "+angle);

        System.out.println("angle = "+ r2d((float)angle));
    }



        //Rectangle2D bounds = shape.getShape().getBounds2D();
        if (((int)tx == (int)toPos.x && (int)ty == (int)toPos.y) ){  //|| toContainer.contains(tx,ty))  {
            lastStep = true;
        }
        else {
	    Vec2d t = new Vec2d((tx-fromPos.x), (ty-fromPos.y));
	    double length = t.length();
            if (log.isDebugEnabled())
		log.debug("current vector length="+length+" target length="+tLength);

	    if (length >= tLength)
		lastStep = true;
        }
        if (lastStep)  {
            //toContainer.add(shape);
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
    }
}
