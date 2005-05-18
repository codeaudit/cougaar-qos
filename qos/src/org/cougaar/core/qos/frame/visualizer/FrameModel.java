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
import org.cougaar.core.qos.frame.visualizer.test.ContainsPredicate;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.visualizer.event.*;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;





public class FrameModel {
    protected static int sid=0;
    protected int internalid = sid++;
    protected FrameSet frameSet;
    protected Collection allFrames;
    protected HashSet dataFrames;
    protected HashSet relationshipFrames;
    // keep track of all shape graphic objects that have an associated Frame object (prototypes and grouping containers
    // are not currently kept track of
    private HashMap graphicsMap;
    // relationship frames that have child == null || parent == null
    protected HashSet pendingRelationshipFrames;


    //protected HashMap frameMap;
    //private Object lock = new Object();

    private transient Logger log = Logging.getLogger(getClass().getName());

    //DisplayWindow displayWindow;
    //static String TICK_EVENT_LABEL = "TICK";
    //protected int tickNumber;

    ChangeModel addedFramesListeners;
    ChangeModel changedFramesListeners;
    ChangeModel removedFramesListeners;
    //ChangeModel transitionListeners;
    ChangeModel graphicChangeListeners;

    public FrameModel() {
        //tickNumber = 0;
        relationshipFrames = new HashSet();
        pendingRelationshipFrames = new HashSet();
        dataFrames = new HashSet();
        allFrames = new HashSet();
        graphicsMap = new HashMap();

        addedFramesListeners = new ChangeModel();
        changedFramesListeners = new ChangeModel();
        removedFramesListeners = new ChangeModel();
        //transitionListeners = new ChangeModel();
        graphicChangeListeners = new ChangeModel();

        // views
        //this.displayWindow = null;
        //this.containerView = null;
        //this.frameTreeView = null;
    }



    public synchronized void addAddedFramesListener(ChangeListener l) {
        addedFramesListeners.addListener(l);
    }
    public synchronized void removeAddedFramesListener(ChangeListener l) {
        addedFramesListeners.removeListener(l);
    }
    public synchronized void addChangedFramesListener(ChangeListener l) {
        changedFramesListeners.addListener(l);
    }
    public synchronized void removeChangedFramesListener(ChangeListener l) {
        changedFramesListeners.removeListener(l);
    }
    public synchronized void addRemovedFramesListener(ChangeListener l) {
        removedFramesListeners.addListener(l);
    }
    public synchronized void removeRemovedFramesListener(ChangeListener l) {
        removedFramesListeners.addListener(l);
    }
    /*public synchronized void addTransitionListener(ChangeListener l) {
        transitionListeners.addListener(l);
    }
    public synchronized void removeTransitionListener(ChangeListener l) {
        transitionListeners.removeListener(l);
    }*/
    public synchronized void addGraphicChangeListener(ChangeListener l) {
        graphicChangeListeners.addListener(l);
    }
    public synchronized void removeGraphicChangeListener(ChangeListener l) {
        graphicChangeListeners.removeListener(l);
    }

    protected synchronized void notifyAddedFrames(AddedFramesEvent e) {
        addedFramesListeners.notifyListeners(e);
        //SwingUtilities.invokeLater(new NotifyHelper(addedFramesListeners, e));
    }
    protected synchronized void notifyChangedFrames(ChangedFramesEvent e) {
        changedFramesListeners.notifyListeners(e);
        //SwingUtilities.invokeLater(new NotifyHelper(changedFramesListeners, e));
    }
    protected synchronized void notifyRemovedFrames(RemovedFramesEvent e) {
        removedFramesListeners.notifyListeners(e);
        //SwingUtilities.invokeLater(new NotifyHelper(removedFramesListeners, e));
    }
    //protected synchronized void notifyNewTransitions(TickEvent e) {
    //    transitionListeners.notifyListeners(e);
    //    //SwingUtilities.invokeLater(new NotifyHelper(transitionListeners, e));
    //}

