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


/**
 * This class is the abstract implementation of the "Producer" facet
 * of the Frame CoordinationArtifact.  That is, it creates or modifies
 * {@link Frame}s, given facts.
 * 
 * @see FrameCoordinationArtifactProvider
 */
abstract public class FactToFrameFacetImpl
    extends FacetImpl
{

    private LoggingService log;
    private ServiceBroker sb;
    private FrameSet frameSet;
    private String[] xml_filenames;
    private String set_name;
    private boolean use_existing_frameset;

    protected FactToFrameFacetImpl(CoordinationArtifact owner,
				   ServiceBroker sb,
				   ConnectionSpec spec, 
				   RolePlayer player)
    {
	super(owner, sb, spec, player);
	log = sb.getService(this, LoggingService.class, null);
	this.sb = sb;
	String files = spec.ca_parameters.getProperty("files");
	set_name = spec.ca_parameters.getProperty("frame-set");
	String use_existing = 
	    spec.ca_parameters.getProperty("use-existing", "false");
	use_existing_frameset = use_existing.equalsIgnoreCase("true");
	if (files == null || set_name == null) {
	    throw new RuntimeException("No frame-sets and no frame-set-files!");
	}

	StringTokenizer tk = new StringTokenizer(files, ",");
	xml_filenames = new String[tk.countTokens()];
	int i =0;
	while (tk.hasMoreTokens()) {
	    String fname = tk.nextToken();
	    if (log.isInfoEnabled())
		log.info("FrameSet " +set_name+ " file " +fname);
	    xml_filenames[i++] = fname;
	}

	linkPlayer();
    }


    abstract protected boolean isNewFrame(Object fact);
    abstract protected boolean isModifiedFrame(Object fact);
    abstract protected DataFrame getFrame(Object fact);
    abstract protected Object getModifications(Object fact);
    
    @Override
   public void setupSubscriptions(BlackboardService bbs) 
    {
    }

    @Override
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

	FrameSetService fss = sb.getService(this, FrameSetService.class, null);
	if (use_existing_frameset) {
	    frameSet = fss.findFrameSet(set_name, null);
	    if (frameSet != null) {
		FrameSetParser fsp = new FrameSetParser(sb, bbs);
		for (int i=0; i<xml_filenames.length; i++)
		    fsp.parseFrameSetFile(set_name, xml_filenames[i] ,frameSet);
	    }
	} else {
	    frameSet = fss.loadFrameSet(set_name, xml_filenames, sb, bbs);
	}
    }

    private void processNewFrame(Object fact)
    {
	DataFrame frame = getFrame(fact);
	frame.addToFrameSet(frameSet);
    }

    private void handleChange(Frame.Change change)
    {
	UID uid = change.getFrameUID();
	Frame frame = frameSet.findFrame(uid);
	if (frame == null) {
	    if (log.isInfoEnabled())
		log.info("No match for uid " +uid);
	    return;
	}

	String attr = change.getSlotName();
	Object val = change.getValue();
	if (log.isInfoEnabled())
	    log.info("Changing " +attr+ " to " +val);
	frame.setValue(attr, val);
    }

    private void processModifiedFrame(Object fact)
    {
	Object mods = getModifications(fact);

	if (mods instanceof Collection) {
	    Collection changes = (Collection) mods;
	    if (log.isInfoEnabled())
		log.info("Processing " + changes.size() + " changes");

	    Iterator itr = changes.iterator();
	    while (itr.hasNext()) {
		Frame.Change change = (Frame.Change) itr.next();
		handleChange(change);
	    }
	} else if (mods instanceof Frame.Change) {
	    Frame.Change change = (Frame.Change) mods;
	    handleChange(change);
	} else {
	    if (log.isWarnEnabled())
		log.warn("Frame Change " +mods+ 
			 " is neither a Collection nor a Frame.Change");
	}
	
    }

    @Override
   public void processFactBase(BlackboardService bbs)
    {
	if (!factsHaveChanged()) return;
	ensureFrameSet(bbs);
	if (frameSet == null) return;
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
