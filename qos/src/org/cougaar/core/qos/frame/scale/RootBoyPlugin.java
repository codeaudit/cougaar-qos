package org.cougaar.core.qos.frame.scale;

import java.util.Enumeration;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.FrameSetService;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;

public class RootBoyPlugin extends ParameterizedPlugin implements FrameSetService.Callback {
    private FrameSet frameset;
    private Root root;
    private LoggingService log;
    private IncrementalSubscription sub;
    
    private final UnaryPredicate rootPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (!(o instanceof Root)) return false;
            Root thing = (Root) o;
            return thing.getFrameSet() == frameset;
        }
    };
    
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
	if (sub.hasChanged()) {
	    Enumeration e;
	    
	    e = sub.getAddedList();
	    if (e.hasMoreElements()) {
		this.root = (Root) e.nextElement();
		this.root.setRootSlotFloat(1f);
	    }
	    e = sub.getChangedList();
	    if (e.hasMoreElements()) {
		this.root.setRootSlotFloat(this.root.getRootSlotFloat()+1);
	    }
	}
    }

    protected void setupSubscriptions() {
	BlackboardService bbs = getBlackboardService();
	sub = (IncrementalSubscription) bbs.subscribe(rootPredicate);
    }

    public void frameSetAvailable(String name, FrameSet set) {
	this.frameset = set;
    }

}
