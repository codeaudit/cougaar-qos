package org.cougaar.core.qos.frame.visualizer;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 1:11:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class Task extends ShapeGraphic {
    Ellipse2D.Double ellipse;
    String jobId;
    Paint paint, selectedPaint, donePaint, failedPaint;
    boolean isFinished;
    boolean hasFailed;

    public Task() {
        this("","","");
    }
    public Task(String id, String label, String jobId) {
        super(id, label);
        this.jobId = jobId;
        createShape();
        reshape(x,y,20d,20d);
        isFinished = false;
        hasFailed = false;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    public void setFailed(boolean failed) {
        hasFailed = failed;
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
        ellipse.height= h;
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

