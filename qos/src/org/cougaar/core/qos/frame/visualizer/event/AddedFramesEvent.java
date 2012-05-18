package org.cougaar.core.qos.frame.visualizer.event;

import java.util.HashSet;

import javax.swing.event.ChangeEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 14, 2005
 * Time: 10:52:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddedFramesEvent extends ChangeEvent {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   HashSet newDataFrames, newRelationFrames;


    public AddedFramesEvent(Object source, HashSet newDataFrames, HashSet newRelationFrames) {
	    super(source);
        this.newDataFrames = newDataFrames;
        this.newRelationFrames = newRelationFrames;
    }

    public HashSet getAddedDataFrames() {
        return newDataFrames;
    }

    public HashSet getAddedRelationFrames() {
        return newRelationFrames;
    }
}
