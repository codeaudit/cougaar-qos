package org.cougaar.core.qos.frame.visualizer.util;



public class Vec2d {
    private double x;
    private double y;
    
    public Vec2d() {}
    
    public Vec2d(Vec2d v) {
	this(v.x, v.y);
    }
    
    public Vec2d(double x, double y) {
	setXY(x, y);
    }
    
    public Vec2d copy() {
	return new Vec2d(this);
    }
    
    public void setXY(double x, double y) {
	this.x = x;
	this.y = y;
    }
    
    public void setX(double x) {
	this.x = x;
    }

    public void setY(double y) {
	this.y = y;
    }

    public double getX() { 
	return x; 
    }
    public double getY() { 
	return y; 
    }
        
    public double dot(Vec2d v) {
	return x * v.x + y * v.y;
    }
    
    public double length() {
	return Math.sqrt(lengthSquared());
    }
    
    public double lengthSquared() {
	return this.dot(this);
    }
    
    public void normalize() {
	double len = length();
	if (len == 0.0f) return;
	scale(1.0f / len);
    }
    
    public Vec2d times(double val) {
	Vec2d newVec = new Vec2d(this);
	newVec.scale(val);
	return newVec;
    }

    public void scale(double val) {
	x *= val;
	y *= val;
    }
    
    public Vec2d plus(Vec2d arg) {
	Vec2d newVec = new Vec2d();
	newVec.add(this, arg);
	return newVec;
    }
    
    public void add(Vec2d b) {
	add(this, b);
    }
    
    public void add(Vec2d a, Vec2d b) {
	x = a.x + b.x;
	y = a.y + b.y;
    }
    
    public Vec2d addScaled(double s, Vec2d arg) {
	Vec2d newVec = new Vec2d();
	newVec.addScaled(this, s, arg);
	return newVec;
    }
    
    public void addScaled(Vec2d a, double s, Vec2d b) {
	x = a.x + s * b.x;
	y = a.y + s * b.y;
    }
    
    public Vec2d minus(Vec2d arg) {
	Vec2d newVec = new Vec2d();
	newVec.sub(this, arg);
	return newVec;
    }
    
    public void sub(Vec2d b) {
	sub(this, b);
    }
    
    public void sub(Vec2d a, Vec2d b) {
	x = a.x - b.x;
	y = a.y - b.y;
    }
    
    @Override
   public String toString() {
	return "Vec2d["+x+", "+y+"]";
    }
}
