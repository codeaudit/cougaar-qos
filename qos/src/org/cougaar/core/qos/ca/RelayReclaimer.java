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

package org.cougaar.core.qos.ca;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;

class RelayReclaimer
{
    private static final long CLEANUP_TIMEOUT = 300000; // 5 min
    private HashMap relays = new HashMap();
    private LoggingService log;

    RelayReclaimer(ServiceBroker sb)
    {
	log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
    }


    // should only be called from add()
    private void reclaim(BlackboardService blackboard) 
    {
	Iterator itr = relays.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    Object relay = entry.getKey();
	    if (true) {
		blackboard.publishRemove(relay);
		itr.remove();
		if (log.isDebugEnabled())
		    log.debug("Removing Relay: " + relay);
	    }
	}
    }

    void add(Object relay, BlackboardService blackboard)
    {
	synchronized (relays) {
	    if (log.isDebugEnabled())
		log.debug("Adding QueryRelay: " + relay);
	    reclaim(blackboard);
	    long expiration = System.currentTimeMillis() + CLEANUP_TIMEOUT;
	    relays.put(relay, new Long(expiration));
	}
    }
}
