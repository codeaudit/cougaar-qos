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

package org.cougaar.core.qos.rss;

import org.cougaar.core.agent.Agent;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.core.component.ComponentDescriptions;
import org.cougaar.core.component.ContainerSupport;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.node.ComponentInitializerService;
import org.cougaar.core.node.NodeControlService;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.qos.metrics.DataFeedRegistrationService;
import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.thread.ThreadServiceProvider;
import org.cougaar.qos.qrs.RSSUtils;

/**
 * This Component/Container provides the RSS-based implementation of
 * the MetricsService and MetricsUpdateService, and instantiates the
 * implementations, RSSMetricsServiceImpl and
 * RSSMetricsUpdateServiceImpl respectively, as child Components.
 * 
 * @see MetricsService
 * @see MetricsUpdateService
 * @see RSSMetricsServiceImpl
 * @see RSSMetricsUpdateServiceImpl
 */
public final class RSSMetricsServiceProvider
    extends ContainerSupport
    implements ServiceProvider
{
    private MetricsService retriever;
    private MetricsUpdateService updater;
    private DataFeedRegistrationService registrar;

    private void makeUpdaterService() {
	updater = new RSSMetricsUpdateServiceImpl();
	add(updater);
    }

    private void makeRetrieverService() {
	retriever = new RSSMetricsServiceImpl();
	add(retriever);
	registrar = (DataFeedRegistrationService) retriever;
    }


    // This is done before child-components are created
    public void loadHighPriorityComponents() {
        super.loadHighPriorityComponents();
	ServiceBroker sb = getServiceBroker();
	NodeControlService ncs = (NodeControlService)
	    sb.getService(this, NodeControlService.class, null);
	ServiceBroker rootsb = ncs.getRootServiceBroker();
	
	// DataFeeds could need a thread service
	// JAZ needs a ComponentDescription?
	ThreadServiceProvider tsp = new ThreadServiceProvider();
	tsp.setParameter("name=Metrics");
	add(tsp);

        // Make a Timer available to RSS and TEC
        //
        // We must do this before we load our subcomponents, otherwise RSSUtils
        // will spawn a default (non-ThreadService-backed) timer thread
        RSSUtils.setScheduler(new CougaarTimer(getChildServiceBroker()));

	// Childern Components need Registration Service
	// but the Registration need the MetricServiceImplementation
	// make Metric Updater Service
	makeUpdaterService();
	rootsb.addService(MetricsUpdateService.class, this);
	// make Metric Service and Feed registration service
	makeRetrieverService();
	rootsb.addService(MetricsService.class, this);
	// register registration service
	sb.addService(DataFeedRegistrationService.class, this);
    }

    // Service Provider API

    public Object getService(ServiceBroker sb, 
			     Object requestor, 
			     Class serviceClass) 
    {
	if (serviceClass == MetricsService.class) {
	    return retriever;
	} else if (serviceClass == MetricsUpdateService.class) {
	    return updater;
	} else if (serviceClass == DataFeedRegistrationService.class) {
	    return registrar;
	} else {
	    return null;
	}
    }

    public void releaseService(ServiceBroker sb, 
			       Object requestor, 
			       Class serviceClass, 
			       Object service)
    {
    }

    // Container API

    protected ComponentDescriptions findInitialComponentDescriptions() {
	ServiceBroker sb = getServiceBroker();
	ComponentInitializerService cis = (ComponentInitializerService) 
	    sb.getService(this, ComponentInitializerService.class, null);
	NodeIdentificationService nis = (NodeIdentificationService)
	    sb.getService(this, NodeIdentificationService.class, null);
	try {
	    String cp = specifyContainmentPoint();
	    String id = nis.getMessageAddress().toString();
	    ComponentDescription[] descs = cis.getComponentDescriptions(id,cp);
 	    // Want only items _below_. Could filter (not doing so now)
	    return new ComponentDescriptions(descs);
	} catch (ComponentInitializerService.InitializerException cise) {
	    cise.printStackTrace();
	    return null;
	} finally {
	    sb.releaseService(this, ComponentInitializerService.class, cis);
	    sb.releaseService(this, NodeIdentificationService.class, nis);
	}
    }

    protected String specifyContainmentPoint() {
	return Agent.INSERTION_POINT + ".MetricsServices";
    }
}
