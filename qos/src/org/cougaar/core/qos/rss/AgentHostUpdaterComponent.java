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
import org.cougaar.core.service.wp.WhitePagesService;
import org.cougaar.core.thread.Schedulable;

public final class AgentHostUpdaterComponent
    extends QosComponent
    implements ServiceProvider
{
    private static final int PERIOD = 3000;

    private AgentHostUpdaterImpl updater;
    private Schedulable schedulable;

    public void load() {
	super.load();
	provideService(getServiceBroker());
    }

    void provideService(ServiceBroker sb) {
	updater = new AgentHostUpdaterImpl(sb);

	ThreadService threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	schedulable = threadService.getThread(this, updater, "AgentHostUpdater");
	schedulable.schedule(0, PERIOD);
	sb.releaseService(this, ThreadService.class, threadService);

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
	implements AgentHostUpdater, EventSubscriber, Runnable
    {
	private static final String TOPOLOGY = "topology";
	private HashMap listeners;
	private HashSet agents, nodes;
	private HashMap agent_hosts, node_hosts, agent_nodes;
	private LoggingService loggingService;
	private WhitePagesService wpService;

	AgentHostUpdaterImpl(ServiceBroker sb) {
	    this.listeners = new HashMap();
	    this.agents = new HashSet();
	    this.nodes = new HashSet();
	    this.agent_hosts = new HashMap();
	    this.agent_nodes = new HashMap();
	    this.node_hosts = new HashMap();

	    loggingService = (LoggingService) 
		sb.getService(this, LoggingService.class, null);

	    wpService = (WhitePagesService)
		sb.getService(this, WhitePagesService.class, null);

	    RSS.instance().subscribeToEvent(this, RSS.CREATION_EVENT);
	}

	public void rssEvent(DataScope scope, int event_type) {
	    if (scope instanceof AgentDS) {
		String name = ((AgentDS) scope).getAgentName();
		synchronized (agents) {
		    agents.add(name);
		}
	    } else if (scope instanceof NodeDS) {
		String name = ((NodeDS) scope).getNodeName();
		synchronized (nodes) {
		    nodes.add(name);
		}
	    }
	}

	public void addListener(AgentHostUpdaterListener listener,
				MessageAddress agent) 
	{
	    String agentName = agent.toString();
	    synchronized (agents) {
		agents.add(agentName);
	    }
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

	private void checkHost(String entity, URI uri, HashMap hosts, Class entityClass) {
	    String node = uri.getPath().substring(1);
	    boolean node_changed = false;

	    if (entityClass == AgentDS.class) {
		// Ensure that the Agent's node is on the nodes list (if it's a real node).
		if (!node.equals(AgentDS.UNKNOWN_NODE)) {
		    synchronized (nodes) {
			nodes.add(node);
		    }
		}
		
		String old_node = (String) agent_nodes.get(entity);
		node_changed = !node.equals(old_node);
		agent_nodes.put(entity, node);
	    }

	    if (entity.equals(AgentDS.UNKNOWN_NODE)) {
		if (loggingService.isErrorEnabled())
		    loggingService.error("checkHost called on foster node!");
	    }

	    String host = uri.getHost();
	    String old_host = (String) hosts.get(entity);

	    if (old_host == null || !old_host.equals(host)) {

		// Entity has moved to a new host.  Delete the
		// old DataScope.
		Object[] params = { entity };
		RSS.instance().deleteScope(entityClass, params);

		hosts.put(entity, host);
		if (loggingService.isDebugEnabled())
		    loggingService.debug("New host " 
					 +host+
					 " for entity " 
					 +entity);


		// We still need to update these sysconds, since
		// they're not subscribed to an Agent formula (if
		// they were, the rest of this would be
		// unnecessary).  They're subscribed to a Host
		// formula.
		//
		// Maybe this should only run when entityClass == AgentDS?
		synchronized (listeners) {
		    ArrayList entityListeners = 
			(ArrayList) listeners.get(entity);
		    if (entityListeners == null) return;
		    Iterator litr = entityListeners.iterator();
		    while (litr.hasNext()) {
			AgentHostUpdaterListener listener =
			    (AgentHostUpdaterListener) litr.next();
			if (loggingService.isDebugEnabled()) 
			    loggingService.debug("New host " +host+
						 " for listener " +listener);
			listener.newHost(host);
		    }
		}
	    } else if (node_changed) {
		// Handle the case in which the agent's node has changed
		// _without_ the host changing.
		Object[] params = { entity };
		RSS.instance().deleteScope(entityClass, params);

		if (loggingService.isDebugEnabled())
		    loggingService.debug("New node " 
					 +node+
					 " for agent " 
					 +entity);
	    }
	}


	
	private void checkEntities(HashSet entities, HashMap hosts, Class entityClass) {
	    // Asking the WhitePages for data can block, so we'd like
	    // not to do it in a synchronized block.  Unfortunately
	    // access to the Set has to be synchronized.  The only
	    // alternative seems to be to copy the set.
	    
	    Set copy = new HashSet();
	    synchronized (entities) {
		copy.addAll(entities);
	    }

	    // Loop over all Agents, seeing if the Host has changed,
	    Iterator itr = copy.iterator();
	    while (itr.hasNext()) {
		String entity_name = (String) itr.next();
		try {
		    AddressEntry entry = wpService.get(entity_name, TOPOLOGY, 10);
		    if (entry != null) checkHost(entity_name, entry.getURI(), hosts, entityClass);
		} catch (Exception ex) {
		}
	    }
	}

	public void run() {
	    checkEntities(nodes, node_hosts, NodeDS.class);
	    checkEntities(agents, agent_hosts, AgentDS.class);
	}


    }
}

