/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.lib.mquo;

import com.bbn.quo.rmi.ExpectedMaxJipsSC;
import com.bbn.quo.rmi.ExpectedBandwidthSC;
import com.bbn.quo.rmi.ExpectedCapacitySC;
import com.bbn.quo.rmi.ExpectedAvailableJipsSC;
import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.SysCond;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.Debug;
import org.cougaar.core.mts.DebugFlags;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.rss.MetricSC;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.service.TopologyEntry;
import org.cougaar.core.service.TopologyReaderService;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Set;


public final class SyscondFactory
    implements DebugFlags
{
    private static final int PERIOD = 30000;
    private static String local_host;


    static {
	try {
	    local_host = java.net.InetAddress.getLocalHost().getHostAddress();
	} catch (java.net.UnknownHostException ex) {
	    local_host = "127.0.0.1";
	}
    }

    private static SyscondFactory factory;

    static SyscondFactory getFactory() {
	return factory;
    }

    private QuoKernel kernel;
    private AgentHostUpdater updater;
    private HashMap jipsSysconds = new HashMap();
    private HashMap effectiveJipsSysconds = new HashMap();
    private HashMap bandwidthSysconds = new HashMap();
    private HashMap capacitySysconds = new HashMap();
    private LoggingService loggingService;
    private TopologyReaderService topologyService;
    private MetricsService metricsService;


    abstract private class AgentHostUpdaterListener 
    {
	protected MetricSC syscond;
	private MessageAddress agentAddress;

	AgentHostUpdaterListener(MetricSC syscond, MessageAddress address)
	{
	    this.syscond = syscond;
	    this.agentAddress = address;
	    updater.addListener(this, address);
	}

	abstract protected String path(String host);

	public void newHost(String host) {
	    try { 
		syscond.newPath(path(host));
	    } catch (java.rmi.RemoteException ex) {
	    }
	}

    }

    private class JipsSyscondListener extends AgentHostUpdaterListener {
	JipsSyscondListener(MetricSC syscond, 
			    MessageAddress address) 
	{
	    super(syscond, address);
	}

	public String path (String host) {
	    return "Host(" +host+ "):Jips";
	}
    }


    private class EffectiveJipsSyscondListener 
	extends AgentHostUpdaterListener 
    {
	EffectiveJipsSyscondListener(MetricSC syscond, 
				     MessageAddress address) 
	{
	    super(syscond, address);
	}

	public String path (String host) {
	    return "Host(" +host+ "):EffectiveMJips";
	}
    }


    private class BandwidthSyscondListener extends AgentHostUpdaterListener
    {
	BandwidthSyscondListener(MetricSC syscond,
				 MessageAddress address) {
	    super(syscond, address);
	}

	public String path (String host) {
	    return "IpFlow(" +local_host+
		"," +host+
		"):CapacityUnused";
	}
    }


    private class CapacitySyscondListener extends AgentHostUpdaterListener {
	CapacitySyscondListener(MetricSC syscond, 
				MessageAddress address) 
	{
	    super(syscond, address);
	}

	public String path (String host) {
	    return "IpFlow(" +local_host+
		"," +host+
		"):CapacityMax";
	}
    }




    private class AgentHostUpdater 
	implements Runnable
    {
	private HashMap listeners;
	private HashMap hosts;

	AgentHostUpdater() {
	    this.listeners = new HashMap();
	    this.hosts = new HashMap();
	}

	public synchronized void addListener(AgentHostUpdaterListener listener,
					     MessageAddress agent) 
	{
	    ArrayList agentListeners = (ArrayList) listeners.get(agent);
	    if (agentListeners == null) {
		agentListeners = new ArrayList();
		listeners.put(agent.toString(), agentListeners);
	    }
	    agentListeners.add(listener);
	    String host = (String) hosts.get(agent.toString());
	    if (host != null) listener.newHost(host);
	}

	public synchronized void 
	    removeListener(AgentHostUpdaterListener listener,
			   MessageAddress agent) 
	{
	    ArrayList agentListeners = (ArrayList) listeners.get(agent);
	    if (agentListeners != null) {
		agentListeners.remove(listener);
	    }
	}

	public void run() {
	    // Loop over all Agents, seeing if the Host has changed,
	    Set matches = topologyService.getAllEntries(null, null, null, null,
							null);
	    Iterator itr = matches.iterator();
	    while (itr.hasNext()) {
		TopologyEntry entry = (TopologyEntry) itr.next();
		String agent = entry.getAgent();
//  		System.out.print("Agent " +agent);
		// Should we restrict this to ACTIVE Agents?
		if (entry.getStatus() != TopologyEntry.ACTIVE) continue;
		String new_host = entry.getHost();
		String host = (String) hosts.get(agent);

//  		System.out.println(" Old host: " +host+
//  				   " New host: " +new_host);

		if (host == null || !host.equals(new_host)) {
		    hosts.put(agent, new_host);
		    if (Debug.isDebugEnabled(loggingService,RMS))
			loggingService.debug("===== New host " 
					     +new_host+
					     " for agent " 
					     +agent);
		    ArrayList agentListeners = 
			(ArrayList) listeners.get(agent);
		    if (agentListeners == null) continue;
		    
		    Iterator litr = agentListeners.iterator();
		    while (litr.hasNext()) {
			AgentHostUpdaterListener listener =
			    (AgentHostUpdaterListener) litr.next();
			listener.newHost(new_host);
		    }
		}
	    }
	}
    }



	    

    public SyscondFactory(ServiceBroker sb) {

	kernel = Utils.getKernel();

	metricsService = (MetricsService)
	    sb.getService(this, MetricsService.class, null);

	ThreadService threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	updater = new AgentHostUpdater();
	TimerTask task = threadService.getTimerTask(this, updater, 
						    "AgentHostUpdater");
	threadService.schedule(task, 0, PERIOD);

	loggingService = (LoggingService) 
	    sb.getService(this, LoggingService.class, null);

	topologyService = (TopologyReaderService) 
	    sb.getService(this, TopologyReaderService.class, null);


	factory = this;
    }


    private MetricSC makeMetricSC(String name) 
    {
	try {
	    MetricSC syscond = 
		(MetricSC)
		kernel.bindSysCond(name,
				   "org.cougaar.core.qos.rss.MetricSC",
				   "org.cougaar.core.qos.rss.MetricSCImpl");
	    
	    syscond.init(metricsService);
	    return syscond;
	} catch (java.rmi.RemoteException ex) {
	    loggingService.error(null, ex);
	    return null;
	}
    }

    public MetricSC getExpectedMaxMJipsForAgentSyscond(MessageAddress addr)
    {
	MetricSC syscond =  (MetricSC) effectiveJipsSysconds.get(addr);
	if (syscond == null) {
	    String name = addr+"MaxMJips";
	    syscond = makeMetricSC(name);
	    JipsSyscondListener syscondListener = 
		new JipsSyscondListener(syscond, addr);
	    effectiveJipsSysconds.put(addr, syscond);
	}
	return syscond;
    }


    public MetricSC getExpectedEffectiveMJipsForAgentSyscond(MessageAddress addr)
    {
	MetricSC syscond =  (MetricSC)jipsSysconds.get(addr);
	if (syscond == null) {
	    String name = addr+"EffectiveMJips";
	    syscond = makeMetricSC(name);
	    EffectiveJipsSyscondListener syscondListener = 
		new EffectiveJipsSyscondListener(syscond, addr);
	    jipsSysconds.put(addr, syscond);
	}
	return syscond;
    }

    public MetricSC getExpectedEffectiveMJipsForHostSyscond(String host)
    {
	String name = host+"EffectiveMJips";
	MetricSC syscond = makeMetricSC(name);
	try {
	    syscond.newPath("Host(" +host+ "):EffectiveMJips");
	} catch (java.rmi.RemoteException ex) {
	}
	return syscond;
    }



    public MetricSC getExpectedBandwidthForAgentSyscond(MessageAddress addr)
    {
	MetricSC syscond = (MetricSC) bandwidthSysconds.get(addr);
	if (syscond == null) {
	    String name = "Bandwidth " +local_host+ " to " +addr;
	    syscond = makeMetricSC(name);
	    BandwidthSyscondListener syscondListener = 
		new BandwidthSyscondListener(syscond, addr);
	    bandwidthSysconds.put(addr, syscond);
	}
	return syscond;
    }

    public MetricSC getExpectedBandwidthForHostSyscond(String host)
    {
	String name = "Bandwidth " +local_host+ " to " +host;
	MetricSC syscond = makeMetricSC(name);
	try {
	    syscond.newPath("IpFlow(" +local_host+
			    "," +host+
			    "):CapacityUnused");
	} catch (java.rmi.RemoteException ex) {
	}
	return syscond;
    }


    public MetricSC getExpectedCapacityForAgentSyscond(MessageAddress addr)
    {
	MetricSC syscond = (MetricSC) capacitySysconds.get(addr);
	if (syscond == null) {
	    String name = "Max Bandwidth " +local_host+ " to " +addr;
	    syscond = makeMetricSC(name);
	    CapacitySyscondListener syscondListener = 
		new CapacitySyscondListener(syscond, addr);
	    capacitySysconds.put(addr, syscond);
	}
	return syscond;
    }


}
