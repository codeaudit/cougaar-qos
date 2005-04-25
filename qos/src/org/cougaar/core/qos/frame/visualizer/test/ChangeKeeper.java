package org.cougaar.core.qos.frame.visualizer.test;

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
public class ChangeKeeper {
    Frame frame;
    ArrayList changeReports;

    public ChangeKeeper(Frame frame) {
        this.frame = frame;
        changeReports = new ArrayList();
    }

    public void addChange(Frame.Change ch) {
        changeReports.add(ch);
    }
    public Frame getFrame() {
        return frame;
    }
    public Collection getChangeReports() {
        return changeReports;
    }
}