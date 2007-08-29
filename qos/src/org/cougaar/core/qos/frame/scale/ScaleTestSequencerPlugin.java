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

import java.util.Enumeration;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.FrameSetService;
import org.cougaar.core.plugin.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;

public class ScaleTestSequencerPlugin extends ParameterizedPlugin implements FrameSetService.Callback {
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
