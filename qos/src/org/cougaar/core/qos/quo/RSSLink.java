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
package org.cougaar.core.qos.quo;

import com.bbn.quo.rmi.ExpectedMaxJipsSC;
import com.bbn.quo.rmi.ExpectedBandwidthSC;
import com.bbn.quo.rmi.ExpectedCapacitySC;
import com.bbn.quo.rmi.ExpectedAvailableJipsSC;
import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.SysCond;
import com.bbn.quo.data.BoundDataFormula;
import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.HostDS;
import com.bbn.quo.data.RSS;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.NameSupport;
import org.cougaar.core.mts.DebugService;
import org.cougaar.core.mts.DebugFlags;
import org.cougaar.core.qos.monitor.ResourceMonitorServiceImpl;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Observable;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;


public class RSSLink extends ResourceMonitorServiceImpl implements DebugFlags
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
    private Timer timer = new Timer(true);
    private RSS rss;
    private QuoKernel kernel;
    private AgentHostUpdater updater;
    private HashMap jipsSysconds = new HashMap();
    private HashMap effectiveJipsSysconds = new HashMap();
    private HashMap bandwidthSysconds = new HashMap();
    private HashMap capacitySysconds = new HashMap();
    private DebugService debugService;

    abstract private class AgentHostUpdaterListener 
    {
	protected SysCond syscond;
	private MessageAddress agentAddress;

	AgentHostUpdaterListener(SysCond syscond, MessageAddress address)
	{
	    this.syscond = syscond;
	    this.agentAddress = address;
	    updater.addListener(this, address);
	}

	abstract protected void updateHost(String host)
	    throws java.rmi.RemoteException;

	public void newHost(String host) {
	    try {
		updateHost(host);
	    } catch (java.rmi.RemoteException remote_ex) {
		debugService.error(null, remote_ex);
	    }
	}

    }

    private class JipsSyscondListener extends AgentHostUpdaterListener {
	JipsSyscondListener(ExpectedMaxJipsSC syscond, 
			    MessageAddress address) 
	{
	    super(syscond, address);
	}

	public void updateHost (String host) 
	    throws java.rmi.RemoteException
	{
	    ((ExpectedMaxJipsSC) syscond).setHost(host);
	}
    }


    private class EffectiveJipsSyscondListener 
	extends AgentHostUpdaterListener 
    {
	EffectiveJipsSyscondListener(ExpectedAvailableJipsSC syscond, 
				     MessageAddress address) 
	{
	    super(syscond, address);
	}

	public void updateHost (String host) 
	    throws java.rmi.RemoteException
	{
	    ((ExpectedAvailableJipsSC) syscond).setHost(host);
	}
    }


    private class BandwidthSyscondListener extends AgentHostUpdaterListener
    {
	BandwidthSyscondListener(ExpectedBandwidthSC syscond,
				 MessageAddress address) {
	    super(syscond, address);
	}

	public void updateHost (String host) 
	    throws java.rmi.RemoteException
	{
	    ((ExpectedBandwidthSC) syscond).setHosts(local_host, host);
	}
    }


    private class CapacitySyscondListener extends AgentHostUpdaterListener {
	CapacitySyscondListener(ExpectedCapacitySC syscond, 
				MessageAddress address) 
	{
	    super(syscond, address);
	}

	public void updateHost (String host) 
	    throws java.rmi.RemoteException
	{
	    ((ExpectedCapacitySC) syscond).setHosts(local_host, host);
	}
    }




    private class AgentHostUpdater 
	extends TimerTask 
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
		listeners.put(agent, agentListeners);
	    }
	    agentListeners.add(listener);
	    String host = (String) hosts.get(agent);
	    if (host != null) listener.newHost(host);
	}

	public synchronized void removeListener(AgentHostUpdaterListener listener,
						MessageAddress agent) 
	{
	    ArrayList agentListeners = (ArrayList) listeners.get(agent);
	    if (agentListeners != null) {
		agentListeners.remove(listener);
	    }
	}

	public void run() {
	    Attributes match = 
		new BasicAttributes(NameSupport.STATUS_ATTR, 
				    NameSupport.REGISTERED_STATUS);
	    String[] attr =  { NameSupport.AGENT_ATTR, NameSupport.HOST_ATTR };
	    Iterator itr = nameSupport.lookupInTopology(match, attr);
	    while (itr.hasNext()) {
		Attributes pair = (Attributes) itr.next();
		if (pair != null) {
		    Attribute new_host_attr = pair.get(NameSupport.HOST_ATTR);
		    Attribute agent_attr = pair.get(NameSupport.AGENT_ATTR);
		    if (new_host_attr == null || agent_attr == null) continue;
		    String new_host = null;
		    MessageAddress agent = null;

		    try {
			new_host = (String) new_host_attr.get();
			agent = (MessageAddress) agent_attr.get();
		    } catch (javax.naming.NamingException name_ex) {
			debugService.error(null, name_ex);
			continue;
		    }

		    String host = (String) hosts.get(agent);

		    if (host == null || !host.equals(new_host)) {
			hosts.put(agent, new_host);
			if (debugService.isDebugEnabled(RMS))
			    debugService.debug("===== New host " 
						      +new_host+
						      " for agent " 
						      +agent);
			ArrayList agentListeners = (ArrayList) listeners.get(agent);
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

    }

	    
    public RSSLink(NameSupport nameSupport, ServiceBroker sb) {
	super(nameSupport, sb);
	Properties props = new Properties();
	Utils.readRSSProperties(props);
	rss = RSS.makeInstance(props);
	kernel = Utils.getKernel(props);
	updater = new AgentHostUpdater();
	timer.schedule(updater, 0, PERIOD);
	debugService =
	    (DebugService) sb.getService(this, DebugService.class, null);
    }



    private DataFormula jips(MessageAddress agentAddress) {
	String host = getHostForAgent(agentAddress);
	if (host == null) return null;
	String[] args = { host };
	DataScopeSpec[] path = new DataScopeSpec[1];
	path[0] = new DataScopeSpec(HostDS.class, args);
	DataScope scope = rss.getDataScope(path);
	return scope.getFormula("Jips");
    }

    public double getExpectedMaxMJipsForAgent(MessageAddress agentAddress) {
	DataFormula jips = jips(agentAddress);
	if (jips != null) {
	    DataValue value = jips.query();
	    return value.getDoubleValue() / 1.0E6; // convert to MJIPS
	} else {
	    return 0.0;
	}
    }

    public Observable getExpectedMaxJipsForAgentObservable (MessageAddress agentAddress) 
    {
	DataFormula formula = jips(agentAddress);
	if (formula != null) {
	    return new BoundDataFormula(formula);
	} else {
	    return null;
	}
    }

    private ExpectedMaxJipsSC makeExpectedMaxMJipsSyscond(MessageAddress address) 
    {
	try {
	    SysCond syscond = 
		kernel.bindSysCond(address + "MaxMJips",
				   "com.bbn.quo.rmi.ExpectedMaxJipsSC",
				   "com.bbn.quo.data.ExpectedMaxJipsSCImpl");
	    return (ExpectedMaxJipsSC) syscond;
	} catch (java.rmi.RemoteException ex) {
	    debugService.error(null, ex);
	    return null;
	}
    }

    public Object getExpectedMaxMJipsForAgentSyscond(MessageAddress address)
    {
	ExpectedMaxJipsSC syscond = 
	    (ExpectedMaxJipsSC)effectiveJipsSysconds.get(address);
	if (syscond == null) {
	    syscond = makeExpectedMaxMJipsSyscond(address);
	    JipsSyscondListener syscondListener = 
		new JipsSyscondListener(syscond,address);
	    effectiveJipsSysconds.put(address, syscond);
	}
	return syscond;
    }


    // EFFECTIVE MJIPS


    private DataFormula effectiveMJips(MessageAddress agentAddress) {
	String host = getHostForAgent(agentAddress);
	if (host == null) return null;
	String[] args = { host };
	DataScopeSpec[] path = new DataScopeSpec[1];
	path[0] = new DataScopeSpec(HostDS.class, args);
	DataScope scope = rss.getDataScope(path);
	return scope.getFormula("EffectiveMJips");
    }

    public double getExpectedEffectiveMJipsForAgent(MessageAddress agentAddress) {
	DataFormula jips = effectiveMJips(agentAddress);
	if (jips != null) {
	    DataValue value = jips.query();
	    return value.getDoubleValue();
	} else {
	    return 0.0;
	}
    }

    public Observable getExpectedEffectiveMJipsForAgentObservable (MessageAddress agentAddress) 
    {
	DataFormula formula = effectiveMJips(agentAddress);
	if (formula != null) {
	    return new BoundDataFormula(formula);
	} else {
	    return null;
	}
    }

    private ExpectedAvailableJipsSC makeExpectedEffectiveMJipsSyscond(MessageAddress address) 
    {
	try {
	    SysCond syscond = 
		kernel.bindSysCond(address + "EffectiveMJips",
				   "com.bbn.quo.rmi.ExpectedAvailableJipsSC",
				   "com.bbn.quo.data.ExpectedAvailableJipsSCImpl");
	    return (ExpectedAvailableJipsSC) syscond;
	} catch (java.rmi.RemoteException ex) {
	    debugService.error(null, ex);
	    return null;
	}
    }

    public Object getExpectedEffectiveMJipsForAgentSyscond(MessageAddress address)
    {
	ExpectedAvailableJipsSC syscond = 
	    (ExpectedAvailableJipsSC)jipsSysconds.get(address);
	if (syscond == null) {
	    syscond = makeExpectedEffectiveMJipsSyscond(address);
	    EffectiveJipsSyscondListener syscondListener = 
		new EffectiveJipsSyscondListener(syscond,address);
	    jipsSysconds.put(address, syscond);
	}
	return syscond;
    }


    // BANDWIDTH
    private DataFormula bandwidth(MessageAddress agentAddress) {
	String host = getHostForAgent(agentAddress);
	if (host == null) return null;
	String[] args = { host };
	DataScopeSpec[] path = new DataScopeSpec[1];
	path[0] = new DataScopeSpec(HostDS.class, args);
	DataScope scope = rss.getDataScope(path);
	return scope.getFormula("CapacityUnused");
    }

    public double getExpectedBandwidthForAgent(MessageAddress agentAddress) {
	DataFormula bandwidth = bandwidth(agentAddress);
	if (bandwidth != null) {
	    DataValue value = bandwidth.query();
	    return value.getDoubleValue();
	} else {
	    return 2.0;
	}
    }

    public Observable getExpectedBandwidthForAgentObservable (MessageAddress agentAddress) 
    {
	DataFormula formula = bandwidth(agentAddress);
	if (formula != null) {
	    return new BoundDataFormula(formula);
	} else {
	    return null;
	}
    }

    private ExpectedBandwidthSC makeExpectedBandwidthSyscond(MessageAddress address) 
    {
	try {
	    SysCond syscond = 
		kernel.bindSysCond("Bandwidth " + 
				   local_host + 
				   " to " +
				   address ,
				   "com.bbn.quo.rmi.ExpectedBandwidthSC",
				   "com.bbn.quo.data.ExpectedBandwidthSCImpl");
	    ((ExpectedBandwidthSC) syscond).doubleValue(1.0);
	    return (ExpectedBandwidthSC) syscond;
	} catch (java.rmi.RemoteException ex) {
	    debugService.error(null, ex);
	    return null;
	}
    }

    public Object getExpectedBandwidthForAgentSyscond(MessageAddress address)
    {
	ExpectedBandwidthSC syscond = 
	    (ExpectedBandwidthSC)bandwidthSysconds.get(address);
	if (syscond == null) {
	    syscond = makeExpectedBandwidthSyscond(address);
	    BandwidthSyscondListener syscondListener = 
		new BandwidthSyscondListener(syscond,address);
	    bandwidthSysconds.put(address, syscond);
	}
	return syscond;
    }

     // CAPACITY
    private DataFormula capacity(MessageAddress agentAddress) {
	String host = getHostForAgent(agentAddress);
	if (host == null) return null;
	String[] args = { host };
	DataScopeSpec[] path = new DataScopeSpec[1];
	path[0] = new DataScopeSpec(HostDS.class, args);
	DataScope scope = rss.getDataScope(path);
	return scope.getFormula("CapacityMax");
    }

    public double getExpectedCapacityForAgent(MessageAddress agentAddress) {
	DataFormula capacity = capacity(agentAddress);
	if (capacity != null) {
	    DataValue value = capacity.query();
	    return value.getDoubleValue();
	} else {
	    return 2.0;
	}
    }

    public Observable getExpectedCapacityForAgentObservable (MessageAddress agentAddress) 
    {
	DataFormula formula = capacity(agentAddress);
	if (formula != null) {
	    return new BoundDataFormula(formula);
	} else {
	    return null;
	}
    }

    private ExpectedCapacitySC makeExpectedCapacitySyscond(MessageAddress address) 
    {
	try {
	    SysCond syscond = 
		kernel.bindSysCond("Max Bandwidth " + 
				   local_host + 
				   " to " +
				   address ,
				   "com.bbn.quo.rmi.ExpectedCapacitySC",
				   "com.bbn.quo.data.ExpectedCapacitySCImpl");
	    ((ExpectedCapacitySC) syscond).doubleValue(1.0);
	
	    return (ExpectedCapacitySC) syscond;
	} catch (java.rmi.RemoteException ex) {
	    debugService.error(null, ex);
	    return null;
	}
    }

    public Object getExpectedCapacityForAgentSyscond(MessageAddress address)
    {
	ExpectedCapacitySC syscond = 
	    (ExpectedCapacitySC)capacitySysconds.get(address);
	if (syscond == null) {
	    syscond = makeExpectedCapacitySyscond(address);
	    CapacitySyscondListener syscondListener = 
		new CapacitySyscondListener(syscond,address);
	    capacitySysconds.put(address, syscond);
	}
	return syscond;
    }


}
