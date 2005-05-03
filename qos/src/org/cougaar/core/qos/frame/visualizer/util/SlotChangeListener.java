package org.cougaar.core.qos.frame.visualizer.util;

import org.cougaar.core.qos.frame.Frame;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeRenderer;
import org.cougaar.core.qos.frame.visualizer.LabelRenderer;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 1, 2005
 * Time: 6:32:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class SlotChangeListener implements Cloneable  {

    private static HashMap listeners = new HashMap();
    public static void add(String name, SlotChangeListener l) {
        listeners.put(name, l);
    }
    public static SlotChangeListener get(String name) {
        return getCopy(name);
    }
    private static SlotChangeListener getCopy(String name) {
        SlotChangeListener s = (SlotChangeListener) listeners.get(name);
        if (s!= null)
            return (SlotChangeListener) s.cloneInstance();
        return null;
    }

    //public final static String SET_RENDERER = "setrenderer";
    //public final static String SET_SHAPE = "setshape";
    //public final static String SET_LABEL_RENDERER = "setlabelrenderer";
    ShapeGraphic shapeGraphic;
    String listenerName, slotName;
    HashMap triggerMap;


    private transient Logger log = Logging.getLogger(getClass().getName());



    public SlotChangeListener(String listenerName, String slotName) {
        this.listenerName = listenerName;
        this.slotName = slotName;
        triggerMap = new HashMap();
    }

    public void setShapeGraphic(ShapeGraphic sh){
        this.shapeGraphic = sh;
    }

    public void setRendererTrigger(ShapeRenderer r, Object value) {
        addTrigger(value, new SetRenderer(r));
    }

    public void setLabelRendererTrigger(LabelRenderer r, Object value) {
        addTrigger(value,  new SetLabelRenderer(r));
    }

    public void setShapeTrigger(RectangularShape shape, Object value) {
        addTrigger(value, new SetShape(shape));
    }

    protected void addTrigger(Object value, TriggerAction action) {
        Trigger t = (Trigger) triggerMap.get(value);
        if (t == null) {
            t = new Trigger();
            triggerMap.put(value, t);
        }
        t.add(action);
    }

    public void slotChanged(Frame frame, Frame.Change change) {
        if (shapeGraphic == null || (shapeGraphic.hasFrame() && frame != shapeGraphic.getFrame()) )
            return;
        String slotName = change.getSlotName();
        if (this.slotName.equals(slotName)) {
            Object value    = change.getValue();

            //if (log.isDebugEnabled())
              //  log.debug("frame "+frame+"  changed   slot="+slotName+"  value="+value+" child="+frame.getValue("child-value"));
            Trigger t = (Trigger) triggerMap.get(value);
            if (t!=null)
                SwingUtilities.invokeLater(t);
        }
    }

    public void validate() {
        if (shapeGraphic == null || !shapeGraphic.hasFrame() )
            return;

        Frame f = shapeGraphic.getFrame();
        Object value = f.getValue(slotName);
        //System.out.println("SlotchangeListener.validate for slot '"+slotName+"' for frame "+f+" value="+value);
        if (value != null) {
            Trigger t = (Trigger) triggerMap.get(value);
            if (t!=null) {
                //System.out.println("SlotchangeListener for slot '"+slotName+"' validating value ="+value);
                //if (log.isDebugEnabled())
                  //  log.debug("validating value ="+value);
                SwingUtilities.invokeLater(t);
            }
        }
    }

    // do a deep clone
    public SlotChangeListener cloneInstance() {
        try {
            SlotChangeListener cloned = (SlotChangeListener) this.clone();
            cloned.slotName = this.slotName;
            cloned.triggerMap = new HashMap();

            // clone triggers
            String key;
            Trigger t,clonedT;
            for (Iterator ii=triggerMap.keySet().iterator(); ii.hasNext();) {
                key = (String) ii.next();
                t = (Trigger) triggerMap.get(key);
                clonedT = t.cloneInstance();
                clonedT.setParent(cloned);
                cloned.triggerMap.put(key, clonedT);
	        }

	        return cloned;
        } catch (CloneNotSupportedException ee) {
            ee.printStackTrace();
        }
        return null;
    }
}





    class Trigger implements Runnable, Cloneable {
        ArrayList actionList;
        ShapeGraphic shapeGraphic;
        SlotChangeListener listener;

        public Trigger() {
            actionList = new ArrayList();
        }
        public void setParent(SlotChangeListener l) {
            listener = l;
        }
        public void setGraphic(ShapeGraphic g) {
            this.shapeGraphic = g;
        }
        public void run() {
            if (actionList.size() > 0) {
                for (Iterator ii=actionList.iterator(); ii.hasNext();)
                  ((TriggerAction)ii.next()).execute(listener.shapeGraphic);
            }
        }
        public void add(TriggerAction t) {
            if (t!=null)
                actionList.add(t);
        }
        public Trigger cloneInstance() {
            try {
                return (Trigger) this.clone();
            } catch (CloneNotSupportedException ee) {
                ee.printStackTrace();
            }
            return null;
        }
    }


    class TriggerAction implements Cloneable {
        public TriggerAction() {}
        public void execute(ShapeGraphic shapeGraphic) {}
    }

    class SetRenderer extends TriggerAction {
        ShapeRenderer shapeRenderer;
        public SetRenderer(ShapeRenderer r) {
            super();
            shapeRenderer=r;
        }
        public void execute(ShapeGraphic shapeGraphic) {
            if(shapeGraphic !=null && shapeRenderer != null)
                shapeGraphic.setRenderer(shapeRenderer);
        }
    }


    class SetLabelRenderer extends TriggerAction {
        LabelRenderer labelRenderer;
        public SetLabelRenderer(LabelRenderer lblr) {
            super();
            this.labelRenderer = lblr;
        }
        public void execute(ShapeGraphic shapeGraphic) {
            if (shapeGraphic != null && labelRenderer != null)
                shapeGraphic.setLabelRenderer(labelRenderer);
        }
    }

    class SetShape extends TriggerAction {
        RectangularShape shape;
        public SetShape(RectangularShape shape) {
            super();
            this.shape=shape;
        }
        public void execute(ShapeGraphic shapeGraphic) {
            if (shapeGraphic != null) {
                shapeGraphic.setShapePrototype(shape);
                shapeGraphic.createShape();
            }
        }
    }




