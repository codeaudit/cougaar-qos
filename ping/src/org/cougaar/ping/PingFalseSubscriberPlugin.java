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
 **/

package org.cougaar.ping;

import java.util.Enumeration;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;

/**
 * Plugin Subscriptions are always false
 * The execute method should never be called
 * The Plugin is used for testing the cost of subscriptions
 */
public class PingFalseSubscriberPlugin
    extends ComponentPlugin 
{
    private LoggingService log;
    private IncrementalSubscription pingSub;


    private static UnaryPredicate createPingPredicate() {
	return new UnaryPredicate() {
		public boolean execute(Object o) {
		    return false;
		}
	    };
    }

    public void start() {
	super.start();

	// get the logging service
	log = (LoggingService)
	    getServiceBroker().getService(this, LoggingService.class, null);
	if (log == null)  log = LoggingService.NULL;

    }


    public void unload() {
	if ((log != null) && (log != LoggingService.NULL)) {
	    getServiceBroker().releaseService(this,
					      LoggingService.class, 
					      log);
	    log = LoggingService.NULL;
	}
	super.unload();
    }

    protected void setupSubscriptions() {
	// subscribe to pings objects
	UnaryPredicate pingPred = createPingPredicate();
	pingSub = (IncrementalSubscription) 
	    blackboard.subscribe(pingPred);
    }


    protected void execute() {

	if (pingSub.hasChanged()) {
	    // Ping added
	    for (Enumeration en = pingSub.getAddedList();
		 en.hasMoreElements();
		 ) {
		Ping p = (Ping) en.nextElement();
		if (log.isInfoEnabled()) {
		    log.info("observed ping ADD: \n"+p);
		}
	    }
	    // Ping Changed
	    for (Enumeration en = pingSub.getChangedList();
		 en.hasMoreElements();
		 ) {
		Ping p = (Ping) en.nextElement();
		if (log.isInfoEnabled()) {
		    log.info("observed ping CHANGE: \n"+p);
		}
	    }
	}
    }
}

