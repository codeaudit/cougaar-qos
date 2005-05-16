package org.cougaar.core.qos.frame.visualizer;


import org.cougaar.core.qos.frame.visualizer.util.XMLParser;
import org.cougaar.core.qos.frame.visualizer.util.ViewConfigParser;
import org.cougaar.core.qos.frame.visualizer.util.SlotChangeListeners;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;
import org.cougaar.core.qos.frame.visualizer.event.*;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseEvent;


import org.xml.sax.Attributes;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 8, 2005
 * Time: 9:31:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class Display extends AnimatedCanvas implements ChangeListener {
    public static boolean ENABLE_ANIMATION = false;//true;

    Shapes shapes;
    LabelRenderers labelRenderers;
    ShapeRenderers shapeRenderers;
    SlotChangeListeners slotListeners;

    ShapeContainer root;
    boolean initialized, processingTickEvent;
    ArrayList transitions, tickEventQueue;

    // debug
    Collection frames;
    HashMap frameContainerMap, prototypeMap;
    FrameModel frameModel;
    private transient Logger log = Logging.getLogger(getClass().getName());

    ViewConfigParser.WindowSpec wSpec;
    ControlPanel cPanel;

    // changes
    org.cougaar.core.qos.frame.visualizer.event.ChangeModel tickStatusListeners;
    ChangeEvent tickCompleted;
    Object lock;




    public Display(FrameModel frameModel, File xmlFile) {
        lock = new Object();
        this.frameModel = frameModel;

        //graphics = new HashMap();
        transitions = new ArrayList();
        tickEventQueue = new ArrayList();
        frameContainerMap = new HashMap();
        prototypeMap = new HashMap();
        processingTickEvent = initialized = false;
        tickStatusListeners = new org.cougaar.core.qos.frame.visualizer.event.ChangeModel();
        tickCompleted = new ChangeEvent(this);
        ViewConfigParser parser = new ViewConfigParser();
        parser.parse(xmlFile);
        wSpec = parser.windowSpec;
        shapes = parser.getShapes();
        labelRenderers = parser.getLabelRenderers();
        shapeRenderers = parser.getShapeRenderers();
        slotListeners  = parser.getSlotListeners();

        root = parser.root;
        initialized = (root != null);
        if (initialized) {
            if (animating != null)
                animating.start();
        }
        frameModel.addAddedFramesListener(this);
        frameModel.addChangedFramesListener(this);
        frameModel.addRemovedFramesListener(this);
        frameModel.addTransitionListener(this);
    }

    public Shapes getShapes() {
        return shapes;
    }
    public LabelRenderers getLabelRenderers() {
        return labelRenderers;
    }
    public ShapeRenderers getShapeRenderers() {
        return shapeRenderers;
    }
    public SlotChangeListeners getSlotListeners() {
        return slotListeners;
    }


    public void stateChanged(ChangeEvent e) {
        ProcessEventHelper helper = new ProcessEventHelper(e);
        if (SwingUtilities.isEventDispatchThread())
            helper.run();
        else SwingUtilities.invokeLater(helper);
    }

    class ProcessEventHelper implements Runnable {
        ChangeEvent e;
        public ProcessEventHelper(ChangeEvent che) { e = che;}
        public void run() {
            if (e instanceof AddedFramesEvent) {
                // process added frames
                AddedFramesEvent ee = (AddedFramesEvent)e;
                root.update(frameModel, ee.getAddedDataFrames(), null, ee.getAddedRelationFrames());

            } else if (e instanceof ChangedFramesEvent) {
                // process slot changes of data frames
                ChangedFramesEvent ee = (ChangedFramesEvent) e;
                HashMap changeMap = ee.getChangedDataFrames();
                if (changeMap == null)
                    return;
                org.cougaar.core.qos.frame.Frame frame;
                ShapeGraphic sh;
                for (Iterator ii=changeMap.keySet().iterator(); ii.hasNext();) {
                    frame = (org.cougaar.core.qos.frame.Frame)ii.next();
                    sh = frameModel.getGraphic(frame);
                    if (sh!= null) {
                        for (Iterator jj=((Collection)changeMap.get(frame)).iterator(); jj.hasNext();)
                            sh.processFrameChange(frame, (org.cougaar.core.qos.frame.Frame.Change) jj.next());
                    }
                }
            } else if (e instanceof RemovedFramesEvent) {
                ;// nothing yet
            } else if (e instanceof TickEvent) {
                // process new transitions (add to a transition queue to be picked up by the
                // animation thread)
                tickEventOccured((TickEvent)e);
            }
        }
    }



    public void addTickStatusListener(ChangeListener l) {
        tickStatusListeners.addListener(l);
    }
    public void removeTickStatusListener(ChangeListener l) {
        tickStatusListeners.removeListener(l);
    }

    public Component getControlPanel() {
        if (cPanel == null)
            cPanel = new ControlPanel();
        return cPanel;
    }


    public ViewConfigParser.WindowSpec getWindowSpec() {
        return wSpec;
    }

    public void reset() {
        Dimension d = getSize();
        reset(d.width, d.height);
    }

    public void reset(int w, int h) {
        super.reset(w,h);
        if (!initialized)
            return;

        root.reshape(0d,0d,(double)w, (double)h);
    }

    public void step(int w, int h) {
        //System.out.println("step");
        if (!initialized)
            return;
        synchronized (lock) {
            ArrayList remove = new ArrayList();
            Transition t;
            boolean finished;
            for (Iterator ii=transitions.iterator(); ii.hasNext();) {
                t = (Transition) ii.next();
                finished = t.step();
                if (finished)
                    remove.add(t);
            }
            int temp = transitions.size();
            for (Iterator rr=remove.iterator(); rr.hasNext();)
                transitions.remove(rr.next());
            if (temp > 0 && transitions.size() == 0) {
                processingTickEvent = false;
                tickStatusListeners.notifyListeners(tickCompleted);
            }
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        //System.out.println("render");
        if (!initialized)
            return;
        synchronized (lock) {
            super.render(w,h,g2);
            root.draw(g2);

            Transition t;
            for (Iterator ii=transitions.iterator(); ii.hasNext();) {
                t = (Transition) ii.next();
                t.draw(g2);
            }

            if (!processingTickEvent)
                processNextTickEvent();
        }
    }


    public ShapeContainer getRootContainer() {
        return root;
    }

    public ShapeGraphic findShape(double x, double y) {
        return root.find(x,y);
    }

    public void tickEventOccured(org.cougaar.core.qos.frame.visualizer.event.TickEvent tick) {
        synchronized (lock) {
            tickEventQueue.add(tick); 
        } 
    }

    public void processNextTickEvent() {
        org.cougaar.core.qos.frame.visualizer.event.TickEvent tickEvent=null;
        synchronized (lock) {
            if (tickEventQueue.size() > 0) {
                tickEvent = (TickEvent)tickEventQueue.get(0);
                tickEventQueue.remove(0);
            }
        }
        if (tickEvent == null)
            return;
        processingTickEvent = true;
        if (log.isDebugEnabled())
            log.debug("processing "+tickEvent);

        transitions.addAll(tickEvent.getTransitions());
    }

  /*
    public void setFrameHelper(FrameModel h) {
        synchronized (lock) {
            //this.graphics = new HashMap();
            this.frameModel = h;
            root.setFrameHelper(frameModel);
        }
    }*/
    
   /*
    public void addFrames(Collection newFrames) {
        synchronized (lock) {
            //root.addedFrames(newFrames, this);
        }
    }

    public void removeFrames(Collection removedFrames) {
        synchronized (lock) {
            //root.removedFrames(removedFrames, this);
        }
    }*/





    class ControlPanel extends Box implements ChangeListener {
        JSlider animationDelaySlider;
        JCheckBox animationOn;

        public ControlPanel() {
            super(BoxLayout.X_AXIS);
            animationDelaySlider = new JSlider(0, 100, (int) sleepAmount);
            animationDelaySlider.addChangeListener(this);
            //animationDelaySlider.setPaintLabels(true);
            animationOn = new JCheckBox("Animation On");
            animationOn.setSelected(Display.ENABLE_ANIMATION);
            animationOn.addChangeListener(this);
            add(new JLabel("Animation delay:"));
            add(animationDelaySlider);
            add(Box.createHorizontalStrut(20));
            add(animationOn);
        }


        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            if (source == animationDelaySlider) {
                sleepAmount = (long) animationDelaySlider.getValue();
            } else if (source == animationOn) {
                Display.ENABLE_ANIMATION = animationOn.isSelected();
            }
        }
    }

    /*
    public void p(String msg) {
    System.out.println(msg);
    }

    public void mousePressed(MouseEvent evt) {
    //super.mousePressed(evt);
    p("Display.mousePressed count="+count);
    if(mouseMoveFlag==false) {
    mousePoint=evt.getPoint();
    count = (++count)%2;
    if (count == 0) {
    lastPoint = mousePoint;
    } else if (count == 1 && lastPoint != null) {
    p("creating a transition from  "+lastPoint.x+","+lastPoint.y+"  to "+mousePoint.x+", "+mousePoint.y);
    transitions.add(new TestTransition(new Point2D.Double((double)lastPoint.x, (double)lastPoint.y),
    new Point2D.Double((double)mousePoint.x, (double)mousePoint.y)));
    lastPoint = null;
    }

    }
    super.mousePressed(evt);
    }
    */
}

