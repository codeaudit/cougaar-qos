
package org.cougaar.core.qos.frame.visualizer.util;


import java.io.Serializable;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * ChangeModel - Maintains a Vector
 * of ChangeListeners and provides add & remove methods for adding/
 * removing listeners. Also provides method to notify listeners of a change.
 */
public class ChangeModel implements Serializable {
    
    protected Vector      listeners;
    private   ChangeEvent defaultChangeEvent;
    private   boolean     enabled;
    private   boolean debug = false;
    
    /**
     * ChangeModel - constructor
     *
     */
    public ChangeModel() {
	listeners           = new Vector();   
	defaultChangeEvent  = new ChangeEvent(this);
	enabled             = true;
    }
    
    public boolean hasListener(ChangeListener listener) {
	return listeners.indexOf(listener) > -1;
    }
    
    /**
     * addListener - adds ChangeListener to be notified when ChangeModel
     * changes.
     *
     * @param listener ChangeListener who wants to be notified of changes
     */
    public void addListener(ChangeListener listener) {
	listeners.addElement(listener);
    }
    
    /**
     * removeListener - removes ChangeListener from list of listeners. 
     * ChangeListener will no longer be notified when ChangeModel
     * changes.
     *
     * @param listener ChangeListener who no longer wants to be notified of 
     * changes
     */
    public void removeListener(ChangeListener listener)  {
	listeners.removeElement(listener);
    }
    
    /**
     * If enabled, events will be sent to the listeners, otherwise
     * no events will be sent.
     *
     * @param enabled -- boolean flag controlling whether 
     * notifications are sent out. Notifications are not cached!!!!
     */
    protected void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }
  
    /**
     * Returns whether or not to hold notifications.
     *
     * @return boolean true if currently ignoring notifications, else false.
     */
    protected boolean isEnabled()  {
	return enabled;
    }
    
    public void notifyListeners() {
	notifyListeners(defaultChangeEvent);
    }
    /**
     * notifyListeners - send ChangeEvent to all listeners
     *
     * @param event ChangeEvent to used
     */
    /*synchronized*/ public void notifyListeners(ChangeEvent event) {
	if (!isEnabled())
	    return;
        if (listeners.size() > 0)  {
	    ChangeListener observers[] = new ChangeListener[listeners.size()];	
	    listeners.copyInto(observers);
	    
	    for (int i = 0; i < observers.length; i++)  {
		if (debug)
		    System.out.println("+++notifying listener: " + observers[i].getClass().getName());
		observers[i].stateChanged(event);
	    }
	}
    }
        
    public int numListeners() {
	return listeners.size();
    }

    public void clearListeners() {
	listeners.clear();
    }
}
