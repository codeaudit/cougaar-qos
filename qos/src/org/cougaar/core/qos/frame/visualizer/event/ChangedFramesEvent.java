package org.cougaar.core.qos.frame.visualizer.event;

import java.util.HashMap;

import javax.swing.event.ChangeEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 14, 2005
 * Time: 10:52:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChangedFramesEvent extends ChangeEvent {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   HashMap changedDataFrames, changedRelationFrames;


    public ChangedFramesEvent(Object source, HashMap changedDataFrames, HashMap changedRelationFrames) {
	    super(source);
        this.changedDataFrames = changedDataFrames;
        this.changedRelationFrames = changedRelationFrames;
    }

    public HashMap getChangedDataFrames() {
        return changedDataFrames;
    }

    public HashMap getChangedRelationFrames() {
        return changedRelationFrames;
    }
}
