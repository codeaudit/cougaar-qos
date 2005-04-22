package org.cougaar.core.qos.frame.visualizer.util;

import org.xml.sax.Attributes;
import org.cougaar.core.qos.frame.visualizer.Surface;
import org.cougaar.core.qos.frame.visualizer.Display;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.cougaar.util.log.Logging;
import org.cougaar.util.log.Logger;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 13, 2005
 * Time: 11:43:06 AM
 * To change this template use File | Settings | File Templates.
 */

/*
<window title="SiteA Viewer" w="700" h="500">

    <container id="root" class="org.cougaar.core.qos.frame.visualizer.ShapeGroup">
        <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalLayout" left="2" right="2" bottom="10" top="10" hpadding="10" vpadding="10"/>

        <container id="customer" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.RectangularContainer">
            <layout class="org.cougaar.core.qos.frame.visualizer.layout.VerticalLayout" left="5" right="5" bottom="5" top="10" hpadding="10" vpadding="10"/>
            <framepredicate isa="customer" frameset="simulator" parentRelationship="contains"/>

                <container id="job-queue" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.RectangularContainer" prototype="true">
                    <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalBoxLayout" left="5" right="5" bottom="5" top="5" hpadding="10" vpadding="10"/>
                    <framepredicate isa="queue" frameset="simulator" parentRelationship="contains"/>

                    <component id="job" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.Job" prototype="true">
                        <framepredicate isa="job" frameset="simulator" parentRelationship="ServicedAt"/>
                    </component>

                </container>
        </container>
*/
public class ViewConfigParser extends XMLParser  {
    public WindowSpec windowSpec;
    Vector containerStack;
    public ShapeContainer root;
    private transient Logger log = Logging.getLogger(getClass().getName());


    public static class WindowSpec {
        String title="";
        Dimension size;
        public WindowSpec(String title, Dimension d) {
            this.title = title;
            this.size = d;
        }
        public String getTitle() { return title;}
        public Dimension getSize() { return size; }
    }

    public ViewConfigParser(){
        super();
        containerStack = new Vector();
    }

    public void startElement(String uri, String local, String name, Attributes attrs) {
        if (name.equals("window"))
            windowSpec = createWindow(attrs);
        else if (name.equals("container")) {
            ShapeContainer c = createContainer(attrs);
            if (root == null && containerStack.size() == 0)
                root = c;
            containerStack.add(c);

        } else if (name.equals("layout")) {
            ShapeGraphic c = (ShapeGraphic) containerStack.get(containerStack.size()-1);
            if (c instanceof ShapeContainer)
                ((ShapeContainer)c).setLayout(createShapeLayout(attrs));
        } else if (name.equals("framepredicate")) {
            ShapeGraphic c = (ShapeGraphic) containerStack.get(containerStack.size()-1);
            c.setFramePredicate(createFramePredicate(attrs));
        } else if (name.equals("component")) {
            //ShapeGraphic c = (ShapeGraphic) containerStack.get(containerStack.size()-1);
            ShapeGraphic g = createComponent(attrs);
            //if (c instanceof ShapeContainer)
              //  ((ShapeContainer)c).add(g);
            containerStack.add(g);
        }
    }


    public void endElement(String uri, String local, String name) {
        ShapeGraphic shg=null;

        if (name.equals("window"))
            ;
        else if (name.equals("container")) {
            if (containerStack.size()>0) {
                shg = (ShapeGraphic) containerStack.remove(containerStack.size()-1);
                ShapeGraphic parent = ((containerStack.size()>0) ? ((ShapeGraphic) containerStack.get(containerStack.size()-1)) : null);
                if (parent != null && parent instanceof ShapeContainer)
                    ((ShapeContainer)parent).add(shg);
            }
        } else if (name.equals("layout"))
            ;
        else if (name.equals("framepredicate"))
            ;
        else if (name.equals("component")) {
           if (containerStack.size()>0)  {
                shg = (ShapeGraphic) containerStack.remove(containerStack.size()-1);
                ShapeGraphic parent = ((containerStack.size()>0) ? ((ShapeGraphic) containerStack.get(containerStack.size()-1)) : null);
                if (parent != null && parent instanceof ShapeContainer)
                    ((ShapeContainer)parent).add(shg);
            }
        }
    }