    public synchronized void fireContainerAddedChild(ShapeContainer c, ShapeGraphic newchild) {
         graphicChangeListeners.notifyListeners(new ContainerChildAddedEvent(this, c, newchild));
    }

    public synchronized void fireContainerRemovedChild(ShapeContainer c, ShapeGraphic removedchild) {
         graphicChangeListeners.notifyListeners(new ContainerChildRemovedEvent(this, c, removedchild));
    }

   /*
    class NotifyHelper implements Runnable {
        ChangeModel changes;
        ChangeEvent event;
        public NotifyHelper(ChangeModel m, ChangeEvent e) {
            changes = m;
            event = e;
        }
        public void run() {
            changes.notifyListeners(event);
        }
    }*/



    public synchronized boolean registerGraphic(org.cougaar.core.qos.frame.Frame f, ShapeGraphic graphic) {
        if (f == null || graphic == null)
            return false;
        ShapeGraphic sg;
        if ((sg=(ShapeGraphic)graphicsMap.get(f)) != null) {
            if (sg != graphic)
                throw new IllegalArgumentException("Error: "+graphic+" is already registered for frame "+f);
        }
        graphicsMap.put(f, graphic);
        return true;
    }

    public synchronized void unregisterGraphic(org.cougaar.core.qos.frame.Frame f, ShapeGraphic graphic) {
        if (f == null || graphic == null)
            return;
         if (graphicsMap.get(f) != null)
             graphicsMap.remove(f);
    }

    public ShapeGraphic getGraphic(org.cougaar.core.qos.frame.Frame f) {
        //synchronized (lock) {
            return (ShapeGraphic) graphicsMap.get(f);
        //}
    }

    public boolean hasFrameSet() {
        return frameSet != null;
    }

    //public synchronized void setDisplayWindow(DisplayWindow displayWindow) {
    //    this.displayWindow = displayWindow;
    //}

    public synchronized void setFrameSet(FrameSet frameSet) {
        this.frameSet = frameSet;
    }

    public synchronized void framesAdded(Collection addedFrames) {
        if (log.isDebugEnabled())
            log.debug("addFrames("+internalid+"): initialized with "+addedFrames.size()+" frames and frameset="+frameSet.getName());
        this.allFrames.addAll(addedFrames);
        process(addedFrames);
    }

    public synchronized void framesRemoved(Collection removedFrames) {
        if (log.isDebugEnabled())
            log.debug("removeFrames("+internalid+"): initialized with "+removedFrames.size()+" frames and frameset="+frameSet.getName());

        allFrames.removeAll(removedFrames);
    }

