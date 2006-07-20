/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.core.qos.frame.scale;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.Frame;
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
    
    private final UnaryPredicate rootPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (!(o instanceof Root)) return false;
            Thing thing = (Thing) o;
            return thing.getFrameSet() == frameset;
        }
    };
    
    private FrameSet frameset;
    private LoggingService log;
    private IncrementalSubscription rsub, tsub, rootSub;
    private int propertyChangeCount;
    private int blackboardChangeCount;
    private long lastLog = 0;

    private int rootChangeCount;
    

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
	Collection added = tsub.getAddedCollection();
	for (Object a : added) {
	    ((Thing) a).addPropertyChangeListener(this);
	}
	
	Collection changed = tsub.getChangedCollection();
	blackboardChangeCount += changed.size();
	
	Collection rootCollection = rootSub.getChangedCollection();
	Root root = null;
	int changeCount = 0;
	for (Object r : rootCollection) {
	    root = (Root) r; // there's only one Root
	    Set changeReports = rootSub.getChangeReports(r);
	    for (Object rawChange : changeReports) {
		if (rawChange instanceof Frame.Change) {
		    Frame.Change change = (Frame.Change) rawChange;
		    if (change.getSlotName().equals("rootSlotFloat")) {
			++changeCount;
		    }
		}
	    }
	}
	if (changeCount > 0) {
	    root.setCount(root.getCount() + 1);
	    ++rootChangeCount;
	    if (changeCount > 1) {
		log.shout(changeCount + " changes to Root in a single execute");
	    }
	}
	
	
	long now = System.currentTimeMillis();
	long deltaT = (now - lastLog);
	if (deltaT > 10000) {
	    log.shout(rootChangeCount + " execute cycles with changes to base rootSlotFloat in "+ deltaT/1000f + " seconds");
	    log.shout(propertyChangeCount + " property changes in "+ deltaT/1000f + " seconds");
	    log.shout(blackboardChangeCount + " blackboard changes in "+ deltaT/1000f + " seconds");
	    lastLog = now;
	    rootChangeCount =0;
	    blackboardChangeCount =0;
	    propertyChangeCount = 0;
	}
	
	if (log.isDebugEnabled()) {
	    log.debug(rsub.getAddedCollection().size() + " Relations added");
	    log.debug(rsub.getRemovedCollection().size() + " Relations removed");
	    log.debug(rsub.getChangedCollection().size() + " Relations changed");
	    log.debug(added.size() + " Things added");
	    log.debug(tsub.getRemovedCollection().size() + " Things removed");
	    log.debug(changed.size() + " Things changed");
	}
	
    }

    protected void setupSubscriptions() {
	BlackboardService bbs = getBlackboardService();
	rsub = (IncrementalSubscription) bbs.subscribe(relationPredicate);
	tsub = (IncrementalSubscription) bbs.subscribe(thingPredicate);
	rootSub = (IncrementalSubscription) bbs.subscribe(rootPredicate);
    }
    
    public void frameSetAvailable(String name, FrameSet set) {
	frameset = set;
	getBlackboardService().signalClientActivity();
    }

    public void propertyChange(PropertyChangeEvent evt) {
	++propertyChangeCount;
    }

}
