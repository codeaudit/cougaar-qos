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

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.DataFeedRegistrationService;
import org.cougaar.core.qos.metrics.DataProvider;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricNotificationQualifier;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.qos.metrics.VariableEvaluator;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.RunnableQueue;
import org.cougaar.util.ConfigFinder;

import com.bbn.quo.data.BoundDataFormula;
import com.bbn.quo.data.DataFeed;
import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.NotificationQualifier;
import com.bbn.quo.data.RSS;
import com.bbn.quo.data.RSSUtils;
import com.bbn.quo.data.SitesDB;

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
    implements MetricsService, DataFeedRegistrationService
{

    // Setup name->class mappings



    private static final String RSS_PROPERTIES =
	"org.cougaar.metrics.properties";
    
    private LoggingService loggingService;
    private ThreadService threadService;
    private RSSMetricsUpdateServiceImpl metricsUpdateService;
    private RunnableQueue subscriptionQueue;
    private HashMap bdfCache;

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


    private static class Qualifier implements NotificationQualifier
    {
	MetricNotificationQualifier qualifier;

	Qualifier(MetricNotificationQualifier qualifier) {
	    this.qualifier = qualifier;
	}

	public synchronized boolean shouldNotify(DataValue value) {
	    return qualifier.shouldNotify(new DataWrapper(value));
	}

    }

    public RSSMetricsServiceImpl() {
    }

    public void load() {
	super.load();

	ServiceBroker sb = getServiceBroker();
	loggingService = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	threadService = (ThreadService)
	    sb.getService(this, ThreadService.class, null);

	subscriptionQueue = new RunnableQueue(threadService, "SubscriptionQueue");

	MetricsUpdateService mus = (MetricsUpdateService)
	    sb.getService(this, MetricsUpdateService.class, null);
	this.metricsUpdateService = (RSSMetricsUpdateServiceImpl) mus;

	Properties properties = new Properties();
	String propertiesURL = System.getProperty(RSS_PROPERTIES);
	if (propertiesURL != null) {
	    try {
		URL url = new URL(propertiesURL);
		java.io.InputStream is = url.openStream();
		properties.load(is);
		is.close();
	    } catch (Exception ex) {
	    }
	}

	// Make a ServiceBroker available to AgentDS and HostDS.
	properties.put("ServiceBroker", sb);

	RSSUtils.registerPackage("org.cougaar.core.qos.rss");
	RSSUtils.registerPackage("org.cougaar.core.qos.gossip");

	// Make a Timer available to RSS and TEC
	CougaarTimer timer = new CougaarTimer(sb);
	RSSUtils.setScheduler(timer);

	DataFeed feed = null;
	String feedName = null;
	feed = metricsUpdateService.getMetricsFeed();
	feedName = "MetricsDataFeed";

	RSS.makeInstance(properties);
	if (feed != null) {
	    feed.setName(feedName);
	    RSS.instance().registerFeed(feed, feedName);
	}

	bdfCache = new HashMap();

	// Used to start this here.  Now do it via loadable Component.
	// AgentHostUpdaterComponent comp = new AgentHostUpdaterComponent();
	// comp.provideService(sb);
    }

    //Data Feed Registration Service
    public boolean registerFeed(Object feed, String name) {
	if (feed instanceof DataFeed) {
	    RSS.instance().registerFeed((DataFeed) feed, name);
	    return true;
	} else {
	    return false;
	}
    }

    public void populateSites(String sitesURLString) {
	ConfigFinder finder = ConfigFinder.getInstance();
	SitesDB db = RSS.instance().getSitesDB();
	try {
	    URI uri = null;
	    try {
		uri = new URI(sitesURLString);
	    } catch (java.net.URISyntaxException ex) {
		return;
	    }
	    
	    String scheme = uri.getScheme();
	    String path = uri.getSchemeSpecificPart();
	    if (scheme.equals(ConfigFinderDataFeedComponent.CONFIG_PROTOCOL)) {
		InputStream stream = finder.open(path);
		db.populate(stream);
	    } else {
		    URL sitesURL = new URL(sitesURLString);
		    db.populate(sitesURL);
	    }
	} catch (Exception ex) {
	    loggingService.error(null, ex);
	}
    }

    private static final Pattern pattern = Pattern.compile("\\$\\([^\\)]*\\)");

    private String evaluateVariables(String path, VariableEvaluator eval) {
	if (eval == null) return path;

	StringBuffer buf = new StringBuffer();
	Matcher matcher = pattern.matcher(path);
	while (matcher.find()) {
	    String match = matcher.group();
	    String var = match.substring(2, match.length()-1);
	    String val = eval.evaluateVariable(var);
	    if (val == null) {
		if (loggingService.isErrorEnabled())
		    loggingService.error("Path variable " +var+
					 " has no value");
		// What's the right way to proceed?  For now put
		// pattern back in.
		matcher.appendReplacement(buf, match);
	    } else {
		matcher.appendReplacement(buf, val);
	    }
	}
	matcher.appendTail(buf);

	return buf.toString();
    }

    // Metric Service
    public Metric getValue(String path) {
	return getValue(path, null, null);
    }

    public Metric getValue(String path, Properties qos_tags) {
	return getValue(path, null,  qos_tags);
    }

    public Metric getValue(String path, VariableEvaluator evaluator) {
	return getValue(path, evaluator, null);
    }



    // Qos properties not supported yet
    public Metric getValue(String path, 
			   VariableEvaluator evaluator,
			   Properties qos_tags) 
    {
	if (path == null) return null;

	path = evaluateVariables(path, evaluator);
	BoundDataFormula bdf = null;
	synchronized (bdfCache) {
	    bdf = (BoundDataFormula) bdfCache.get(path);
	    if (bdf == null) {
		try { 
		    bdf = new BoundDataFormula(path);	
		} 
		catch (com.bbn.quo.data.NullFormulaException ex) {
		    return null;
		}
		bdfCache.put(path, bdf);
	    }
	}
	Object rawValue = bdf.getCurrentValue();
	// Object rawValue = RSSUtils.getPathValue(path);
	
	if (rawValue == null) {
	    return null;
	} else if (rawValue instanceof DataFormula) {
	    return new DataWrapper(((DataFormula) rawValue).query());
	} else if (rawValue instanceof DataValue) {
	    return  new DataWrapper((DataValue) rawValue);
	} else {
	    if (loggingService.isErrorEnabled())
		loggingService.error("Unexpected data value " + rawValue +
				     " for path " + path);
	    return null;
	}
    }



    public Object subscribeToValue(String path, Observer observer) {
	return subscribeToValue(path, observer, null, null);
    }

    public Object subscribeToValue(String path, 
				   Observer observer,
				   VariableEvaluator evaluator)
    {
	return subscribeToValue(path, observer, evaluator, null);
    }

    public Object subscribeToValue(String path, 
				   Observer observer,
				   MetricNotificationQualifier qualifier) 
    {
	return subscribeToValue(path, observer, null, qualifier);
    }


    public Object subscribeToValue(String path, 
				   Observer observer,
				   VariableEvaluator evaluator,
				   MetricNotificationQualifier qualifier)
    {
	path = evaluateVariables(path, evaluator);
	try {
	    Qualifier qual = 
		qualifier == null ? null : new Qualifier(qualifier);
	    // Defer the formula creation, since it might result in a
	    // 'dns' lookup.
	    BoundDataFormula bdf = new BoundDataFormula(path, true, qual);
	    Runnable binder = bdf.getDelayedFormulaCreator();
	    subscriptionQueue.add(binder);
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