    public synchronized void framesChanged(HashMap changes) {
        processChanges(changes);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Data exporation methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getFrameSetName() {
        return frameSet.getName();
    }

    public synchronized Collection getAllFrames() {
        return new ArrayList(allFrames);
    }

    public synchronized Collection getRelationshipFrames() {
        return relationshipFrames;
    }

    public synchronized Collection getDataFrames() {
        return dataFrames;
    }

    public org.cougaar.core.qos.frame.Frame findFrame(FramePredicate predicate) {
        return findFrame(allFrames, predicate);
    }

    public synchronized org.cougaar.core.qos.frame.Frame findFrame(Collection frames, FramePredicate predicate) {
        //if (predicate != null)
           // predicate.setFrameSetName((frameSet != null ? frameSet.getName() : null));
        if (log.isDebugEnabled())
            log.debug("findFrame(:"+internalid+"  frames["+frames.size()+"], predicate="+predicate+")");
        org.cougaar.core.qos.frame.Frame f;
        for (Iterator ii = frames.iterator(); ii.hasNext(); ) {
            f = (org.cougaar.core.qos.frame.Frame) ii.next();
            if (predicate.execute(f)) {
                //if (log.isDebugEnabled())
                //  log.debug(":"+internalid+" found frame=  "+f);
                return f;
            }
        }
        return null;
    }

    public synchronized RelationFrame findRelation(org.cougaar.core.qos.frame.DataFrame parent, org.cougaar.core.qos.frame.DataFrame child, String relationName) {
        return findRelation(relationshipFrames, parent, child, relationName);
    }
    public synchronized RelationFrame findRelation(Collection relationFrames, org.cougaar.core.qos.frame.DataFrame parent, org.cougaar.core.qos.frame.DataFrame child, String relationName) {
        Collection relations = findRelations(relationFrames, parent, child);
        String name;
        RelationFrame rf;
        for (Iterator ii=relations.iterator(); ii.hasNext();) {
            rf = (RelationFrame) ii.next();
            if (rf.getKind().equals(relationName))
                return rf;
        }
        return null;
    }

    public synchronized Collection findRelations(org.cougaar.core.qos.frame.DataFrame parent, org.cougaar.core.qos.frame.DataFrame child) {
        return findRelations(relationshipFrames, parent, child);
    }
    public synchronized Collection findRelations(Collection relationFrames, org.cougaar.core.qos.frame.Frame parent, org.cougaar.core.qos.frame.Frame child) {
        ArrayList result = new ArrayList();
        RelationFrame rf;
        for (Iterator ii=relationFrames.iterator(); ii.hasNext();) {
            rf = (RelationFrame) ii.next();
            if (rf.relationshipParent() == parent && rf.relationshipChild() == child)
                result.add(rf);
        }
        return result;
    }


    public synchronized Collection findFrames(FramePredicate predicate) {
        return findFrames(allFrames, predicate);
    }

    public synchronized Collection findFrames(Collection frames, FramePredicate predicate) {
        //if (predicate != null)
         //   predicate.setFrameSetName((frameSet != null ? frameSet.getName() : null));


        ArrayList result = new ArrayList();
        org.cougaar.core.qos.frame.Frame f;
        for (Iterator ii = frames.iterator(); ii.hasNext(); ) {
            f = (org.cougaar.core.qos.frame.Frame) ii.next();
            if (predicate.execute(f))
                result.add(f);
        }
        //if (log.isDebugEnabled())
          //  printResult(result);
        if (log.isDebugEnabled())
            log.debug("findFrames(:"+internalid+" frames["+frames.size()+"], predicate="+predicate+"):  found "+result.size()+" frames");
        return result;
    }


    protected void printResult(Collection result) {
        if (log.isDebugEnabled() && result.size()> 0) {
            StringBuffer sb = new StringBuffer("found: \n");
            for (Iterator jj=result.iterator(); jj.hasNext();) {
                sb.append(jj.next().toString());
                sb.append("\n");
            }
        } else if (log.isDebugEnabled() && result.size()==0)
            log.debug("found no matches");
    }


    public synchronized Collection getAllChildren(org.cougaar.core.qos.frame.Frame parent, String relationship) {
        Set s = frameSet.findRelations(parent, "child", relationship);
        if (log.isDebugEnabled())
            log.debug("getAllChildren(:"+internalid+" parent="+parent+" relationship="+relationship+"):  found "+s.size()+" children");
        return  s;
    }

    public synchronized Collection getAllChildren(org.cougaar.core.qos.frame.Frame parent, Collection frames, String relationship) {
        Set s = frameSet.findRelations(parent, "child", relationship);
        if (log.isDebugEnabled())
            log.debug("getAllChildren(:"+internalid+" parent="+parent+" relationship="+relationship+"):  found "+s.size()+" children");
        return  s;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Incoming event processing and notification
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected boolean isValid(RelationFrame rframe) {
        return (rframe.relationshipChild() != null && rframe.relationshipParent() != null);
    }

    protected void process(Collection frames) {
        org.cougaar.core.qos.frame.Frame f;
        RelationFrame rf;
        HashSet newRelationFrames=null, newDataFrames=null;

        for (Iterator ii=frames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.Frame) ii.next();
            if (f instanceof RelationFrame) {
                rf = (RelationFrame) f;
                if (!isValid(rf)) {
                    pendingRelationshipFrames.add(f);
                    if (log.isDebugEnabled())
                        log.debug("process(:"+internalid+" added relationFrame ("+rf.getKind()+") to pendingFrames size="+pendingRelationshipFrames.size());
                } else {
                    relationshipFrames.add(f);
                    if (newRelationFrames == null)
                        newRelationFrames = new HashSet(frames.size());
                    newRelationFrames.add(f);
                }
            } else {
                dataFrames.add(f);
                if (newDataFrames == null)
                    newDataFrames = new HashSet(frames.size());
                newDataFrames.add(f);
            }
        }

        HashSet moreNewRelations = checkPendingRelations();
        if (moreNewRelations != null && moreNewRelations.size() > 0) {
            if (newRelationFrames != null)
                newRelationFrames.addAll(moreNewRelations);
            else newRelationFrames = moreNewRelations;
        }

        notifyAddedFrames(new AddedFramesEvent(this, newDataFrames, newRelationFrames));
    }


    // check all relation frames and see if they are valid (parent and child are non null)
    // if so, remove from the pending list and return in a collection
    protected HashSet checkPendingRelations() {
        HashSet validRelationFrames = null;
        org.cougaar.core.qos.frame.RelationFrame f;

        for (Iterator ii=pendingRelationshipFrames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.RelationFrame) ii.next();
            if (f.relationshipChild() != null && f.relationshipParent() != null) {
                //pendingRelationshipFrames.remove(f);
                if (validRelationFrames == null)
                    validRelationFrames = new HashSet(pendingRelationshipFrames.size());
                validRelationFrames.add(f);
            }
        }
        if (validRelationFrames != null) {
            for (Iterator ii=validRelationFrames.iterator(); ii.hasNext();)
                pendingRelationshipFrames.remove(ii.next());
        }
        return validRelationFrames;
    }


