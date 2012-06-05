package org.cougaar.core.qos.frame.visualizer.layout;

import org.cougaar.core.qos.frame.visualizer.ShapeContainer;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 4, 2005
 * Time: 11:05:33 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ShapeLayout implements Cloneable {
    //protected ShapeContainer container;
    protected double leftMargin, rightMargin, bottomMargin,topMargin;
    protected double hpadding, vpadding;

    public ShapeLayout() {
        leftMargin=rightMargin=bottomMargin=topMargin=0d;
        hpadding=vpadding=0d;
    }

    public double left() { return leftMargin;}
    public double right() { return rightMargin;}
    public double top() { return topMargin;}
    public double bottom() { return bottomMargin;}
    public double hpadding() { return hpadding; }
    public double vpadding() {return vpadding;}
    //public void setContainer(ShapeContainer c) {
    //  container = c;
    // }

    public void setMargins(double left, double right, double bottom, double top, double hpadding, double vpadding) {
        this.leftMargin = left;
        this.rightMargin = right;
        this.bottomMargin =  bottom;
        this.topMargin = top;
        this.hpadding = hpadding;
        this.vpadding = vpadding;
    }
    public abstract void doLayout(ShapeContainer container);

    public ShapeLayout cloneSelf() {
	try {
	    return (ShapeLayout) clone();
	} catch (CloneNotSupportedException ee) {
            ee.printStackTrace();
        }
        return null;
    }
}
