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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.EventSubscriber;
import com.bbn.quo.data.RSS;

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

	public String getAgentHost(MessageAddress agent)
	{
	    String agentName = agent.toString();
	    String host = null;
	    synchronized (agent_hosts) {
		host = (String) agent_hosts.get(agentName);
	    }
	    if (host != null) return host;
	    synchronized (agents) {
		agents.add(agentName);
	    }
	    return null; // caller should try again later
	}

	public String getNodeHost(MessageAddress node)
	{
	    String nodeName = node.toString();
	    String host = null;
	    synchronized (node_hosts) {
		host = (String) node_hosts.get(nodeName);
	    }
	    if (host != null) return host;
	    synchronized (nodes) {
		nodes.add(nodeName);
	    }
	    return null; // caller should try again later
	}

	public String getAgentNode(MessageAddress agent)
	{
	    String agentName = agent.toString();
	    String node = null;
	    synchronized (agent_hosts) {
		node = (String) agent_nodes.get(agentName);
	    }
	    if (node != null) return node;
	    synchronized (agents) {
		agents.add(agentName);
	    }
	    return null; // caller should try again later
	}

	public void addListener(AgentHostUpdaterListener listener,
				MessageAddress agent) 
	{
	    String agentName = agent.toString();
	    synchronized (agents) {
		agents.add(agentName);
	    }
	    synchronized (listeners) {
		HashSet agentListeners = (HashSet) 
		    listeners.get(agentName);
		if (agentListeners == null) {
		    agentListeners = new HashSet();
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
		HashSet agentListeners = (HashSet) 
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
		    HashSet entityListeners = (HashSet) listeners.get(entity);
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

