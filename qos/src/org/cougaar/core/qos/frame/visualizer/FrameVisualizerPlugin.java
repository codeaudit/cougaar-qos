/*
 * <copyright>
 *  
 *  Copyright 1997-2005 BBNT Solutions, LLC
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

package org.cougaar.core.qos.frame.visualizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.*;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.qos.frame.visualizer.test.TickEvent;

import javax.swing.*;


public class FrameVisualizerPlugin
    extends ParameterizedPlugin
{
    private UnaryPredicate framePred = new UnaryPredicate() {
	    public boolean execute(Object o) {
	       return ((o instanceof DataFrame) &&
		    ((DataFrame) o).getFrameSet().getName().equals(frameSetName)) ;
	    }
	};
    private IncrementalSubscription sub;
    private LoggingService log;
    private String frameSetName;
    private DisplayWindow pluginDisplay;
    private ArrayList frameCache;


    FrameHelper helper;
    boolean newFramesPresent;
    static String TICK_EVENT_LABEL = "TICK";
    int tickNumber = 0;



    public void load()
    {
	super.load();

	ServiceBroker sb = getServiceBroker();

	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
    }

    public void start()
    {
	frameSetName = (String) getParameter("frame-set");
	
	frameCache = new ArrayList();
	newFramesPresent = false;
	
	String specFileName = (String) getParameter("spec-file");
	File xml_file = ConfigFinder.getInstance().locateFile(specFileName);
	pluginDisplay = new DisplayWindow(xml_file);
	//SwingUtilities.invokeLater(new CreateWindowHelper(xml_file));  
	super.start();
    }

    class CreateWindowHelper implements Runnable {
        File xmlFile;
        public CreateWindowHelper(File xmlFile){
            CreateWindowHelper.this.xmlFile = xmlFile;
        }
        public void run() {
            pluginDisplay = new DisplayWindow(xmlFile);
        }
    }

   /* class SetFrameHelper implements Runnable {
        FrameHelper fh;
        public SetFrameHelper(FrameHelper helper){
            fh = helper;
        }
        public void run() {
            pluginDisplay.setFrameHelper(fh);
        }
    }*/

    private void do_execute(BlackboardService bbs)
    {
	if (!sub.hasChanged()) {
	    if (log.isDebugEnabled())
		log.debug("No Frame changes");
	    return;
	}
	if (log.isDebugEnabled())
	    log.debug("There are changes.");
	Enumeration en;
		
	// New Frames
	en = sub.getAddedList();
	FrameSet frameSet = null;
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (frameSet == null)
		frameSet = frame.getFrameSet();
	    if (log.isDebugEnabled()) {
		log.debug("Observed added "+frame);
	    }
	    // Handle new Frame
	    frameCache.add(frame);
	    newFramesPresent = true;
	}
	
	if (helper == null || newFramesPresent) {
	    if (log.isDebugEnabled()) 
		log.debug("setting frames on the display");
	    helper = new FrameHelper(frameCache, frameSet);
	    pluginDisplay.setFrameHelper(helper);
	    newFramesPresent = false;
	}
		
		
	// Changed Frames
	en = sub.getChangedList();
	ArrayList transitions = new ArrayList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {
		    log.debug("Observed changed "+frame);
	    }
	    Collection changes = sub.getChangeReports(frame);
	    // A collection of Frame.Change instances.
	    if (changes != null) {
		if (frame instanceof RelationFrame) //frame.isa("relationship"))  
		    transitions.addAll(processRelationshipChanges((RelationFrame)frame, changes.iterator()));
	    } else {
            ShapeGraphic sh = pluginDisplay.findShape(frame);
            if (log.isDebugEnabled())
		        log.debug("Observed changed "+frame+ "  container="+sh);

            if (sh != null) {
                for (Iterator ii=changes.iterator(); ii.hasNext();)
                    sh.processFrameChange(frame, (Frame.Change) ii.next());
            }
        }
	}
	if (transitions.size() > 0) {
	    //pluginDisplay.updateFrameView();
	    pluginDisplay.tickEventOccured(new TickEvent(this, tickNumber++, TICK_EVENT_LABEL, transitions));
    }


	// Remove Frames.  Won't happen.
	en = sub.getRemovedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    if (log.isDebugEnabled()) {			
		log.debug("Observed removed "+frame);
	    }
	}
    }




    protected Collection processRelationshipChanges(RelationFrame frame, Iterator changes) {
        String slotName;
        Object value;
        ShapeGraphic child, parent;
        Frame fch, fp;
        ArrayList transitions = new ArrayList();

        while (changes.hasNext()) {
            Frame.Change change = (Frame.Change) changes.next();
            // Handle change to existing frame
            slotName = change.getSlotName();
            value    = change.getValue();

            if (log.isDebugEnabled())
                log.debug("frame "+frame+"  changed   slot="+slotName+"  value="+value+" child="+frame.getValue("child-value"));

            fp = frame.relationshipParent();
            fch  = frame.relationshipChild();

            if (log.isDebugEnabled())
                log.debug("processRelationshipChanges parentFrame="+fp+"  childFrame="+fch);

            parent = pluginDisplay.findShape(fp);
            child  = pluginDisplay.findShape(fch);
            if (parent == null || child == null) {
                if (log.isDebugEnabled())
                    log.debug("did not find shapes");
                continue;
            }
            if (child.getParent() == null || child.getParent().getId().equals(parent.getId())) {
                if (log.isDebugEnabled())
                    log.debug("error: old parent = "+child.getParent()+" new parent="+parent+", ignoring...");
                continue;
            }
	    //log.debug("parent="+parent.getClass().getName());
	    //log.debug("child ="+child.getClass().getName());
            transitions.add(new Transition(child, child.getParent(), (ShapeContainer)parent));
        }
        return transitions;
    } 

    
    protected void execute()
    {
        BlackboardService bbs = getBlackboardService();
        do_execute(bbs);
    }

    protected void setupSubscriptions() 
    {
	BlackboardService bbs = getBlackboardService();
	if (log.isDebugEnabled())
	    log.debug("FrameSet name is " + frameSetName);

	sub = (IncrementalSubscription)
	    bbs.subscribe(framePred);
	
	if (!sub.getAddedCollection().isEmpty() && log.isDebugEnabled())
	    log.debug("Subscription has initial contents");
	do_execute(bbs);

    }
    


}
