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
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.service.LoggingService;

import org.cougaar.core.qos.rss.AgentHostUpdater;
import org.cougaar.core.qos.rss.AgentHostUpdaterListener;

import java.util.HashMap;


final class SyscondFactoryImpl implements SyscondFactoryService
{
    private static String local_host;


    static {
	try {
	    local_host = java.net.InetAddress.getLocalHost().getHostAddress();
	} catch (java.net.UnknownHostException ex) {
	    local_host = "127.0.0.1";
	}
    }


    private QuoKernel kernel;
    private AgentHostUpdater updater;
    private HashMap jipsSysconds = new HashMap();
    private HashMap effectiveJipsSysconds = new HashMap();
    private HashMap bandwidthSysconds = new HashMap();
    private HashMap capacitySysconds = new HashMap();
    private LoggingService loggingService;
    private MetricsService metricsService;


    abstract private class MetricSCListener
	implements AgentHostUpdaterListener 
    {
	protected MetricSC syscond;
	private MessageAddress agentAddress;

	MetricSCListener(MetricSC syscond, MessageAddress address)
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
		ex.printStackTrace();
	    }
	}

    }

    private class JipsSyscondListener extends MetricSCListener {
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
	extends MetricSCListener 
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


    private class BandwidthSyscondListener extends MetricSCListener
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


    private class CapacitySyscondListener extends MetricSCListener {
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




    SyscondFactoryImpl(ServiceBroker sb) {
	kernel = Utils.getKernel();

	metricsService = (MetricsService)
	    sb.getService(this, MetricsService.class, null);

	loggingService = (LoggingService) 
	    sb.getService(this, LoggingService.class, null);

	updater = (AgentHostUpdater)
	    sb.getService(this, AgentHostUpdater.class, null);
    }




    private MetricSC makeMetricSC(String name) 
    {
	try {
	    MetricSC syscond = 
		(MetricSC)
		kernel.bindSysCond(name,
				   "org.cougaar.lib.mquo.MetricSC",
				   "org.cougaar.lib.mquo.MetricSCImpl");
	    
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
	    ex.printStackTrace();
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
	    ex.printStackTrace();
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
