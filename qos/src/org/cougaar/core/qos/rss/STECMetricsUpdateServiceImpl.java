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

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.naming.NS;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.node.NodeIdentifier;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.NamingService;
import org.cougaar.core.service.ThreadService;

import com.bbn.quo.event.Connector;
import com.bbn.quo.event.status.StatusTEC;
import com.bbn.quo.event.topology.TopologyRing;
import com.bbn.quo.event.sysstat.StatusSupplierSysStat;
import com.bbn.quo.event.sysstat.DirectSysStatSupplier;

import org.omg.CosTypedEventChannelAdmin.TypedEventChannel;

import java.util.TimerTask;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.BasicAttributes;

/**
 * The implementation of MetricsUpdateService, and a child component
 * of MetricsServiceProvider.  This implementation uses the RSS,
 * either directly or via a CORBA TypedEventChannel, depending on
 * whether or not JacORB is available.
 *
 * @property org.omg.CORBA.ORBClass Set this to org.jacorb.orb.ORB if
 * you want to use the TypedEventChannels to share metrics among
 * Nodes in a society and to get data from third-party collectors.
 *
 * @property org.omg.CORBA.ORBSingletonClass Set this to
 * org.jacorb.orb.ORBSingleton if you want to use the
 * TypedEventChannels to share metrics among Nodes in a society and to
 * get data from third-party collectors.
 *
 * @property org.cougaar.metrics.stec If true, and if JacORB is
 * enabled, a TypedEventChannel data feed will be created in the RSS.
 * If false (the default) or if JacORB is not enabled, a simple data
 * feed will be created instead.
 *
 * @property org.cougaar.metrics.stec.mesh If true, and if event
 * channels are in use, the channels from each Node in the society
 * will be linked in a ring.  This enables metrics data collected in
 * one Node to be accessed by another Node.  If
 * org.cougaar.metrics.stec.mesh is true, org.cougaar.metrics.stec is
 * ignored.
 *
 * @property org.cougaar.metrics.topology.iorfile If present, and if
 * event channels are in use, the ior of the channel topology manager
 * will be written to the given file.  This allows external
 * applications to communicate with the RSS.
 *
 */
