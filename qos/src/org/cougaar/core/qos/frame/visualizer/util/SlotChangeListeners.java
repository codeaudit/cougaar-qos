package org.cougaar.core.qos.frame.visualizer.util;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 15, 2005
 * Time: 4:44:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class SlotChangeListeners {

    private HashMap listeners;

    public SlotChangeListeners() {
        listeners = new HashMap();
    }

    public void add(String name, SlotChangeListener l) {
        listeners.put(name, l);
    }
    public  SlotChangeListener get(String name) {
        return getCopy(name);
    }
    private SlotChangeListener getCopy(String name) {
        SlotChangeListener s = (SlotChangeListener) listeners.get(name);
        if (s!= null)
            return (SlotChangeListener) s.cloneInstance();
        return null;
    }

    public Collection getAll() {
        return listeners.values();
    }
}
