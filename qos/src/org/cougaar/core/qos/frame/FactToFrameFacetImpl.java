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

package org.cougaar.core.qos.frame;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.ca.ConnectionSpec;
import org.cougaar.core.qos.ca.CoordinationArtifact;
import org.cougaar.core.qos.ca.FacetImpl;
import org.cougaar.core.qos.ca.FactAssertion;
import org.cougaar.core.qos.ca.FactRevision;
import org.cougaar.core.qos.ca.RolePlayer;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.util.UID;


abstract public class FactToFrameFacetImpl
    extends FacetImpl
{

    private LoggingService log;
    private ServiceBroker sb;
    private FrameSet frameSet;
    private String xml_filename;

    protected FactToFrameFacetImpl(CoordinationArtifact owner,
				   ServiceBroker sb,
				   ConnectionSpec spec, 
				   RolePlayer player)
    {
	super(owner, sb, spec, player);
	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
	this.sb = sb;
	this.xml_filename = spec.role_parameters.getProperty("frame-set");
	linkPlayer();
    }


    abstract protected boolean isNewFrame(Object fact);
    abstract protected boolean isModifiedFrame(Object fact);
    abstract protected Frame getFrame(Object fact);
    abstract protected UID getUID(Object fact);
    abstract protected Collection  getModifications(Object fact);
    
    public void setupSubscriptions(BlackboardService bbs) 
    {
    }

    public void execute(BlackboardService bbs)
    {
    }


    private synchronized void ensureFrameSet(BlackboardService bbs)
    {
	if (frameSet != null) return;
	FrameSetService	fss = (FrameSetService)
	    sb.getService(this, FrameSetService.class, null);
	frameSet = fss.makeFrameSet(xml_filename, sb, bbs);
	sb.releaseService(this, FrameSetService.class, fss);
    }

    private void processNewFrame(Object fact)
    {
	Frame frame = getFrame(fact);
	String kind = frame.getKind();
	Properties props = frame.getProperties();
	frameSet.makeFrame(kind, props);
    }

    private void processModifiedFrame(Object fact)
    {
	UID uid = getUID(fact);
	Collection changes = getModifications(fact);
	Frame frame = frameSet.findFrame(uid);
	if (frame == null) return;

	Iterator itr = changes.iterator();
	while (itr.hasNext()) {
	    Frame.Change change = (Frame.Change) itr.next();
	    String attr = change.getAttribute();
	    Object val = change.getValue();
	    frame.setValue(attr, val);
	}
	
    }

    public void processFactBase(BlackboardService bbs)
    {
	if (!factsHaveChanged()) return;
	ensureFrameSet(bbs);
	for (FactRevision frev=nextFact(); frev != null; frev=nextFact()) {
	    if (log.isDebugEnabled()) 
		log.debug("Processing fact " + frev.getFact());
	    if (frev instanceof FactAssertion) {
		Object fact = frev.getFact();
		if (isNewFrame(fact)) {
		    processNewFrame(fact);
		} else if (isModifiedFrame(fact)) {
		    processModifiedFrame(fact);
		} else {
		    if (log.isWarnEnabled())
			log.warn(fact + " is neither a new frame nor a modification to an existing frame");
		}
	    } else {
		// no retractions yet
	    }
	}
    }

}