public class STECMetricsUpdateServiceImpl
    extends QosComponent
    implements MetricsUpdateService
{
    private static final String RSS_DIR = "RSS";
    private static final String USE_TEC_PROPERTY = 
	"org.cougaar.metrics.stec";
    private static final String USE_TOPOLOGY_PROPERTY = 
	"org.cougaar.metrics.stec.mesh";
    private static final String TOPOLOGY_DUMP_IORFILE_PROPERTY = 
	"org.cougaar.metrics.topology.iorfile";
    
    private STECSender sender;
    private NamingService namingService;
    private TypedEventChannel channel;
    private TrivialDataFeed dataFeed;
    private com.bbn.quo.data.DataInterpreter interpreter;

    public STECMetricsUpdateServiceImpl() {
    }

    private boolean use_tec() {
	boolean use_tec = 
	    Boolean.getBoolean(USE_TOPOLOGY_PROPERTY) ||
	    Boolean.getBoolean(USE_TEC_PROPERTY);
	if (!use_tec) return false;
	String orbclass = System.getProperty("org.omg.CORBA.ORBClass");
	return orbclass != null && orbclass.equals("org.jacorb.orb.ORB");
    }

    public void load() {
	super.load();

	ServiceBroker sb = getServiceBroker();

	if (use_tec()) {
	    namingService = (NamingService)
		sb.getService(this, NamingService.class, null);

	    NodeIdentificationService nis = (NodeIdentificationService)
		sb.getService(this, NodeIdentificationService.class, null);
	    NodeIdentifier id = nis.getNodeIdentifier();

	    channel = makeChannel(id);

	    StatusSupplierSysStat sysstat =
		new StatusSupplierSysStat(channel);
	    sysstat.schedule(3000);


	    sender = new STECSender(sb, channel, Connector.poa());

	} else {
	    dataFeed = new TrivialDataFeed(sb);
	    interpreter = new MetricInterpreter();
	    DirectSysStatSupplier supplier = 
		new DirectSysStatSupplier(dataFeed);
	    supplier.schedule(3000);
	}

    }

    TrivialDataFeed getMetricsFeed() {
	return dataFeed;
    }

    TypedEventChannel getChannel() {
	return channel;
    }

    private Object grabKey(String key, Object value) {
	DirContext ctx = null;
	Name name = null;
	try {
	    ctx = namingService.getRootContext();
	    name = ctx.getNameParser("").parse(key);
	    while (name.size() > 1) {
		Name prefix = name.getPrefix(1);
		name = name.getSuffix(1);
		try {
		    ctx = (DirContext) ctx.lookup(prefix);
		} catch (NamingException ne) {
		    BasicAttributes empty_attr = new BasicAttributes();
		    ctx = (DirContext) 
			ctx.createSubcontext(prefix, empty_attr);
		} catch (Exception e) {
		    throw new NamingException(e.toString());
		}
	    }
	} catch (NamingException ex) {
	    return null;
	}

	try {
	    ctx.bind(name, value, new BasicAttributes());
	    return value;
	} catch (NamingException bind_ex) {
	    try { return ctx.lookup(name); }
	    catch (NamingException lookup_ex) { return null; }
	}
    }

    private Object lookupKey(String key) {
	try {
	    DirContext ctx = namingService.getRootContext();
	    Name name = ctx.getNameParser("").parse(key);
	    while (name.size() > 1) {
		Name prefix = name.getPrefix(1);
		name = name.getSuffix(1);
		try {
		    ctx = (DirContext) ctx.lookup(prefix);
		} catch (NamingException ne) {
		    BasicAttributes empty_attr = new BasicAttributes();
		    ctx = (DirContext) 
			ctx.createSubcontext(prefix, empty_attr);
		} catch (Exception e) {
		    throw new NamingException(e.toString());
		}
	    }
	    return ctx.lookup(key);
	} catch (NamingException ex) {
	    return null;
	}

    }


    // Silly approach, but the only one I can think of: make a
    // topology manager; register it in the name server if no other
    // process has created; kill it and grab whatever's registered if
    // some other process has created one.
    private String topologyIOR() {
	String key = RSS_DIR +NS.DirSeparator+ "TopologyIOR";
	String ior = (String) lookupKey(key);
	if (ior != null) {
	    // System.out.println("Found TopologyManager " +ior);
	    return ior;
	}

	Connector.orb(); // ensure orb exists
	TopologyRing mgr = new TopologyRing();
	ior = Connector.orb().object_to_string(mgr._this());
	String real_ior = (String) grabKey(key, ior);
	if (real_ior == ior) {
	    String iorfile = System.getProperty(TOPOLOGY_DUMP_IORFILE_PROPERTY);
	    mgr.start(iorfile);
	} else {
	    // System.out.println("Found TopologyManager " +real_ior);
	}
	return real_ior;
    }

    private TypedEventChannel makeChannel(NodeIdentifier id) {
	String ior = null;
	String channel_id = null;
	StatusTEC channel = null;
	if (Boolean.getBoolean(USE_TOPOLOGY_PROPERTY)) {
	    ior = topologyIOR();
	    channel_id = id+"Channel";
	    String args[] = 
		{ "-queue",
		  "com.bbn.quo.event.status.PrioritizedStatusChunkingQ",
		  "-policy",
		  "com.bbn.quo.event.status.StatusValidatingCachingPolicy",
		  "-id", channel_id,
		  "-ior", ior,
		};
	    channel = new StatusTEC(args);
	} else {
	    String args[] = 
		{ "-queue",
		  "com.bbn.quo.event.status.PrioritizedStatusChunkingQ",
		  "-policy",
		  "com.bbn.quo.event.status.StatusValidatingCachingPolicy",
		};
	    channel = new StatusTEC(args);
	}

	return channel._this();
    }

    public void updateValue(String key, Metric value) {
	if (sender != null) 
	    sender.send(key, value);
	else
	    dataFeed.newData(key, value, interpreter);
    }


}
