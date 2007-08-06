/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import org.cougaar.qos.ResourceStatus.RSSSubscriber;
import org.cougaar.qos.ResourceStatus.ResourceNode;


class RSSSubscriberProxy implements Observer
{
    private BoundDataFormula bdf;
    private RSSSubscriber subscriber;
    private ResourceStatusServiceImpl service;
    private int callback_id;
    Logger logger;

    private class Updater implements Runnable {
	private org.cougaar.qos.ResourceStatus.DataValue corbaDataValue;
	
	Updater(org.cougaar.qos.ResourceStatus.DataValue value) {
	    this.corbaDataValue = value;
	}
	    
	public void run() {
	    synchronized(RSSSubscriberProxy.this) {
		if (subscriber != null) {
		    try {
			subscriber.dataUpdate(callback_id, corbaDataValue);
		    } catch (Exception ex) {
			// silently assume remote object is dead
			service.unsubscribe(subscriber, bdf.getDescription());
		    }
		}
	    }
	}
    }

    RSSSubscriberProxy(BoundDataFormula bdf,
		       RSSSubscriber subscriber,
		       int callback_id,
		       ResourceStatusServiceImpl service)
    {
	this.bdf = bdf;
	this.subscriber = subscriber;
	this.callback_id = callback_id;
	this.service = service;
	logger = Logging.getLogger(RSSSubscriberProxy.class);
	bdf.addObserver(this);
    }

    public void update(Observable o, Object value) {
	DataValue v = (DataValue) value;
	if (logger.isDebugEnabled()) logger.debug("Update: " +value);
	org.cougaar.qos.ResourceStatus.DataValue corbaDataValue = 
	    v.getCorbaValue();
	// Do the actual CORBA call in a dedicated thread, so as not
	// to tie up the caller's thread.
	RSSUtils.schedule(new Updater(corbaDataValue), 0);
    }
    
    boolean hasPath(ResourceNode[] path)
    {
	ResourceNode[] candidate = bdf.getDescription();
	if (candidate.length != path.length) return false;
	for (int i=0; i<candidate.length; i++) {
	    if (!candidate[i].equals(path[i])) return false;
	}
	return true;
    }

    synchronized void unbind() {
	if (bdf != null) {
	    bdf.deleteObserver(this);
	    bdf.unsubscribe();
	    bdf = null;
	    subscriber = null;
	}
    }
    
}
