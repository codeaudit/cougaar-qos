package org.cougaar.core.qos.frame.visualizer.event;

import javax.swing.event.ChangeEvent;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 14, 2005
 * Time: 10:52:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChangedFramesEvent extends ChangeEvent {
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