    // utiltity
    public static WindowSpec createWindow(Attributes attrs) {
        String title = attrs.getValue("title");
        int width = Integer.parseInt(attrs.getValue("w"));
        int height= Integer.parseInt(attrs.getValue("h"));
        return new WindowSpec(title, new Dimension(width, height));
    }

    protected static Object createInstance(String classname) {
        try {
            Class c = Class.forName(classname);
            Constructor constructor = c.getConstructor(new Class[0]);
            return constructor.newInstance(new Object[0]);
        }  catch (Exception ee) {
            ee.printStackTrace();
        }
        return null;
    }

    protected static ShapeLayout createShapeLayout(Attributes attrs) {
        String classname = attrs.getValue("class");
        String leftMargin = attrs.getValue("left");
        String rightMargin= attrs.getValue("right");
        String topMargin  = attrs.getValue("top");
        String bottomMargin= attrs.getValue("bottom");
        String hpadding = attrs.getValue("hpadding");
        String vpadding = attrs.getValue("vpadding");

        ShapeLayout sl = (ShapeLayout) createInstance(classname);
        if (sl != null)
            sl.setMargins( ((leftMargin !=  null && leftMargin.length()>0) ? Double.parseDouble(leftMargin):0d),
                           ((rightMargin != null && rightMargin.length()>0) ? Double.parseDouble(rightMargin):0d),
                           ((bottomMargin != null && bottomMargin.length()>0) ? Double.parseDouble(bottomMargin):0d),
                           ((topMargin != null && topMargin.length()>0) ? Double.parseDouble(topMargin):0d),
                           ((hpadding !=null && hpadding.length()>0) ? Double.parseDouble(hpadding) : 0d),
                           ((vpadding != null && vpadding.length()>0) ? Double.parseDouble(vpadding) : 0d));
        return sl;
    }

    protected static FramePredicate createFramePredicate(Attributes attrs) {
        if (attrs == null)
            return null;
        String kind = attrs.getValue("isa");
        String name = attrs.getValue("name");
        String frameset= attrs.getValue("frameset");
        String parentRelationship=attrs.getValue("parentRelationship");
        return new FramePredicate(kind, name, frameset, parentRelationship);
    }

    protected static ShapeGraphic createComponent(Attributes attrs) {
        String classname = attrs.getValue("class");
        ShapeGraphic shape = (ShapeGraphic) createInstance(classname);
        shape.setId(attrs.getValue("id"));
        shape.setLabel(attrs.getValue("label"));
        String frameIdSlotName = attrs.getValue("idframeslot");
        String prototypeBool = attrs.getValue("prototype");
        boolean isprototype = false;
        if (prototypeBool != null && prototypeBool.equals("true"))
            isprototype = true;
        if (frameIdSlotName != null)
            shape.setFrameIdSlotName(frameIdSlotName);
        shape.setPrototype(isprototype);
        return shape;
    }

    protected ShapeContainer createContainer(Attributes attrs) {
        // container
        String classname = attrs.getValue("class");
        ShapeContainer container = (ShapeContainer) createInstance(classname);
        container.setId(attrs.getValue("id"));
        container.setLabel(attrs.getValue("label"));
        String frameIdSlotName = attrs.getValue("idframeslot");
        String prototypeBool = attrs.getValue("prototype");
        boolean isprototype = false;
        if (prototypeBool != null && prototypeBool.equals("true"))
            isprototype = true;
        if (frameIdSlotName != null)
            container.setFrameIdSlotName(frameIdSlotName);
        container.setPrototype(isprototype);
        return container;
    }
}
