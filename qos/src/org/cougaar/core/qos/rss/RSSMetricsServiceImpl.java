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

import com.bbn.quo.data.BoundDataFormula;
import com.bbn.quo.data.DataFeed;
import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.RSS;
import com.bbn.quo.data.RSSUtils;
import com.bbn.quo.event.Connector;
import com.bbn.quo.event.EventUtils;
import com.bbn.quo.event.data.StatusConsumerFeed;


import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.DataProvider;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.node.NodeIdentifier;
import org.cougaar.core.service.LoggingService;

import org.omg.CosTypedEventChannelAdmin.TypedEventChannel;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;

/**
 * The implementation of MetricsService, and a child component of
 * MetricsServiceProvider.  This implementation uses the RSS for data
 * lookup.
 *
 * @property org.cougaar.metrics.properties The name of an RSS config
 * file.
 */
public class RSSMetricsServiceImpl 
    extends QosComponent
    implements MetricsService 
{

    // Setup name->class mappings

    static {
	DataScopeSpec.defineNameToClass("Node",
					org.cougaar.core.qos.rss.NodeDS.class);
	DataScopeSpec.defineNameToClass("node", 
					org.cougaar.core.qos.rss.NodeDS.class);
	DataScopeSpec.defineNameToClass("NODE", 
					org.cougaar.core.qos.rss.NodeDS.class);
	DataScopeSpec.defineNameToClass("Agent",
					org.cougaar.core.qos.rss.AgentDS.class);
	DataScopeSpec.defineNameToClass("agent", 
					org.cougaar.core.qos.rss.AgentDS.class);
	DataScopeSpec.defineNameToClass("AGENT", 
					org.cougaar.core.qos.rss.AgentDS.class);
	DataScopeSpec.defineNameToClass("Service",
					org.cougaar.core.qos.rss.ServiceDS.class);
	DataScopeSpec.defineNameToClass("service", 
					org.cougaar.core.qos.rss.ServiceDS.class);
	DataScopeSpec.defineNameToClass("SERVICE", 
					org.cougaar.core.qos.rss.ServiceDS.class);
    }


    private static final String RSS_PROPERTIES =
	"org.cougaar.metrics.properties";
    
    private LoggingService loggingService;
    private STECMetricsUpdateServiceImpl metricsUpdateService;

    static class DataWrapper implements Metric {
	private DataValue data;

	DataWrapper(DataValue data) {
	    this.data = data;
	}

	public String toString() {
	    return data.toString();
	}

	DataValue getDataValue() {
	    return data;
	}

	public String stringValue() { return data.getStringValue(); }
	public byte byteValue() { return data.getByteValue(); }
	public short shortValue() { return data.getShortValue(); }
	public int intValue() { return data.getIntValue(); }
	public long longValue() { return data.getLongValue(); }
	public float floatValue() { return data.getFloatValue(); }
	public double doubleValue() { return data.getDoubleValue(); }
	public char charValue() { return data.getCharValue(); }
	public boolean booleanValue() { return data.getBooleanValue(); }

	public Object getRawValue() { return data.getRawValue(); }
	public double getCredibility() { return data.getCredibility(); }
	public String getUnits() { return data.getUnits(); }
	public String getProvenance() { return data.getProvenance(); }
    }


    private static class DataValueObserver 
	implements Observer, DataProvider
    {
	Observer observer;
	BoundDataFormula bdf;

	DataValueObserver(Observer observer, BoundDataFormula bdf) {
	    this.observer = observer;
	    this.bdf = bdf;
	    
	    bdf.addObserver(this);
	}

	public void update (Observable observable, Object value) {
	    DataValue dValue = (DataValue) value;
	    DataWrapper wrapper = new DataWrapper(dValue);
	    observer.update(observable, wrapper);
	}

	void unsubscribe() {
	    bdf.deleteObserver(this);
	}

	public String getPath() {
	    return bdf.getPath();
	}
    }

    public RSSMetricsServiceImpl() {
    }

    public void load() {
	super.load();

	ServiceBroker sb = getServiceBroker();
	loggingService = (LoggingService)
	    sb.getService(this, LoggingService.class, null);

	MetricsUpdateService mus = (MetricsUpdateService)
	    sb.getService(this, MetricsUpdateService.class, null);
	this.metricsUpdateService = (STECMetricsUpdateServiceImpl) mus;

	Properties properties = new Properties();
	String propertiesURL = System.getProperty(RSS_PROPERTIES);
	if (propertiesURL != null) {
	    try {
		java.net.URL url = new java.net.URL(propertiesURL);
		java.io.InputStream is = url.openStream();
		properties.load(is);
		is.close();
	    } catch (Exception ex) {
	    }
	}

	// Make a ServiceBroker available to AgentDS and HostDS.
	properties.put("ServiceBroker", sb);

	// Make a Timer available to RSS and TEC
	Timer timer = new CougaarTimer(sb);
	RSSUtils.setTimer(timer);
	EventUtils.setTimer(timer);

	DataFeed feed = null;
	String feedName = null;
	TypedEventChannel channel = null;
	channel = metricsUpdateService.getChannel();
	if (channel != null) {
	    String ior = Connector.orb().object_to_string(channel);
	    // 		String feeds = properties.getProperty("rss.DataFeeds", "");
	    // 		feeds += " STEC_Channel";
	    // 		properties.put("rss.DataFeeds", feeds);
	    // 		properties.put("STEC_Channel.class", 
	    // 			       "com.bbn.quo.event.data.StatusConsumerFeed");
	    // 		properties.put("STEC_Channel.args", "-ior " +ior);
	    String[] args = { "-ior", ior };
	    feed = new StatusConsumerFeed(args);
	    feedName = "Local_STEC_Channel";
	} else {
	    feed = metricsUpdateService.getMetricsFeed();
	    feedName = "MetricsDataFeed";
	}

	RSS.makeInstance(properties);
	if (feed != null) {
	    feed.setName(feedName);
	    RSS.instance().registerFeed(feed, feedName);
	}
    }



    public Metric getValue(String path) {
	Object rawValue = RSSUtils.getPathValue(path);
	
	if (rawValue == null) {
	    return null;
	} else if (rawValue instanceof DataFormula) {
	    return new DataWrapper(((DataFormula) rawValue).query());
	} else if (rawValue instanceof DataValue) {
	    return  new DataWrapper((DataValue) rawValue);
	} else {
	    System.err.println("Unexpected data value " + rawValue +
			       " for path " + path);
	    return null;
	}

    }

    public Metric getValue(String path, Properties qos_tags) {
	// TO BE DONE
	return getValue(path);
    }

    public Object subscribeToValue(String path, Observer observer) {
	try {
	    BoundDataFormula bdf = new BoundDataFormula(path);
	    return new DataValueObserver(observer, bdf);
	} catch (com.bbn.quo.data.NullFormulaException ex) {
	    loggingService.error(path+ " is not valid");
	    return null;
	}
    }

    public void unsubscribeToValue(Object key) {
	DataValueObserver obs = (DataValueObserver) key;
	obs.unsubscribe();
    }

}

