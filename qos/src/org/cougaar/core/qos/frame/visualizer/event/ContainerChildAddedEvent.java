package org.cougaar.core.qos.frame.visualizer.event;

import javax.swing.event.ChangeEvent;

import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 14, 2005
 * Time: 2:36:33 PM
 * To change this template use File | Settings | File Templates.
 */

public class ContainerChildAddedEvent extends ChangeEvent {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   ShapeContainer container;
    ShapeGraphic newChild;


    public ContainerChildAddedEvent(Object source, ShapeContainer container, ShapeGraphic newchild) {
	    super(source);
        this.container = container;
        this.newChild = newchild;
    }

    public ShapeContainer getContainer() {
        return container;
    }

    public ShapeGraphic getChild() {
        return newChild;
    }
}