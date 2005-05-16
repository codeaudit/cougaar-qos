package org.cougaar.core.qos.frame.visualizer.util;

import org.cougaar.core.qos.frame.Frame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 25, 2005
 * Time: 9:23:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class FrameChanges {
    Frame frame;
    ArrayList changeReports;

    public FrameChanges(Frame frame) {
        this.frame = frame;
        changeReports = new ArrayList();
    }

    public void addChange(Frame.Change ch) {
        changeReports.add(ch);
    }
    public void addChanges(Collection changes) {
        changeReports.addAll(changes);    
    }

    public Frame getFrame() {
        return frame;
    }
    public Collection getChangeReports() {
        return changeReports;
    }
}