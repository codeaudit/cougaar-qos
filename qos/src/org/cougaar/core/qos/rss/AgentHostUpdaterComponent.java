/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.core.qos.rss;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.EventSubscriber;
import com.bbn.quo.data.RSS;

import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.service.wp.AddressEntry;
import org.cougaar.core.service.wp.Application;
import org.cougaar.core.service.wp.WhitePagesService;

public final class AgentHostUpdaterComponent
    extends QosComponent
    implements ServiceProvider
{
    private static final int PERIOD = 3000;

    private AgentHostUpdaterImpl updater;

    public void load() {
	super.load();
	provideService(getServiceBroker());
    }

    void provideService(ServiceBroker sb) {
	updater = new AgentHostUpdaterImpl(sb);

	ThreadService threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	threadService.schedule(updater, 0, PERIOD);

	sb.addService(AgentHostUpdater.class, this);

    }

    public Object getService(ServiceBroker sb, 
			     Object requestor, 
			     Class serviceClass) 
    {
	if (serviceClass == AgentHostUpdater.class) {
	    return updater;
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



    private static class AgentHostUpdaterImpl 
	extends TimerTask 
	implements AgentHostUpdater, EventSubscriber
    {
	private static final Application TOPOLOGY = 
	    Application.getApplication("topology");
	private static final String SCHEME = "node";
	private HashMap listeners;
	private HashSet agents;
	private HashMap agent_hosts;
	private HashMap node_hosts;
	private LoggingService loggingService;
	private WhitePagesService wpService;

	AgentHostUpdaterImpl(ServiceBroker sb) {
	    this.listeners = new HashMap();
	    this.agents = new HashSet();
	    this.agent_hosts = new HashMap();
	    this.node_hosts = new HashMap();

	    loggingService = (LoggingService) 
		sb.getService(this, LoggingService.class, null);

	    wpService = (WhitePagesService)
		sb.getService(this, WhitePagesService.class, null);

	    RSS.subscribeToEvent(this, RSS.CREATION_EVENT);
	}

	public void rssEvent(DataScope scope, int event_type) {
	    if (scope instanceof AgentDS) {
		String name = ((AgentDS) scope).getAgentName();
		synchronized (agents) {
		    agents.add(name);
		}
	    }
	}

	public void addListener(AgentHostUpdaterListener listener,
				MessageAddress agent) 
	{
	    String agentName = agent.toString();
	    synchronized (listeners) {
		ArrayList agentListeners = (ArrayList) 
		    listeners.get(agentName);
		if (agentListeners == null) {
		    agentListeners = new ArrayList();
		    listeners.put(agentName, agentListeners);
		}
		agentListeners.add(listener);
	    }
	    String host = (String) agent_hosts.get(agentName);
	    if (host != null) listener.newHost(host);
	}

	public void removeListener(AgentHostUpdaterListener listener,
				   MessageAddress agent) 
	{
	    String agentName = agent.toString();
	    synchronized (listeners) {
		ArrayList agentListeners = (ArrayList) 
		    listeners.get(agentName);
		if (agentListeners != null) {
		    agentListeners.remove(listener);
		}
	    }
	}

	public void run() {
	    // Loop over all Agents, seeing if the Host has changed,
	    synchronized (agents) {
		Iterator itr = agents.iterator();
		while (itr.hasNext()) {
		    String agent = (String) itr.next();
		    try {
			AddressEntry entry = wpService.get(agent, TOPOLOGY, 
							   SCHEME);
			if (entry != null) checkHost(agent,entry.getAddress());
		    } catch (Exception ex) {
		    }
		}
	    }
	}

	private void checkHost(String agent, URI uri) {
	    // See if the Node has moved
	    String node = uri.getPath().substring(1);
	    String host = uri.getHost();
	    String old_node_host = (String) node_hosts.get(node);
	    String old_agent_host = (String) agent_hosts.get(agent);
	    if (old_node_host == null || !old_node_host.equals(host)) {
		// node has moved. 
		Object[] params = { node };
		Class cls = NodeDS.class;
		RSS.instance().deleteScope(cls, params);
		node_hosts.put(node, host);
	    }


	    if (old_agent_host == null || !old_agent_host.equals(host)) {
		// Agent has moved to a new host.  Delete the
		// old DataScope.
		Object[] params = { agent };
		Class cls = AgentDS.class;
		RSS.instance().deleteScope(cls, params);
		agent_hosts.put(agent, host);
		if (loggingService.isDebugEnabled())
		    loggingService.debug("===== New host " 
					 +host+
					 " for agent " 
					 +agent);


		// We still need to update these sysconds, since
		// they're not subscribed to an Agent formula (if
		// they were, the rest of this would be
		// unnecessary).  They're subscribed to a Host
		// formula.
		synchronized (listeners) {
		    ArrayList agentListeners = 
			(ArrayList) listeners.get(agent);
		    if (agentListeners == null) return;
		    Iterator litr = agentListeners.iterator();
		    while (litr.hasNext()) {
			AgentHostUpdaterListener listener =
			    (AgentHostUpdaterListener) litr.next();
			if (loggingService.isDebugEnabled()) 
			    loggingService.debug("New host " +host+
						 " for " +listener);
			listener.newHost(host);
		    }
		}
	    }
	}
    }
}

