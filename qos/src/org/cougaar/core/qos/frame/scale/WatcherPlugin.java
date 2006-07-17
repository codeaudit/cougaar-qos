package org.cougaar.core.qos.frame.scale;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.FrameSetService;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;

public class WatcherPlugin extends ParameterizedPlugin 
implements FrameSetService.Callback, PropertyChangeListener {
    
    private final UnaryPredicate relationPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (!(o instanceof Relationship)) return false;
            Relationship rel = (Relationship) o;
            return rel.getFrameSet() == frameset;
        }
    };
    
    private final UnaryPredicate thingPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (!(o instanceof Thing)) return false;
            Thing thing = (Thing) o;
            return thing.getFrameSet() == frameset;
        }
    };
    
    private FrameSet frameset;
    private LoggingService log;
    private IncrementalSubscription rsub, tsub;
    private int changeCount;
    

    public void start() {
	String frameSetName = getParameter("frame-set");
	ServiceBroker sb = getServiceBroker();
	log = (LoggingService) sb.getService(this, LoggingService.class, null);
	FrameSetService fss = (FrameSetService) sb.getService(this, FrameSetService.class, null);
	if (fss == null) {
	    log.error("Couldn't find FrameSetService");
	} else {
	    frameset = fss.findFrameSet(frameSetName, this);
	}
	super.start();
    }
    
    protected void execute() {
	log.shout(rsub.getAddedCollection().size() + " Relations added");
	log.shout(rsub.getRemovedCollection().size() + " Relations removed");
	log.shout(rsub.getChangedCollection().size() + " Relations changed");
	
	log.shout(changeCount + " slot changes");
	changeCount = 0;
	
	Collection added = tsub.getAddedCollection();
	for (Object a : added) {
	    ((Thing) a).addPropertyChangeListener(this);
	}
	log.shout(added.size() + " Things added");
	log.shout(tsub.getRemovedCollection().size() + " Things removed");
	log.shout(tsub.getChangedCollection().size() + " Things changed");
	
    }

    protected void setupSubscriptions() {
	BlackboardService bbs = getBlackboardService();
	rsub = (IncrementalSubscription) bbs.subscribe(relationPredicate);
	tsub = (IncrementalSubscription) bbs.subscribe(thingPredicate);
    }
    
    public void frameSetAvailable(String name, FrameSet set) {
	frameset = set;
	getBlackboardService().signalClientActivity();
    }

    public void propertyChange(PropertyChangeEvent evt) {
	++changeCount;
    }

}
