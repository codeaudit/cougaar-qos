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
import java.util.StringTokenizer;

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
    private String[] xml_filenames;

    protected FactToFrameFacetImpl(CoordinationArtifact owner,
				   ServiceBroker sb,
				   ConnectionSpec spec, 
				   RolePlayer player)
    {
	super(owner, sb, spec, player);
	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
	this.sb = sb;
	String files = spec.ca_parameters.getProperty("frame-set-files");
	if (files != null) {
	    StringTokenizer tk = new StringTokenizer(files, ",");
	    xml_filenames = new String[tk.countTokens()];
	    int i =0;
	    while (tk.hasMoreTokens()) xml_filenames[i++] = tk.nextToken();
	    linkPlayer();
	} else {
	    throw new RuntimeException("No frame-set-files !");
	}
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


    // Make the FrameSet available to subclasses
    protected FrameSet getFrameSet()
    {
	return frameSet;
    }

    private synchronized void ensureFrameSet(BlackboardService bbs)
    {
	if (frameSet != null) return;
	if (xml_filenames == null) return;

	FrameSetService	fss = (FrameSetService)
	    sb.getService(this, FrameSetService.class, null);
	frameSet = fss.loadFrameSet(xml_filenames, sb, bbs);
	sb.releaseService(this, FrameSetService.class, fss);
    }

    private void processNewFrame(Object fact)
    {
	Frame frame = getFrame(fact);
	frame.copyToFrameSet(frameSet);
    }

    private void processModifiedFrame(Object fact)
    {
	UID uid = getUID(fact);
	Collection changes = getModifications(fact);
	Frame frame = frameSet.findFrame(uid);
	if (frame == null) {
	    if (log.isInfoEnabled())
		log.info("No match for uid " +uid);
	    return;
	}
	if (log.isInfoEnabled())
	    log.info("Processing " + changes.size() + " changes");

	Iterator itr = changes.iterator();
	while (itr.hasNext()) {
	    Frame.Change change = (Frame.Change) itr.next();
	    String attr = change.getSlot();
	    Object val = change.getValue();
	    if (log.isInfoEnabled())
		log.info("Changing " +attr+ " to " +val);
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
		    // subclass is using it for some other purpose
		}
	    } else {
		// no retractions yet
	    }
	}
	frameSet.processQueue();
    }

}
