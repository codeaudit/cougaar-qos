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
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;

/**
 * This plugin provides the FacetBroker service, which is
 * implemented by an inner class.
 */
public class FacetBrokerPlugin
    extends ComponentPlugin
    implements ServiceProvider
{
    private FacetBroker impl;

    public void start()
    {
	super.start();

	ServiceBroker sb = getServiceBroker();
	impl = new Impl(sb);
	sb.addService(FacetBroker.class, this); // should be root-level
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
	if (serviceClass == FacetBroker.class) {
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

	
    private static class Impl implements FacetBroker
    {
	ThreadService tsvc;
	HashMap pendingRequests;
	HashMap facetProviders;
	Schedulable requestsThread;
	ServiceBroker sb;

	Impl(ServiceBroker sb)
	{
	    tsvc = (ThreadService)
		sb.getService(this, ThreadService.class, null);
	    pendingRequests = new HashMap();
	    facetProviders = new HashMap();
	    Runnable runner = new Runnable() {
		    public void run() {
			checkPendingRequests();
		    }
		};
	    requestsThread = tsvc.getThread(this, runner, "FacetBroker");
	    this.sb = sb;
	}

	public void requestFacet(ConnectionSpec spec,
				 RolePlayer rolePlayer)
	{
	    if (!findFacet(spec, rolePlayer)) {
		// assume one queued request per player
		synchronized (pendingRequests) {
		    pendingRequests.put(rolePlayer, spec);
		    requestsThread.start();
		}
	    }
	}



	public void registerFacetProvider(String kind, 
					  FacetProvider provider)
	{
	    synchronized (facetProviders) {
		List providers = (List) facetProviders.get(kind);
		if (providers == null) {
		    providers = new ArrayList();
		    facetProviders.put(kind, providers);
		}
		providers.add(provider);
	    }
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
	    synchronized (facetProviders) {
		List providers = (List) facetProviders.get(spec.kind);
		if (providers != null) {
		    for (int i=0; i<providers.size(); i++) {
			FacetProvider prvdr = (FacetProvider) providers.get(i);
			if (prvdr.matches(spec)) {
			    prvdr.provideFacet(spec, player);
			    return true;
			}
		    }
		}
	    }
	    return false;
	}


    }

}

