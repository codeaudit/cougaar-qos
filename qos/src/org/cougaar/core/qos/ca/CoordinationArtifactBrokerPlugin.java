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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;

/**
 * This plugin provides the CoordinationArtifactBroker service, which is
 * implemented by an inner class.
 */
public class CoordinationArtifactBrokerPlugin
    extends ParameterizedPlugin
    implements ServiceProvider
{
    private CoordinationArtifactBroker impl;
    private LoggingService log;

    public void load()
    {
	super.load();

	ServiceBroker sb = getServiceBroker();
	impl = new Impl(sb);
	sb.addService(CoordinationArtifactBroker.class, this);

	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
    }


    // plugin
    protected void execute()
    {
    }

    protected void setupSubscriptions() 
    {
    }



     // ServiceProvider Interface
    public Object getService(ServiceBroker sb, 
			     Object requestor, 
			     Class serviceClass) 
    {
	if (serviceClass == CoordinationArtifactBroker.class) {
	    return impl;
	} else {
	    return null;
	}
    }

    public void releaseService(ServiceBroker sb, 
			       Object requestor, 
			       Class serviceClass, 
			       Object service)
    {
    }

	
    private class Impl implements CoordinationArtifactBroker
    {
	ThreadService tsvc;
	HashMap pendingRequests;
	ArrayList templates;
	Schedulable requestsThread;
	ServiceBroker sb;

	Impl(ServiceBroker sb)
	{
	    tsvc = (ThreadService)
		sb.getService(this, ThreadService.class, null);
	    pendingRequests = new HashMap();
	    templates = new ArrayList();
	    Runnable runner = new Runnable() {
		    public void run() {
			checkPendingRequests();
		    }
		};
	    requestsThread = tsvc.getThread(this, runner, "ArtifactBroker");
	    this.sb = sb;
	}

	public void requestFacet(ConnectionSpec spec,
				 RolePlayer rolePlayer)
	{
	    if (!findFacet(spec, rolePlayer)) {
		// assume one queued request per player
		synchronized (pendingRequests) {
		    if (log.isDebugEnabled())
			log.debug("Pending request for "
				  +spec.ca_kind+ " "
				  +spec.role);
		    pendingRequests.put(rolePlayer, spec);
		    requestsThread.start();
		}
	    }
	}



	public void registerCoordinationArtifactTemplate(CoordinationArtifactTemplate cat)
	{
	    synchronized (templates) {
		templates.add(cat);
	    }
	    if (log.isDebugEnabled())
		log.debug("Registered artifact for " + cat.getArtifactKind());
	    requestsThread.start();
	}


	// The body of the Schedulable
	private void checkPendingRequests() 
	{
	    synchronized (pendingRequests) {
		Iterator itr = pendingRequests.entrySet().iterator();
		while (itr.hasNext()) {
		    Map.Entry entry = (Map.Entry) itr.next();
		    RolePlayer player = (RolePlayer) entry.getKey();
		    ConnectionSpec spec = (ConnectionSpec) entry.getValue();
		    if (findFacet(spec, player)) itr.remove();
		}
	    }
	}

	private boolean findFacet(ConnectionSpec spec, RolePlayer player) 
	{
	    if (log.isDebugEnabled())
		log.debug("Looking for " +spec.ca_kind+ " " +spec.role);
	    synchronized (templates) {
		for (int i=0; i<templates.size(); i++) {
		    CoordinationArtifactTemplate cat = (CoordinationArtifactTemplate) 
			templates.get(i);
		    
		    if (cat.supports(spec)) {
			if (log.isDebugEnabled())
			    log.debug("Found " +spec.ca_kind+ " " 
				      +spec.role);
			cat.provideFacet(spec, player);
			return true;
			
		    }
		}
	    }
	    if (log.isDebugEnabled())
		log.debug("Didn't find " +spec.ca_kind+ " " +spec.role);
	    return false;
	}


    }

}

