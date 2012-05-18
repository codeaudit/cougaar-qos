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
import org.cougaar.core.service.BlackboardService;

/**
 * Does the main work of the Alarm Coordinaton Artifact.  In
 * particular, it schedules cougaar Alarms with the
 * AlarmService. Subclasses are responsible for creating the Alarm.
 */
abstract public class SleeperFacet 
    extends FacetImpl
{
    protected SleeperFacet(CoordinationArtifact owner,
			   ServiceBroker sb,
			   ConnectionSpec spec, 
			   RolePlayer player)
    {
	super(owner, sb, spec, player);
    }

    @Override
   public void setupSubscriptions(BlackboardService blackboard)
    {
    }

    @Override
   public void execute(BlackboardService blackboard)
    {
    }

    protected abstract void processFactAssertion(Object fact);

    @Override
   public void processFactBase(BlackboardService blackboard)
    {
	if (!factsHaveChanged()) return;
	for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
	    if (frev instanceof FactAssertion) {
		Object fact = frev.getFact();
		processFactAssertion(fact);
	    } else {
		// cancel the alarm?
	    }
	}
    }


}


