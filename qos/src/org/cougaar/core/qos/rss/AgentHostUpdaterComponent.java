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

package org.cougaar.core.qos.rss;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.node.NodeControlService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.service.wp.AddressEntry;
import org.cougaar.core.service.wp.WhitePagesService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.qos.qrs.EventSubscriber;
import org.cougaar.qos.qrs.RSS;
import org.cougaar.qos.qrs.ResourceContext;


/**
 * This Component provides the {@link AgentHostUpdater} service.
 * Inner classes implement that service as well as the {@link
 * AgentTopologyService}.  The implementations uses a combination of
 * RSS events and a poller that gets data from the WhitePages.
 */
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


	NodeControlService ncs = (NodeControlService)
	    sb.getService(this, NodeControlService.class, null);
	if (ncs != null) {
	    ServiceBroker rootsb = ncs.getRootServiceBroker();
	    rootsb.addService(AgentHostUpdater.class, this);
	    rootsb.addService(AgentTopologyService.class, this);
	    sb.releaseService(this, NodeControlService.class, ncs);
	}

    }

    public Object getService(ServiceBroker sb, 
			     Object requestor, 
			     Class serviceClass) 
    {
	if (serviceClass == AgentHostUpdater.class) {
	    return updater;
	} else if (serviceClass == AgentTopologyService.class) {
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
	implements AgentHostUpdater, AgentTopologyService,
		   EventSubscriber, Runnable
    {
	private static final String TOPOLOGY = "topology";
	private HashMap listeners;
	private HashSet agents, nodes;
	private HashMap node_hosts, agent_nodes;
	private HashMap agent_hosts; // only used for callbacks
	private LoggingService loggingService;
	private WhitePagesService wpService;

	AgentHostUpdaterImpl(ServiceBroker sb) 
	{
	    this.listeners = new HashMap();
	    this.agents = new HashSet();
	    this.nodes = new HashSet();
	    this.agent_hosts = new HashMap(); // cache only
	    this.agent_nodes = new HashMap(); // authoritative
	    this.node_hosts = new HashMap();  // authoritative

	    loggingService = (LoggingService) 
		sb.getService(this, LoggingService.class, null);

	    wpService = (WhitePagesService)
		sb.getService(this, WhitePagesService.class, null);

	    RSS.instance().subscribeToEvent(this, RSS.CREATION_EVENT);
	}


	// RSS EventSubscriber interface
	public void rssEvent(ResourceContext context, int event_type) 
	{
	    if (context instanceof AgentDS) {
		String name = ((AgentDS) context).getAgentName();
		synchronized (this) {
		    agents.add(name);
		}
	    } else if (context instanceof NodeDS) {
		String name = ((NodeDS) context).getNodeName();
		synchronized (this) {
		    nodes.add(name);
		}
	    }
	}

	// AgentTopologyService interface
	public synchronized String getAgentHost(MessageAddress agent)
	{
	    String nodeName = getAgentNode(agent);
	    if (nodeName != null) {
		MessageAddress node = MessageAddress.getMessageAddress(nodeName);
		return getNodeHost(node);
	    } else {
		// No known node yet -  add it to the set
		return null;
	    }
	}

	public synchronized String getNodeHost(MessageAddress node)
	{
	    String nodeName = node.toString();
	    nodes.add(nodeName);
	    return (String) node_hosts.get(nodeName);
	}

	public synchronized String getAgentNode(MessageAddress agent)
	{
	    String agentName = agent.toString();
	    agents.add(agent.toString());
	    return (String) agent_nodes.get(agentName);
	}



	// AgentHostUpdater interface
	public synchronized void addListener(AgentHostUpdaterListener listener,
					     MessageAddress agent) 
	{
	    String agentName = agent.toString();
	    agents.add(agentName);
	    HashSet agentListeners = (HashSet) 
		listeners.get(agentName);
	    if (agentListeners == null) {
		agentListeners = new HashSet();
		listeners.put(agentName, agentListeners);
	    }
	    agentListeners.add(listener);

	    String host = getAgentHost(agent);
	    if (host != null) listener.newHost(host);
	}

	public synchronized void removeListener(AgentHostUpdaterListener listener,
						MessageAddress agent) 
	{
	    String agentName = agent.toString();
	    HashSet agentListeners = (HashSet) 
		listeners.get(agentName);
	    if (agentListeners != null) {
		agentListeners.remove(listener);
	    }
	}

	private void notifyListeners(String agent, String host)
	{
	    // We still need to update these sysconds, since
	    // they're not subscribed to an Agent formula (if
	    // they were, the rest of this would be
	    // unnecessary).  They're subscribed to a Host
	    // formula.
	    //
	    // Maybe this should only run when entityClass == AgentDS?
	    HashSet agentListeners = (HashSet) listeners.get(agent);
	    if (agentListeners == null) return;
	    Iterator litr = agentListeners.iterator();
	    while (litr.hasNext()) {
		AgentHostUpdaterListener listener =
		    (AgentHostUpdaterListener) litr.next();
		if (loggingService.isDebugEnabled()) 
		    loggingService.debug("New host " +host+
					 " for listener " +listener);
		listener.newHost(host);
	    }
	}




	private boolean checkAgentMoved(String agent, String node, URI uri)
	{
	    // Ensure that the Agent's node is on the nodes list (if
	    // it's a real node).
	    if (!node.equals(AgentDS.UNKNOWN_NODE)) {
		nodes.add(node);
	    }
		
	    String old_node = (String) agent_nodes.get(agent);

	    if (!node.equals(old_node)) {
		agent_nodes.put(agent, node);
		if (loggingService.isDebugEnabled())
		    loggingService.debug("New node " 
					 +node+
					 " for agent " 
					 +agent);
		return true;
	    } else {
		return false;
	    }
	}

	private boolean checkNodeMoved(String node, URI uri) 
	{
	    if (node.equals(AgentDS.UNKNOWN_NODE)) {
		if (loggingService.isErrorEnabled())
		    loggingService.error("checkHost called on foster node!");
	    }

	    String host = uri.getHost();
	    String old_host = (String) node_hosts.get(node);

	    if (old_host == null || !old_host.equals(host)) {
		node_hosts.put(node, host);
		if (loggingService.isDebugEnabled())
		    loggingService.debug("New host " 
					 +host+
					 " for node " 
					 +node);

		return true;
	    } else {
		return false;
	    }
	}


	private ArrayList agentWalk(HashMap node_wp) 
	{
	    // Loop over all Agents, seeing if the Node has changed,
	    ArrayList changed = new ArrayList();
	    Iterator itr = agents.iterator();
	    while (itr.hasNext()) {
		String agent = (String) itr.next();
		try {
		    AddressEntry entry = wpService.get(agent, TOPOLOGY, -1);
		    if (entry != null) {
			URI uri = entry.getURI();
			String node = uri.getPath().substring(1);
			if (node.equals(agent)) {
			    // This is a node-agent, add it
			    node_wp.put(node, entry);
			}
			if (checkAgentMoved(agent, node, uri))
			    changed.add(agent);
		    }
		} catch (Exception ex) {
		}
	    }
	    return changed;
	}


	private ArrayList agentNotificationWalk() 
	{
	    // Loop over all Agents, seeing if the Host has changed,
	    ArrayList change_list = new ArrayList();
	    Iterator itr = agents.iterator();
	    while (itr.hasNext()) {
		String agent = (String) itr.next();
		MessageAddress addr  = MessageAddress.getMessageAddress(agent);
		String current_host = getAgentHost(addr);
		String old_host = (String) agent_hosts.get(agent);
		if (current_host != null) {
		    if (old_host == null || !old_host.equals(current_host)) {
			agent_hosts.put(agent, current_host);
			change_list.add(agent);
		    }
		}
	    }
	    return change_list;
	}


	private ArrayList nodeWalk(HashMap node_wp) 
	{
	    // Loop over all Nodes, seeing if the Host has changed,
	    ArrayList changed = new ArrayList();
	    Iterator itr = nodes.iterator();
	    while (itr.hasNext()) {
		String node = (String) itr.next();
		AddressEntry entry = (AddressEntry)
		    node_wp.get(node);
		if (entry == null) {
		    try {
			entry = wpService.get(node, TOPOLOGY, -1);
		    } catch (Exception ex) {
		    }
		}
		if (entry != null) {
		    if (checkNodeMoved(node, entry.getURI()))
			changed.add(node);
		}
	    }
	    return changed;
	}

	private void updateRSS(ArrayList names, String type)
	{
	    RSS rss = RSS.instance();
	    String[] params = new String[1];
	    if (names != null) {
		int length = names.size();
		for (int i=0; i<length; i++) {
		    params[0] = (String) names.get(i);
		    rss.deleteContext(type, params);
		}
	    }
	}

	private void updateListeners(ArrayList change_list)
	{
	    int length = change_list.size();
	    for (int i=0; i<length; i++) {
		String agent = (String) change_list.get(i);
		String host = getAgentHost(MessageAddress.getMessageAddress(agent));
		notifyListeners(agent, host);
	    }
	}

	public void run() 
	{
	    ArrayList agent_host_change_list = null;
	    ArrayList agent_node_change_list = null;
	    ArrayList node_host_change_list = null;

	    // First, walk through the agents collecting a list of
	    // those whose node has changed.  Also collect a map of
	    // node names and the corresponding wp entry for each
	    // node agent.  The update list will be used to sync the
	    // RSS. The map will be used in step 2.
	    // 
	    // Next, walk through the nodes collecting a list of
	    // those whose host has changed.  Use the wp entries as
	    // collected in step 1.  The update list will be used to
	    // sync the RSS.
	    //
	    // Finally, walk through the agents again collecting
	    // a list of those whose host has changed.  The update
	    // list will be used to notify subscribers.
	    //
	    // By using a single wp entry per agent and synchronizing
	    // all three calls, the result should be a consistent
	    // topology that can be used by the AgentTopologyService.
	    synchronized (this) {
		HashMap node_wp = new HashMap(); // node -> wp-entry
		agent_node_change_list = agentWalk(node_wp);
		node_host_change_list = nodeWalk(node_wp);
		agent_host_change_list = agentNotificationWalk();
	    }

	    // Sync the RSS by reconstructing the resource contexts
	    // whose parent has changed.
	    updateRSS(agent_node_change_list, "Agent");
	    updateRSS(node_host_change_list, "Node");

	    // Inform the subcribers of agent-host changes.
	    updateListeners(agent_host_change_list);
	}
    }
}

