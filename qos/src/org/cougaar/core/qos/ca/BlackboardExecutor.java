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

import org.cougaar.core.service.BlackboardService;

/**
 * This interface is the common base of {@link Receptacle}s and {@link
 * Facet}s.  The processing of these two interfaces are identical, but
 * the accessibility is not: the methods here will be invoked on a
 * Facet by a {@link CoordinationArtifact}, with its Blackboard, while
 * the methods will be invoked on a Receptacle by a {@link
 * RolePlayer}, with its Blackboard.  Note that, unlike 'Facet' and
 * 'Receptacle', this interface is <i>not</i> analagous to the
 * 'Executor' entity in the CORBA Component Model,  But it seemed like
 * a good name anyway.
 */
public interface BlackboardExecutor
{
    /**
     * Process any queued facts.  This should be run in a blackboard
     * transaction.
    */
    public void processFactBase(BlackboardService blackboard);


    /**
     * Handle subscription updates. This should be run in a blackboard
     * transaction. 
     */
    public void execute(BlackboardService blackboard);


    /**
     * Handle blackboard subscriptions.  This should be run in a
     * blackboard transaction.
    */
    public void setupSubscriptions(BlackboardService blackboard);

}