    protected void processChanges(HashMap changeMap) {
        // A collection of Frame.Change instances.
        ArrayList transitions = new ArrayList();
        org.cougaar.core.qos.frame.Frame frame;
        Collection changeReports, trans;

        HashMap changedDataFrames = null; //new HashMap();
        HashMap changedRelationFrames = null; //new HashMap();
        RelationFrame rf;

        // if any of the pending relations became valid since the last time we checked, process
        // them as new frames
        Collection validRelations = checkPendingRelations();
        if (validRelations != null && validRelations.size() > 0)
            process(validRelations);
        for (Iterator ii=changeMap.keySet().iterator(); ii.hasNext(); ) {
            frame = (org.cougaar.core.qos.frame.Frame) ii.next();
            changeReports = (Collection) changeMap.get(frame);


            if (frame instanceof RelationFrame) {
                rf = (RelationFrame) frame;
                // if a relation frame gets changed and the relationship points to a frame that has not been added
                // add this relation to the pending relations
                if (!isValid(rf)) {
                    pendingRelationshipFrames.add(rf);
                    if (log.isDebugEnabled())
                        log.debug("processChanges(:"+internalid+" added relationFrame ("+rf.getKind()+") to pendingFrames size="+pendingRelationshipFrames.size());
                } else {
                    if (log.isDebugEnabled())
                        log.debug("processChanges:  frame="+frame.getKind()+" "+frame+"  changes.size="+changeReports.size());
                    // if this is a relationship change, create transition that will animate moving a
                    // child from one parent to another
                    if (changeReports != null && changeReports.size() > 0) {
                        //trans = processRelationshipChanges((RelationFrame)frame, changeReports);
                        //if (trans != null)
                        //     transitions.addAll(trans);

                        if (changedRelationFrames == null)
                            changedRelationFrames = new HashMap();
                        changedRelationFrames.put(frame, changeReports);
                    }
                }
            } else {
                if (changeReports != null && changeReports.size() > 0) {
                    if (changedDataFrames == null)
                        changedDataFrames = new HashMap();
                    changedDataFrames.put(frame, changeReports);
                }

                // if this is not a relationship change (we currently only animate the relationship changes)
                // than trigger the container's  slot listeners (if any)
                //ShapeGraphic sh = getGraphic(frame);
                //if (log.isDebugEnabled())
                //    log.debug("Observed changed "+frame+ "  container="+ (sh==null?"NULL!!!":sh.toString()));
                //SwingUtilities.invokeLater(new SetChangeHelper(frame, sh, changeReports));
            }
        }

        //if (transitions.size() > 0)
        //    notifyNewTransitions(new org.cougaar.core.qos.frame.visualizer.event.TickEvent(this, tickNumber++, TICK_EVENT_LABEL, transitions));
            //displayWindow.tickEventOccured(new org.cougaar.core.qos.frame.visualizer.event.TickEvent(this, tickNumber++, TICK_EVENT_LABEL, transitions));

        if (changedDataFrames != null || changedRelationFrames != null)
            notifyChangedFrames(new ChangedFramesEvent(this, changedDataFrames, changedRelationFrames));
    }


