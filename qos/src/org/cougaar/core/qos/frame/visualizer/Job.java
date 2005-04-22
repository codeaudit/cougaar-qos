package org.cougaar.core.qos.frame.visualizer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 1:01:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Job extends ShapeGraphic {
    Ellipse2D.Double ellipse;
    ArrayList tasks;
    Paint paint, selectedPaint, failedPaint, donePaint;

    public Job() {
        super();
        this.tasks = new ArrayList();
        createShape();
        reshape(x,y,25d,25d);
    }

    public Job(String id, String label, Collection tasks) {
        super(id, label);
        this.tasks = new ArrayList();
        this.tasks.addAll(tasks);
        createShape();
        reshape(x,y,25d,25d);
    }

    public Collection getTasks() {
        return tasks;
    }

    public boolean isFinished() {
        for (Iterator ii=tasks.iterator(); ii.hasNext();)
            if (! ((Task)ii.next()).isFinished())
                return false;
        return true;
    }

    public boolean hasFailed() {
        for (Iterator ii=tasks.iterator(); ii.hasNext();)
            if ( ((Task)ii.next()).hasFailed())
                return true;
        return false;
    }

    protected Shape createShape() {
        paint = new GradientPaint(0,0,Color.blue,(float)width*.35f,(float)height*.35f,Color.blue);
        selectedPaint = new  GradientPaint(0,0,Color.magenta,(float)width*.35f,(float)height*.35f,Color.magenta);
        failedPaint =  new  GradientPaint(0,0,Color.red,(float)width*.35f,(float)height*.35f,Color.red);
        donePaint = new  GradientPaint(0,0,Color.green,(float)width*.35f,(float)height*.35f,Color.green);
        ellipse = new Ellipse2D.Double(x,y,width,height);
        return ellipse;
    }

    public boolean contains(double mx, double my) {
        return ellipse.contains(mx,my);
    }

    public ShapeGraphic find(double mx, double my) {
        return (contains(mx,my) ? this : null);
    }

    public void reshape(double tx, double ty, double w, double h) {
        super.reshape(tx,ty,w,h);
        ellipse.x = tx;
        ellipse.y = ty;
        ellipse.width = w;
        ellipse.height=h;
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        for (Iterator ii=tasks.iterator(); ii.hasNext();)
            ((Task)ii.next()).setSelected(selected);
    }

    public void draw(Graphics2D g2) {
        if (hasFailed())
            g2.setPaint(failedPaint);
        else if (isFinished())
            g2.setPaint(donePaint);
        else
            g2.setPaint(paint);
        if (isSelected())
            g2.setPaint(selectedPaint);

        g2.draw(ellipse);
        if (label != null)
           g2.drawString(label, (int)(ellipse.x+(.25*ellipse.width)), (int)(ellipse.y+(.7*ellipse.height)));
    }
}
