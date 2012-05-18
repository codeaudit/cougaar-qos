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

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.service.BlackboardService;

/**
 * CA interface to the MetricsService
 */
abstract public class MetricsWriterFacet 
    extends FacetImpl
{

    private MetricsUpdateService metricsUpdateService;

    protected MetricsWriterFacet(CoordinationArtifact owner,
				 ServiceBroker sb,
				 ConnectionSpec spec, 
				 RolePlayer player)
    {
	super(owner, sb, spec, player);
	metricsUpdateService = sb.getService(this, MetricsUpdateService.class, null);
	linkPlayer();
    }


    abstract protected String getKey(Object fact);
    abstract protected Metric getValue(Object fact);


    private void pushData(Object fact)
    {
	String key = getKey(fact);
	Metric value = getValue(fact);
	metricsUpdateService.updateValue(key, value);
    }

    @Override
   public void setupSubscriptions(BlackboardService blackboard)
    {
    }

    @Override
   public void execute(BlackboardService blackboard)
    {
    }

    @Override
   public void processFactBase(BlackboardService blackboard)
    {
	if (!factsHaveChanged()) return;
	for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
	    Object fact = frev.getFact();
	    if (frev instanceof FactAssertion) {
		pushData(fact);
	    }
	}
    }


}


