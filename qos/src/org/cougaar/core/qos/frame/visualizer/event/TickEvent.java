package org.cougaar.core.qos.frame.visualizer.event;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.ChangeEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 1, 2005
 * Time: 9:50:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class TickEvent extends ChangeEvent {
    int tickNumber;
    String label;
    Collection transitions;


    public TickEvent(Object source, int tickNumber, String label) {
	this(source, tickNumber, label, new ArrayList());
    }

    public TickEvent(Object source, int tickNumber, String label, Collection transitions) {
        super(source);
        this.tickNumber = tickNumber;
        this.label = label;
        this.transitions = transitions;
    }

    public int getTickNumber() { return tickNumber; }
    public String getLabel() { return label; }

    public String toString() {
        return "TickEvent ["+tickNumber+", "+label+" #transitions="+transitions.size()+"]";
    }


    public Collection getTransitions() { return transitions; }
}
