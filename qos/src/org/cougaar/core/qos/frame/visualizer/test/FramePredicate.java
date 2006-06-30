package org.cougaar.core.qos.frame.visualizer.test;

import org.cougaar.core.qos.frame.Frame;


/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 8, 2005
 * Time: 3:11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FramePredicate {
    String kind, name, parentRelationship;
    //String frameset;

    public FramePredicate(String kind, String name, String parentRelationship) {
        this.kind = kind;
        this.name = name;
        //this.frameset = null;
        this.parentRelationship = parentRelationship;
    }

    public FramePredicate(FramePredicate p, String name) {
        this.name = name;
        this.kind = p.kind;
        //this.frameset = null;
        this.parentRelationship = p.parentRelationship;
    }

    //public void setFrameSetName(String frameSetName) {
      //  this.frameset = frameSetName;
    //}

    public String getKind() {
        return kind;
    }

    // a hack
    public String getParentRelationship() {
        return parentRelationship;
    }

    public boolean execute(Object o) {
        Frame f;
        if (o instanceof Frame) {
            f = (Frame) o;
            //System.out.println("comparing  f.isa('"+kind+"' name='"+name+"') = "+f.isa(kind));

            boolean nameBool = true;
            boolean kindBool = true;

            kindBool     = (kind != null ? f.isa(kind) : true);
            if (!kindBool)
                return false;
            nameBool = (name!=null ? ((String)f.getValue("name")).equals(name) : true);
            //framesetBool = (frameset !=null ? f.getFrameSet().getName().equals(frameset) : true);


            return nameBool && /*framesetBool &&*/ kindBool;
        }
        return false;
    }

    public String toString() {
        return "<Predicate: kind='"+kind+"' name='"+name+"'>";// frameset="+frameset+">";

    }
}
