/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.core.service.ThreadService;

import com.bbn.quo.sysstat.DirectSysStatSupplier;


import java.net.URI;
import java.util.StringTokenizer;
import java.util.TimerTask;

/**
 * The implementation of MetricsUpdateService, and a child component
 * of MetricsServiceProvider.  This implementation uses the RSS.
 *
 * @property org.cougaar.metrics.probes If provided, this should be a
 * comma-separated list of host probes, as understood by the QuO
 * StatusTEC; or 'all'; or 'none'.  The list of possible probes is as
 * follows: Bogomips, Cache, Clock, FreeMemory, TotalMemory,
 * LoadAverage, TCPInUse, UDPInUse, Jips, and CPUCount.  If the
 * property is not provided, or is provided with the value 'all', all
 * probes are run.  If the property is provided with the value 'none',
 * the probe task will not be started.  Note that some probes are only
 * available in Linux.
 *
 *
 */
public class RSSMetricsUpdateServiceImpl
    extends QosComponent
    implements MetricsUpdateService
{
    private static final String SYSSTAT_KINDS_PROPERTY = 
	"org.cougaar.metrics.probes";
    
    private TrivialDataFeed dataFeed;
    private com.bbn.quo.data.DataInterpreter interpreter;

    public RSSMetricsUpdateServiceImpl() {
    }

    public void load() {
	super.load();

	ServiceBroker sb = getServiceBroker();

	String kinds_string = System.getProperty(SYSSTAT_KINDS_PROPERTY);
	// kinds_string absent or 'all' for all probes
	// kinds_string empty or 'none' for no probes
	// Otherwise it should be a comma-separated list

	String[] kinds = null;
	// kinds == null for all probes
	// kinds == zero-length array for no probes
	// kinds == true array of strings for specified probes

	if (kinds_string != null) {
	    if (kinds_string.equalsIgnoreCase("none")) {
		kinds = new String[0];
	    } else if (!kinds_string.equalsIgnoreCase("all")) {
		StringTokenizer tokenizer = 
		    new StringTokenizer(kinds_string, ",");
		kinds = new String[tokenizer.countTokens()];
		int i = 0;
		while (tokenizer.hasMoreElements()) {
		    kinds[i] = tokenizer.nextToken();
		}
	    }
	}

	dataFeed = new TrivialDataFeed(sb);
	interpreter = new MetricInterpreter();
	if (kinds == null || kinds.length > 0) {
	    DirectSysStatSupplier supplier = 
		new DirectSysStatSupplier(kinds, dataFeed);
	    supplier.schedule(3000);
	}

    }

    TrivialDataFeed getMetricsFeed() {
	return dataFeed;
    }


    public void updateValue(String key, Metric value) {
	dataFeed.newData(key, value, interpreter);
    }


}
