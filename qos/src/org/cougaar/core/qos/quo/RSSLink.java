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
import org.cougaar.core.society.MessageAddress;
import org.cougaar.core.mts.NameSupport;
import org.cougaar.core.qos.monitor.ResourceMonitorServiceImpl;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Observable;


public class RSSLink extends ResourceMonitorServiceImpl
{
    private static final String RSS_PROPFILE = "org.cougaar.rss.propfile";
    private static final int PERIOD = 5000;

    private Timer timer = new Timer(true);
    private RSS rss;
    private QuoKernel kernel;
    private HashMap updaters = new HashMap();
    private HashMap jipsSysconds = new HashMap();
    

    interface AgentHostUpdaterListener {
	void newHost(String host);
    }

    private class JipsSyscondListener implements AgentHostUpdaterListener {
	private ExpectedMaxJipsSC syscond;

	JipsSyscondListener(ExpectedMaxJipsSC syscond, MessageAddress address)
	{
	    this.syscond = syscond;
	    getUpdater(address).addListener(this);
	}

	public void newHost(String host) {
	    try {
		syscond.setHost(host);
	    } catch (java.rmi.RemoteException remote_ex) {
		remote_ex.printStackTrace();
	    }
	}
    }

    private class AgentHostUpdater 
	extends TimerTask 
    {
	private MessageAddress agent;
	private String host;
	private ArrayList listeners;

	AgentHostUpdater(MessageAddress agent) {
	    this.agent = agent;
	}

	public void addListener(AgentHostUpdaterListener listener) {
	    listeners.add(listener);
	    if (host != null) listener.newHost(host);
	}

	public void removeListener(AgentHostUpdaterListener listener) {
	    listeners.remove(listener);
	}

	public void run() {
	    String new_host = getHostForAgent(agent);
	    if (new_host == null) return;
	    if (host == null || !host.equals(new_host)) {
		host = new_host;
		System.out.println("===== New host " + host +
				   " for agent " + agent);
		Iterator itr = listeners.iterator();
		while (itr.hasNext()) {
		    AgentHostUpdaterListener listener =
			(AgentHostUpdaterListener) itr.next();
		    listener.newHost(host);
		}
	    }
	}
    }

	    
    public RSSLink(NameSupport nameSupport, ServiceBroker sb) {
	super(nameSupport, sb);
	String propfile = System.getProperty(RSS_PROPFILE);
	rss = RSS.makeInstance(propfile);
	kernel = Utils.getKernel();
	if (Boolean.getBoolean("org.cougaar.lib.quo.kernel.gui")) {
	    try {
		kernel.newFrame();
	    } catch (java.rmi.RemoteException ex) {
		ex.printStackTrace();
	    }
	}
    }


    private synchronized AgentHostUpdater getUpdater(MessageAddress address) {
	AgentHostUpdater updater = (AgentHostUpdater) updaters.get(address);
	if (updater == null) {
	    updater = new AgentHostUpdater(address);
	    updaters.put(address, updater);
	    timer.schedule(updater, 0, PERIOD);
	}
	return updater;
    }

    private DataFormula jips(MessageAddress agentAddress) {
	String host = getHostForAgent(agentAddress);
	if (host == null) return null;
	String[] args = { host };
	DataScopeSpec[] path = new DataScopeSpec[1];
	path[0] = new DataScopeSpec(HostDS.class, args);
	DataScope scope = rss.getDataScope(path);
	return scope.getFormula("EffectiveMJips");
    }

    public double getJipsForAgent(MessageAddress agentAddress) {
	DataFormula jips = jips(agentAddress);
	if (jips != null) {
	    DataValue value = jips.query();
	    return value.getDoubleValue();
	} else {
	    return 0.0;
	}
    }

    public Observable getJipsForAgentObservable(MessageAddress agentAddress) {
	DataFormula formula = jips(agentAddress);
	if (formula != null) {
	    return new BoundDataFormula(formula);
	} else {
	    return null;
	}
    }

    private ExpectedMaxJipsSC makeJipsSyscond(MessageAddress address) {
	try {
	    SysCond syscond = 
		kernel.bindSysCond(address + "MaxJips",
				   "com.bbn.quo.rmi.ExpectedMaxJipsSC",
				   "com.bbn.quo.data.ExpectedMaxJipsSCImpl");
	    return (ExpectedMaxJipsSC) syscond;
	} catch (java.rmi.RemoteException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    public Object getJipsForAgentSyscond(MessageAddress address) {
	ExpectedMaxJipsSC syscond = 
	    (ExpectedMaxJipsSC)jipsSysconds.get(address);
	if (syscond == null) {
	    syscond = makeJipsSyscond(address);
	    JipsSyscondListener syscondListener = 
		new JipsSyscondListener(syscond,address);
	    jipsSysconds.put(address, syscond);
	}
	return syscond;
    }

}
