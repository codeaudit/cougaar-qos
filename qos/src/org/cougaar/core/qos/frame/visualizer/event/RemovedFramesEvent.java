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
public class RemovedFramesEvent extends ChangeEvent {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   HashSet removedDataFrames, removedRelationFrames;


    public RemovedFramesEvent(Object source, HashSet removedDataFrames, HashSet removedRelationFrames) {
	    super(source);
        this.removedDataFrames = removedDataFrames;
        this.removedRelationFrames = removedRelationFrames;
    }

    public HashSet getRemovedDataFrames() {
        return removedDataFrames;
    }

    public HashSet getRemovedRelationFrames() {
        return removedRelationFrames;
    }
}
