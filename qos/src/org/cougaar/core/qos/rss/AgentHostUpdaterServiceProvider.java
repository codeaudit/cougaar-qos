/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import com.bbn.quo.data.RSS;

import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.TopologyReaderService;
import org.cougaar.core.service.TopologyEntry;
import org.cougaar.core.service.ThreadService;

class AgentHostUpdaterServiceProvider implements ServiceProvider
{
    private static final int PERIOD = 3000;

    private AgentHostUpdaterImpl updater;

    AgentHostUpdaterServiceProvider(ServiceBroker sb) {
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
	implements AgentHostUpdater
    {
	private HashMap listeners;
	private HashMap agent_hosts;
	private HashMap node_hosts;
	private LoggingService loggingService;
	private TopologyReaderService topologyService;

	AgentHostUpdaterImpl(ServiceBroker sb) {
	    this.listeners = new HashMap();
	    this.agent_hosts = new HashMap();
	    this.node_hosts = new HashMap();

	    loggingService = (LoggingService) 
		sb.getService(this, LoggingService.class, null);

	    topologyService = (TopologyReaderService) 
		sb.getService(this, TopologyReaderService.class, null);
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
	    Set matches = null;
	    try {
		matches = topologyService.getAllEntries(null, null, null, null,
							null);
	    } catch (Exception ex) {
		// Node hasn't finished initializing yet
		return;
	    }
	    if (matches == null) return;
	    Iterator itr = matches.iterator();
	    while (itr.hasNext()) {
		TopologyEntry entry = (TopologyEntry) itr.next();

		// See if the Node has moved
		String nodeName = entry.getNode();
		String old_node_host = (String) node_hosts.get(nodeName);
		String new_node_host = entry.getHost();
		if (old_node_host == null || !old_node_host.equals(new_node_host)) {
		    // node has moved. 
		    Object[] params = { nodeName };
		    Class cls = NodeDS.class;
		    RSS.instance().deleteScope(cls, params);
		    // 		    System.err.println("===== Deleted Node " +node+
		    // 				       " scope");
		    node_hosts.put(nodeName, new_node_host);
		}

		String agentName = entry.getAgent();
		//  		System.out.print("Agent " +agent);
		// Should we restrict this to ACTIVE Agents?
		if (entry.getStatus() != TopologyEntry.ACTIVE) continue;
		String new_host = entry.getHost();
		String host = (String) agent_hosts.get(agentName);

		//  		System.out.println(" Old host: " +host+
		//  				   " New host: " +new_host);

		if (host == null || !host.equals(new_host)) {
		    // Agent has moved to a new host.  Delete the
		    // old DataScope.
		    Object[] params = { agentName };
		    Class cls = AgentDS.class;
		    RSS.instance().deleteScope(cls, params);
		    // 		    System.err.println("===== Deleted Agent " +agent+
		    // 				       " scope");
		    agent_hosts.put(agentName, new_host);
		    if (loggingService.isDebugEnabled())
			loggingService.debug("===== New host " 
					     +new_host+
					     " for agent " 
					     +agentName);


		    // We still need to update these sysconds, since
		    // they're not subscribed to an Agent formula (if
		    // they were, the rest of this would be
		    // unnecessary).  They're subscribed to a Host
		    // formula.
		    synchronized (listeners) {
			ArrayList agentListeners = 
			    (ArrayList) listeners.get(agentName);
			if (agentListeners == null) continue;
			Iterator litr = agentListeners.iterator();
			while (litr.hasNext()) {
			    AgentHostUpdaterListener listener =
				(AgentHostUpdaterListener) litr.next();
			    if (loggingService.isDebugEnabled()) 
				loggingService.debug("New host " +new_host+
						     " for " +listener);
			    listener.newHost(new_host);
			}
		    }
		}
	    }
	}
    }    
}

