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
import org.cougaar.core.node.NodeIdentifier;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.service.NamingService;
import org.cougaar.core.service.ThreadService;

import com.bbn.quo.event.Connector;
import com.bbn.quo.event.status.StatusTEC;
import com.bbn.quo.event.topology.TopologyRing;

import org.omg.CosTypedEventChannelAdmin.TypedEventChannel;

import java.util.TimerTask;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.BasicAttributes;

public class STECMetricsUpdateServiceImpl
    implements MetricsUpdateService
{
    private static final String RSS_DIR = "RSS";
    
    private ServiceBroker sb;
    private STECSender sender;
    private NamingService namingService;


    public STECMetricsUpdateServiceImpl(ServiceBroker sb, NodeIdentifier id) {
	this.sb = sb;

	// make sure jacorb is configured
	String orbclass = System.getProperty("org.omg.CORBA.ORBClass");
	if (orbclass == null || !orbclass.equals("org.jacorb.orb.ORB")) return;

	namingService = (NamingService)
	    sb.getService(this, NamingService.class, null);

	TypedEventChannel channel = getChannel(id);

	sender = new STECSender(channel, Connector.poa());

	Heartbeater beater = new Heartbeater(sender);
	ThreadService threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);
	TimerTask task = threadService.getTimerTask(this, beater, "Beater");
	threadService.schedule(task, 0, 1000);

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
	    System.out.println("Found TopologyManager " +ior);
	    return ior;
	}

	TopologyRing mgr = new TopologyRing();
	ior = Connector.orb().object_to_string(mgr._this());
	String real_ior = (String) grabKey(key, ior);
	if (real_ior == ior) {
	    System.err.println("Running TopologyManager " +ior);
	    mgr.start(null);
	} else {
	    System.out.println("Found TopologyManager " +real_ior);
	}
	return real_ior;
    }

    private TypedEventChannel getChannel(NodeIdentifier id) {
	String ior = topologyIOR();
	String channel_id = id+"Channel";
	String args[] = 
	    { "-queue",
	      "com.bbn.quo.event.status.PrioritizedStatusChunkingQ",
	      "-policy",
	      "com.bbn.quo.event.status.StatusValidatingCachingPolicy",
	      "-id", channel_id,
	      "-ior", ior,
	    };
	StatusTEC channel = new StatusTEC(args);
	return channel._this();
    }

    public void updateValue(String key, String type, Metric value) {
	if (sender != null) 
	    sender.send(key, type, value);
	else
	    System.err.println("No JacORB!");
    }


}