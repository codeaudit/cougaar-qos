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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import com.bbn.quo.data.RSS;

import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.service.wp.Application;
import org.cougaar.core.service.wp.AddressEntry;
import org.cougaar.core.service.wp.WhitePagesService;
import org.cougaar.core.wp.ListAllAgents;

public final class AgentHostUpdaterComponent
    extends QosComponent
    implements ServiceProvider
{
    private static final Application TOPOLOGY = 
	Application.getApplication("topology");
    private static final String SCHEME = "node";
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
	implements AgentHostUpdater
    {
	private HashMap listeners;
	private HashMap agent_hosts;
	private HashMap node_hosts;
	private LoggingService loggingService;
	private WhitePagesService wps;

	AgentHostUpdaterImpl(ServiceBroker sb) {
	    this.listeners = new HashMap();
	    this.agent_hosts = new HashMap();
	    this.node_hosts = new HashMap();

	    loggingService = (LoggingService) 
		sb.getService(this, LoggingService.class, null);

	    wps = (WhitePagesService) 
		sb.getService(this, WhitePagesService.class, null);
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
	    Set allAgents = null;
	    try {
		allAgents = ListAllAgents.listAllAgents(wps); // not scalable!
	    } catch (Exception ex) {
		// Node hasn't finished initializing yet
		return;
	    }
	    if (allAgents == null) return;
	    for (Iterator itr = allAgents.iterator(); itr.hasNext(); ) {
		String agentName = (String) itr.next();

                // Lookup the agent's host & node
                String nodeName = null;
		String new_host = null;
                try {
                    AddressEntry entry = wps.get(agentName, TOPOLOGY, SCHEME);
                    if (entry != null) {
                        new_host = entry.getAddress().getHost();
                        nodeName = entry.getAddress().getPath().substring(1);
                    }
                } catch (Exception ex) {
		    if (loggingService.isDebugEnabled())
                        loggingService.debug("Failed lookup("+agentName+")", ex);
                }
                if (nodeName == null || new_host == null) {
                    // Ignore and continue to the next agent?
                    continue;
                }
		String new_node_host = new_host;

		// See if the Node has moved
		String old_node_host = (String) node_hosts.get(nodeName);
		if (old_node_host == null || !old_node_host.equals(new_node_host)) {
		    // node has moved. 
		    Object[] params = { nodeName };
		    Class cls = NodeDS.class;
		    RSS.instance().deleteScope(cls, params);
		    // 		    System.err.println("===== Deleted Node " +node+
		    // 				       " scope");
		    node_hosts.put(nodeName, new_node_host);
		}

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

