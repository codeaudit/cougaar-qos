package org.cougaar.core.qos.frame.visualizer.util;

import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.cougaar.core.qos.frame.Frame;
import org.cougaar.core.qos.frame.visualizer.LabelRenderer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeRenderer;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 1, 2005
 * Time: 6:32:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class SlotChangeListener implements Cloneable  {


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

    public String getName() {
        return listenerName;
    }
    public String getSlotName() {
        return slotName;
    }

    public void setShapeGraphic(ShapeGraphic sh){
        this.shapeGraphic = sh;
    }

    public void setRendererTrigger(String shRendererName, ShapeRenderer r, Object value) {
        addTrigger(value, new SetRenderer(slotName, shRendererName,r));
    }

    public void setLabelRendererTrigger(String lblRendererName, LabelRenderer r, Object value) {
        addTrigger(value,  new SetLabelRenderer(slotName, lblRendererName, r));
    }

    public void setShapeTrigger(String shapeName, RectangularShape shape, Object value) {
        addTrigger(value, new SetShape(slotName, shapeName, shape));
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


    protected static void pad(StringBuffer sb, int start, int end) {
        int i, length = sb.length();
        for (i=start; (i <= end && i < length); i++)
            sb.setCharAt(i,' ');
    }
    protected static StringBuffer getPadded(int numSpaces) {
        StringBuffer sb = new StringBuffer();
        sb.setLength(numSpaces);
        pad(sb, 0, numSpaces-1);
        return sb;
    }

    /*
    <slotChangeListener name="jobWatcher" slot="job-status">
       <trigger value="processing" action="setrenderer" name="processedJobRenderer"/>
       <trigger value="processing" action="setshape" shape="circle2"/>
       <?trigger value="processing" action="setlabelrenderer" name="defaultLabelRenderer"/?>
       <trigger value="waiting" action="setrenderer" name="waitingJobRenderer"/>
       <trigger value="waiting" action="setshape" shape="circle1"/>
       <trigger value="done" action="setshape" shape="circle0"/>
       <trigger value="done" action="setrenderer" name="doneJobRenderer"/>
    </slotChangeListener>
    */
    // hack
    public String toXML(int indent, int offset) {
        StringBuffer sb = new StringBuffer();

        sb.append(getPadded(indent));
        sb.append("<slotChangeListener name=\""+listenerName+"\" slot=\""+slotName+"\">\n");
        TriggerAction t;
        String triggActions[];

        for (Iterator ii=triggerMap.values().iterator(); ii.hasNext();) {
            //sb.append(getPadded(indent+offset));
            triggActions =  ((Trigger)ii.next()).toXML();
            if (triggActions == null || triggActions.length ==0)
                continue;
            for (int i=0; i < triggActions.length; i++) {
                sb.append(getPadded(indent+(2*offset)));
                sb.append(triggActions[i]);
                sb.append("\n");
            }
        }
        sb.append(getPadded(indent));
        sb.append("</slotChangeListener>");
        return sb.toString();
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
    /*
       <trigger value="processing" action="setrenderer" name="processedJobRenderer"/>
       <trigger value="processing" action="setshape" shape="circle2"/>
       <?trigger value="processing" action="setlabelrenderer" name="defaultLabelRenderer"/?>
    */
    public String[] toXML() {
        String xml[] = new String[actionList.size()];
        TriggerAction t;
        int i=0;
        for (Iterator ii=actionList.iterator(); ii.hasNext();i++) {
            t = (TriggerAction)ii.next();
            xml[i]= t.toXML();
        }
        return xml;
    }
}


class TriggerAction implements Cloneable {
    String slotName;
    public TriggerAction(String slotName) { this.slotName = slotName;}
    public void execute(ShapeGraphic shapeGraphic) {}
    public String toXML() {return "";}
}

class SetRenderer extends TriggerAction {
    ShapeRenderer shapeRenderer;
    String shapeRendererName;

    public SetRenderer(String slotName, String shRendererName, ShapeRenderer r) {
        super(slotName);
        shapeRendererName = shRendererName;
        shapeRenderer=r;
    }
    public void execute(ShapeGraphic shapeGraphic) {
        if(shapeGraphic !=null && shapeRenderer != null)
            shapeGraphic.setRenderer(shapeRenderer);
    }

    public String toXML() {
        return "<trigger value=\""+slotName+"\" action=\"setrenderer\" name=\""+shapeRendererName+"\"/>";
    }
}


class SetLabelRenderer extends TriggerAction {
    LabelRenderer labelRenderer;
    String labelRendererName;
    public SetLabelRenderer(String slotName, String lblRendererName, LabelRenderer lblr) {
        super(slotName);
        this.labelRendererName = lblRendererName;
        this.labelRenderer = lblr;
    }
    public void execute(ShapeGraphic shapeGraphic) {
        if (shapeGraphic != null && labelRenderer != null)
            shapeGraphic.setLabelRenderer(labelRenderer);
    }
    public String toXML() {
        return "<trigger value=\""+slotName+"\" action=\"setlabelrenderer\" name=\""+labelRendererName+"\"/>";
    }
}

class SetShape extends TriggerAction {
    RectangularShape shape;
    String shapeName;

    public SetShape(String slotName, String shName, RectangularShape shape) {
        super(slotName);
        this.shapeName = shName;
        this.shape=shape;
    }
    public void execute(ShapeGraphic shapeGraphic) {
        if (shapeGraphic != null) {
            shapeGraphic.setShapePrototype(shape);
            shapeGraphic.createShape();
        }
    }
    public String toXML() {
        return "<trigger value=\""+slotName+"\" action=\"setshape\" shape=\""+shapeName+"\"/>";
    }
}




