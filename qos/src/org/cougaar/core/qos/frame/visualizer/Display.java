package org.cougaar.core.qos.frame.visualizer;


import org.cougaar.core.qos.frame.visualizer.util.XMLParser;
import org.cougaar.core.qos.frame.visualizer.util.ViewConfigParser;
import org.cougaar.core.qos.frame.visualizer.util.ChangeModel;
import org.cougaar.core.qos.frame.visualizer.test.TickEvent;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.core.qos.frame.visualizer.test.TestTransition;
import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
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
public class Display extends AnimatedCanvas {
    public static boolean ENABLE_ANIMATION = false;//true;

     ShapeContainer root;
     boolean initialized, processingTickEvent;
     ArrayList transitions, tickEventQueue;

     // debug
     Collection frames;
     HashMap frameContainerMap, prototypeMap;
     FrameHelper frameHelper;
     private transient Logger log = Logging.getLogger(getClass().getName());

    ViewConfigParser.WindowSpec wSpec;
    ControlPanel cPanel;

    // changes
    ChangeModel changes;
    ChangeEvent change;


     public Display(File xmlFile) {
         transitions = new ArrayList();
         tickEventQueue = new ArrayList();
         frameContainerMap = new HashMap();
         prototypeMap = new HashMap();
         processingTickEvent = initialized = false;
         changes = new ChangeModel();
         change = new ChangeEvent(this);
         ViewConfigParser parser = new ViewConfigParser();
         parser.parse(xmlFile);
         wSpec = parser.windowSpec;
         root = parser.root;
         initialized = (root != null);
         if (initialized) {
            if (animating != null)
                animating.start();
         }
    }

    public void addChangeListener(ChangeListener l) {
        changes.addListener(l);
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
        if (!initialized)
            return;
        ArrayList remove = new ArrayList();
        Transition t;
        boolean finished;
        for (Iterator ii=transitions.iterator(); ii.hasNext();) {
           t = (Transition) ii.next();
           finished = t.step();
           if (finished)
               remove.add(t);
        }
        for (Iterator rr=remove.iterator(); rr.hasNext();)
            transitions.remove(rr.next());
        if (transitions.size() == 0) {
            processingTickEvent = false;
            changes.notifyListeners(change);
        }

    }

    public void render(int w, int h, Graphics2D g2) {
        if (!initialized)
            return;
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


    public ShapeContainer getRootContainer() {
        return root;
    }

    public ShapeGraphic findShape(double x, double y) {
        return root.find(x,y);
    }

    public ShapeGraphic findShape(org.cougaar.core.qos.frame.Frame f) {
	    return root.find(f);
    }


    
    public void tickEventOccured(TickEvent tick) {
        synchronized (tickEventQueue) {
            tickEventQueue.add(tick); 
        } 
    } 

    public void processNextTickEvent() {
        TickEvent tickEvent=null;
        synchronized (tickEventQueue) {
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


    public void setFrameHelper(FrameHelper h) {
        this.frameHelper = h;
        root.setFrameHelper(frameHelper);
    }





    class ControlPanel extends Box implements ChangeListener {
        JSlider animationDelaySlider;
        JCheckBox animationOn;

        public ControlPanel() {
            super(BoxLayout.X_AXIS);
            animationDelaySlider = new JSlider(0, 1000, (int) sleepAmount);
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

