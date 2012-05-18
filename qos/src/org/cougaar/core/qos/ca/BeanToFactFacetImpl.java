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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;


/**
 * This class is the abstract implementation of a facet creates facts
 * representing the creation or modification of Java Beans.
 * 
 */
abstract public class BeanToFactFacetImpl
    extends FacetImpl
{
    private IncrementalSubscription sub;
    private LoggingService log;

    protected BeanToFactFacetImpl(CoordinationArtifact owner,
				  ServiceBroker sb,
				  ConnectionSpec spec, 
				  RolePlayer player)
    {
	super(owner, sb, spec, player);
	log = sb.getService(this, LoggingService.class, null);
	initialize(spec);
	linkPlayer();
    }
    
    abstract protected UnaryPredicate getPredicate();
    abstract protected Object beanToFact(Object bean);
    abstract protected Object changesToFact(Object bean, Collection changes);

    protected void initialize(ConnectionSpec spec)
    {
    }

    private void do_execute(BlackboardService bbs)
    {
	if (sub == null || !sub.hasChanged()) {
	    if (log.isDebugEnabled())
		log.debug("No Bean changes");
	    return;
	}

	RolePlayer player = getPlayer();

	Enumeration en;
		
	// New Beans
	en = sub.getAddedList();
	while (en.hasMoreElements()) {
	    Object bean = en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed added "+bean);
	    }
	    Object fact = beanToFact(bean);
	    if (fact instanceof Collection) {
		Iterator itr = ((Collection) fact).iterator();
		while (itr.hasNext())  player.assertFact(itr.next());
	    } else if (fact != null) {
		player.assertFact(fact);
	    }
	}
		
		
	// Changed Beans
	en = sub.getChangedList();
	while (en.hasMoreElements()) {
	    Object bean = en.nextElement();
	    if (log.isDebugEnabled()) {
		log.debug("Observed changed "+bean);
	    }
	    Collection changes = sub.getChangeReports(bean);
	    Object fact = changesToFact(bean, changes);
	    if (fact instanceof Collection) {
		Iterator itr = ((Collection) fact).iterator();
		while (itr.hasNext()) {
		    Object change_fact = itr.next();
		    if (change_fact != null) player.assertFact(change_fact);
		}
	    } else if (fact != null) {
		player.assertFact(fact);
	    }
	}
		
	// Remove Beans.  TBD
	en = sub.getRemovedList();
	while (en.hasMoreElements()) {
	    Object bean = en.nextElement();
	    if (log.isDebugEnabled()) {			
		log.debug("Observed removed "+bean);
	    }
	}
    }

    @Override
   public void setupSubscriptions(BlackboardService bbs) 
    {
	UnaryPredicate pred = getPredicate();
	sub = (IncrementalSubscription) bbs.subscribe(pred);
	
	if (!sub.getAddedCollection().isEmpty() && log.isDebugEnabled())
	    log.debug("Subscription has initial contents");
	do_execute(bbs);
    }

    @Override
   public void execute(BlackboardService bbs)
    {
	do_execute(bbs);
    }

}
