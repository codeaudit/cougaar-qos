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

package org.cougaar.lib.mquo;

import java.util.Observable;
import java.util.Observer;

import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.rss.DataWrapper;

import com.bbn.quo.ValueSCImpl;

public class MetricSCImpl
    extends ValueSCImpl
    implements Observer, MetricSCOperations
{

    private String path;
    private MetricsService svc;
    private Object key;

    public void update(Observable obs, Object val) {
	DataWrapper wrapper = (DataWrapper) val;
	if (val != null)  setValueInternal(wrapper.getDataValue());
    }

    public synchronized void init(MetricsService svc) {
	this.svc = svc;
    }
    
    public synchronized void newPath(String path) {
	if (key != null) svc.unsubscribeToValue(key);
	this.path = path;
	this.key = svc.subscribeToValue(path, this);
    }

}
