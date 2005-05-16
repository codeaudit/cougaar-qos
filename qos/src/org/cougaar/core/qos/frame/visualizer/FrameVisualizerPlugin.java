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
import java.util.*;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.*;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.qos.frame.visualizer.util.FrameChanges;

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


    FrameModel frameModel;
    boolean newFramesPresent;

    //static String TICK_EVENT_LABEL = "TICK";
    //protected int tickNumber;



    public void load() {
        super.load();
        ServiceBroker sb = getServiceBroker();
        log = (LoggingService)
                sb.getService(this, LoggingService.class, null);
    }

    protected void setupSubscriptions()  {
        BlackboardService bbs = getBlackboardService();
        if (log.isDebugEnabled())
            log.debug("FrameSet name is " + frameSetName);

        sub = (IncrementalSubscription)
                bbs.subscribe(framePred);

        if (!sub.getAddedCollection().isEmpty() && log.isDebugEnabled())
            log.debug("Subscription has initial contents");
        do_execute(bbs);
    }

    public void start() {
        frameSetName = (String) getParameter("frame-set");
        frameCache = new ArrayList();
        newFramesPresent = false;

        String specFileName = (String) getParameter("spec-file");
        File xml_file = ConfigFinder.getInstance().locateFile(specFileName);
        frameModel = new FrameModel();
        pluginDisplay = new DisplayWindow(frameModel, xml_file);

        //SwingUtilities.invokeLater(new CreateWindowHelper(xml_file));
        super.start();
    }


    protected void execute() {
        BlackboardService bbs = getBlackboardService();
        do_execute(bbs);
    }

    private void do_execute(BlackboardService bbs) {
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
        if (en.hasMoreElements()) {
            if (log.isDebugEnabled())
                log.debug("There are new frames.");
            processNewFrames(en);
        }

        // Changed Frames
        en = sub.getChangedList();
        if (en.hasMoreElements()) {
            if (log.isDebugEnabled())
                log.debug("There are changed frames.");
            processChangedFrames(en, sub);
        }
        // Remove Frames.  Won't happen.
        en = sub.getRemovedList();
        if (en.hasMoreElements()) {
            if (log.isDebugEnabled())
                log.debug("There are removed frames (ignored).");
            processRemovedFrames(en);
        }
    }


    private void processNewFrames(Enumeration en) {
        FrameSet frameSet = null;
        ArrayList added = new ArrayList();
        while (en.hasMoreElements()) {
            Frame frame = (Frame) en.nextElement();
            if (frameSet == null)
                frameSet = frame.getFrameSet();
            if (log.isDebugEnabled()) {
                log.debug("Observed added "+frame);
            }
            // Handle new Frame
            added.add(frame);
            frameCache.add(frame);
            newFramesPresent = true;
        }

        if (newFramesPresent) {
            if (!frameModel.hasFrameSet())
                frameModel.setFrameSet(frameSet);

             frameModel.framesAdded(added);
            //pluginDisplay.setFrameHelper(frameModel);
            newFramesPresent = false;
        }
    }


    private void processChangedFrames(Enumeration en, IncrementalSubscription sub) {
        HashMap changes = new HashMap();
        Collection ch;
        while (en.hasMoreElements()) {
            Frame frame = (Frame) en.nextElement();
            if (log.isDebugEnabled()) {
                log.debug("Observed changed "+frame);
            }

            ch = sub.getChangeReports(frame);
            if (changes.get(frame) != null)
                throw new IllegalArgumentException("already created a change for this frame: "+frame);
            changes.put(frame, ch);
        }
        if (log.isDebugEnabled())
                log.debug("Calling frameModel.framesChanged");
        frameModel.framesChanged(changes);
    }


    private void processRemovedFrames(Enumeration en) {
        while (en.hasMoreElements()) {
            Frame frame = (Frame) en.nextElement();
            if (log.isDebugEnabled()) {
                log.debug("Observed removed "+frame);
            }
        }
    }


}
