package org.cougaar.core.qos.frame.visualizer.test;


import org.cougaar.core.qos.frame.Frame;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 11, 2005
 * Time: 2:58:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContainsPredicate extends FramePredicate {
    String parent;

    public ContainsPredicate(String relationship, String parent) {
        this(relationship, parent, null);
    }
    public ContainsPredicate(String relationship, String parent, String frameset) {
        super(relationship, null, frameset, relationship);
        this.parent = parent;
    }

    public boolean execute(Object o) {
        Frame f;

        if (o instanceof Frame) {
            f = (Frame) o;
	    boolean kindBool = (kind != null ?  f.isa(kind) : true);
	    if (!kindBool)
		return false;
            String p = (String) f.getValue("parent-value");
	    return (p!= null && p.equals(parent));
        }
        return false;
    }

    public String toString() {
        return "<ContainsPredicate: kind="+kind+" name="+name+" frameset="+frameset+">";
    }
}