    public static String getName(org.cougaar.core.qos.frame.Frame frame) {
        return (frame != null ? (String)frame.getValue("name") : null);
    }


    /*
    protected Transition processRelationshipFrame(RelationFrame rframe) {
        ShapeGraphic child, parent;
        Frame fchild, fparent;

        fparent = rframe.relationshipParent();
        fchild  = rframe.relationshipChild();
        if (fparent == null || fchild == null) {
            if (log.isDebugEnabled())
                log.debug("processRelationshipChange: invalid Relation '"+rframe.getKind()+"'  parent="+getName(fparent)+" child="+getName(fchild));
        }

        parent = getGraphic(fparent);
        child  = getGraphic(fchild);
        if (parent == null || child == null) {
            if (log.isDebugEnabled())
                log.debug("--error: did not find shapes: Relation '"+rframe.getKind()+"' changed   parent="+(fparent==null?"+NULL+":rframe.getParentValue())
                    +"  child="+(fchild==null?"+NULL+":rframe.getChildValue())+" parentFrame="+fparent+" childFrame="+fchild);
            return null;
        }
        if (child.getParent() != null && child.getParent().getId().equals(parent.getId())) {
            if (log.isDebugEnabled())
                log.debug("--warning: old parent = "+child.getParent()+" new parent="+parent+", ignoring...");
            return null;
        }
        if (log.isDebugEnabled())
            log.debug("Relation '"+rframe.getKind()+"' changed   parent="+(fparent==null?"+NULL+":rframe.getParentValue())
                    +"  child="+(fchild==null?"+NULL+":rframe.getChildValue()));//+" parentFrame="+fparent+" childFrame="+fchild);


        return new Transition(child, child.getParent(), (ShapeContainer)parent);
    }

     protected Collection processRelationshipChanges(RelationFrame rframe, Collection changeReports) {
        ShapeGraphic child, parent;
        Frame fchild, fparent;
        ArrayList transitions = null; //new ArrayList();

        if (log.isDebugEnabled())
            log.debug("processRelationshipChanges: frame="+rframe.getKind()+" changeReports.size="+changeReports.size());

        Transition t;
        for (Iterator ii=changeReports.iterator(); ii.hasNext();) {
            Frame.Change change = (Frame.Change) ii.next();
            // Handle change to existing frame
            //slotName = change.getSlotName();
            //value    = change.getValue();
            t = processRelationshipFrame(rframe);
            if (t!=null) {
                if (transitions == null)
                    transitions = new ArrayList();
                transitions.add(t);
            }
        }
        return transitions;
    }
    */

    public String toString() {
        return "FrameModel("+internalid+"):  frameset='"+getFrameSetName()+"' has "+allFrames.size()+" frames, "+dataFrames.size()+" data frames";
    }
}
