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

package org.cougaar.core.qos.ca;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricNotificationQualifier;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.metrics.VariableEvaluator;
import org.cougaar.core.service.BlackboardService;

/**
 * CA interface to the MetricsService
 */
abstract public class MetricsReaderFacet 
    extends FacetImpl
{

    private MetricsService metricsService;
    private HashMap subscriptionKeys;

    protected MetricsReaderFacet(CoordinationArtifact owner,
				 ServiceBroker sb,
				 ConnectionSpec spec, 
				 RolePlayer player)
    {
	super(owner, sb, spec, player);
	subscriptionKeys = new HashMap();
	metricsService = (MetricsService)
	    sb.getService(this, MetricsService.class, null);
	linkPlayer();
    }


    abstract protected String getPath(Object fact);
    abstract protected MetricNotificationQualifier getQualifier(Object fact);
    abstract protected VariableEvaluator getEvaluator(Object fact);
    abstract protected void assertDataFact(Metric value);

    private class SubscriptionObserver implements Observer
    {
	String path;
	SubscriptionObserver(String path)
	{
	    this.path = path;
	}

	public void update(Observable xxx, Object update) 
	{
	    assertDataFact((Metric) update);
	}
    }

    private void subscribe(Object fact)
    {
	String path = getPath(fact);
	VariableEvaluator evaluator = getEvaluator(fact);
	MetricNotificationQualifier qualifier = getQualifier(fact);
	Observer observer = new SubscriptionObserver(path);
	Object key = metricsService.subscribeToValue(path, observer, 
						     evaluator, qualifier);
	synchronized (subscriptionKeys) {
	    subscriptionKeys.put(path, key);
	}
    }

    private void unsubscribe(Object fact)
    {
	String path = getPath(fact);
	Object key = null;
	synchronized (subscriptionKeys) {
	    key = subscriptionKeys.get(path);
	    subscriptionKeys.remove(path);
	}
	if (key != null) metricsService.unsubscribeToValue(key);
	
    }

    public void setupSubscriptions(BlackboardService blackboard)
    {
    }

    public void execute(BlackboardService blackboard)
    {
    }

    public void processFactBase(BlackboardService blackboard)
    {
	if (!factsHaveChanged()) return;
	for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
	    Object fact = frev.getFact();
	    if (frev instanceof FactAssertion) {
		subscribe(fact);
	    } else if (frev instanceof FactRetraction) {
		unsubscribe(fact);
	    }
	}
    }


}


