package org.cougaar.core.qos.frame.visualizer.event;

import javax.swing.event.ChangeEvent;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 14, 2005
 * Time: 10:52:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddedFramesEvent extends ChangeEvent {
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
