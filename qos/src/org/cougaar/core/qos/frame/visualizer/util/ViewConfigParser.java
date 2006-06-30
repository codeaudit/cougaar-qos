package org.cougaar.core.qos.frame.visualizer.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.geom.RectangularShape;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cougaar.core.qos.frame.visualizer.ContainerLabelRenderer;
import org.cougaar.core.qos.frame.visualizer.LabelRenderer;
import org.cougaar.core.qos.frame.visualizer.LabelRenderers;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeRenderer;
import org.cougaar.core.qos.frame.visualizer.ShapeRenderers;
import org.cougaar.core.qos.frame.visualizer.Shapes;
import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;
import org.cougaar.core.qos.frame.visualizer.shapes.RoundRectangle;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;
import org.xml.sax.Attributes;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 13, 2005
 * Time: 11:43:06 AM
 * To change this template use File | Settings | File Templates.
 */

/*

<window title="SiteA Viewer" w="800" h="700">

    <shape name="circle1" class="java.awt.geom.Ellipse2D.Double" x="0" y="0" w="10" h="10"/>
    <shape name="circle2" class="java.awt.geom.Ellipse2D.Double" x="0" y="0" w="12" h="12"/>
    <shape name="Rect1" class="java.awt.geom.Rectangle2D.Double" x="0" y="0" w="10" h="10" />
    <shape name="RoundRect1" class="java.awt.geom.RoundRectangle2D.Double" x="0" y="0" w="10" h="10" arcw="3" arch="3" />>


    <labelrenderer name="defaultLabelRenderer" xoff="+2" yoff="+5" font="default" color="Color.blue"/>
    <labelrenderer name="jobLabelRenderer" xoff="+2" yoff="+5" font="default" color="Color.blue"/>
    <labelrenderer name="taskLabelRenderer" xoff="+2" yoff="+5" font="default" color="Color.blue"/>
    <labelrenderer name="hostLabelRenderer" xoff="+2" yoff="+5" font="default" color="Color.blue"/>
    <containerlabelrenderer name="queueLabelRenderer" xoff="+2" yoff="+2" font="default" color="Color.blue"/>
    <labelrenderer name="customerLabelRenderer" xoff="+2" yoff="+5" font="default" color="Color.blue"/>

    <shaperenderer name="defaultRenderer" paint="Color.green" selectedpaint="Color.yellow" linewidth="2" bordered="true" filled="true"/>
    <shaperenderer name="processedJobRenderer" paint="Color.megenta" selectedpaint="Color.yellow" linewidth="2" bordered="true" filled="true" />
    <shaperenderer name="doneJobRenderer" paint="Color.green" selectedpaint="Color.yellow" linewidth="1" bordered="true" filled="true"/>
    <shaperenderer name="failedJobRenderer" paint="Color.red" selectedpaint="Color.yellow" linewidth="1" bordered="true" filled="true"/>


    <slotChangeListener name="jobWatcher" slot="status">
       <trigger value="busy" action="setrenderer" name="processedJobRenderer"/>
       <trigger value="busy" action="setshape" shape="circle2"/>
       <trigger value="idle" action="setrenderer" name="defaultRenderer"/>
       <trigger value="idle" action="setshape" shape="circle1"/>
    </slotChangeListener>


    <container id="root" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" shape="Rect1">
        <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalLayout" left="2" right="2" bottom="5" top="10" hpadding="15" vpadding="5"/>

        <container id="customer" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" shape="Rect1">
            <layout class="org.cougaar.core.qos.frame.visualizer.layout.VerticalLayout" left="5" right="5" bottom="5" top="30" hpadding="5" vpadding="5"/>
            <framepredicate isa="customer" frameset="simulator" parentRelationship="contains"/>

                   <container id="job-queue" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" prototype="true" shape="RoundRect1">

                       <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalBoxLayout" left="5" right="5" bottom="5" top="20" hpadding="10" vpadding="10"/>
                       <framepredicate isa="queue" frameset="simulator" parentRelationship="contains"/>

                       <component id="job" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeGraphic" prototype="true" shape="circle1">
                           <framepredicate isa="job" frameset="simulator" parentRelationship="ServicedAt"/>
                       </component>

                   </container>

        </container>


        <container id="siteContainer" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" shape="Rect1">
            <layout class="org.cougaar.core.qos.frame.visualizer.layout.VerticalLayout" left="5" right="5" bottom="10" top="30" hpadding="10" vpadding="10"/>
            <framepredicate isa="site" frameset="simulator" name="Local"  parentRelationship="contains"/>



            <container id="hostContainer" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" shape="Rect1" visible="false">
                <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalLayout" left="5" right="5" bottom="5" top="15" hpadding="5" vpadding="5"/>

                <container id="host" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.RectangularContainer" prototype="true" shape="Rect1">
                    <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalLayout" left="5" right="5" bottom="0" top="15" hpadding="5" vpadding="5"/>
                    <framepredicate isa="host" frameset="simulator"  parentRelationship="contains"/>



                    <container id="queue" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" prototype="true" shape="RoundRect1">

                       <layout class="org.cougaar.core.qos.frame.visualizer.layout.VerticalBoxLayout" left="5" right="5" bottom="2" top="20" hpadding="1" vpadding="1"/>
                       <framepredicate isa="queue" frameset="simulator" parentRelationship="contains"/>

                       <component id="task" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeGraphic" prototype="true" shape="circle1">
                          <framepredicate isa="task" frameset="simulator"  parentRelationship="ServicedAt"/>
                       </component>
                    </container>
                </container>
            </container>



            <container id="queueGroup" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" shape="Rect1" visible="false">
                    <layout class="org.cougaar.core.qos.frame.visualizer.layout.VerticalLayout" left="2" right="2" bottom="2" top="2" hpadding="5" vpadding="5"/>

                    <container id="task-queue" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" shape="RoundRect1" prototype="true">
                        <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalBoxLayout" left="2" right="2" bottom="2" top="20" hpadding="5" vpadding="5"/>
                        <framepredicate isa="queue" frameset="simulator" parentRelationship="contains"/>
                        <component id="task" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeGraphic" prototype="true" shape="circle1">
                            <framepredicate isa="task" frameset="simulator"  parentRelationship="ServicedAt"/>
                        </component>
                    </container>
            </container>
         </container>


   </container>

</window>
*/
public class ViewConfigParser extends XMLParser  {
    public WindowSpec windowSpec;
    Vector containerStack;
    public ShapeContainer root;
    protected static HashMap colorNameMap=null;
    protected static SlotChangeListener lastSlotChangeListener = null;

    Shapes shapes;
    LabelRenderers labelRenderers;
    ShapeRenderers shapeRenderers;
    SlotChangeListeners slotListeners;

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
        shapes = new Shapes();
        labelRenderers = new LabelRenderers();
        shapeRenderers = new ShapeRenderers();
        slotListeners  = new SlotChangeListeners();
        containerStack = new Vector();
        if (colorNameMap == null)
            initColorNameMap();
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

    protected void initColorNameMap() {
        colorNameMap = new HashMap();
        colorNameMap.put("Color.black", Color.black);
        colorNameMap.put("Color.BLACK", Color.BLACK);
        colorNameMap.put("Color.blue", Color.blue);
        colorNameMap.put("Color.BLUE",Color.BLUE);
        colorNameMap.put("Color.cyan",Color.cyan);
        colorNameMap.put("Color.CYAN", Color.CYAN);
        colorNameMap.put("Color.DARK_GRAY", Color.DARK_GRAY);
        colorNameMap.put("Color.darkGray", Color.darkGray);
        colorNameMap.put("Color.gray", Color.gray);
        colorNameMap.put("Color.GRAY", Color.GRAY);
        colorNameMap.put("Color.green", Color.green);
        colorNameMap.put("Color.GREEN", Color.GREEN);
        colorNameMap.put("Color.LIGHT_GRAY", Color.LIGHT_GRAY);
        colorNameMap.put("Color.magenta", Color.magenta);
        colorNameMap.put("Color.MAGENTA", Color.MAGENTA);
        colorNameMap.put("Color.orange", Color.orange);
        colorNameMap.put("Color.ORANGE", Color.ORANGE);
        colorNameMap.put("Color.pink", Color.pink);
        colorNameMap.put("Color.PINK", Color.PINK);
        colorNameMap.put("Color.red", Color.red);
        colorNameMap.put("Color.RED", Color.RED);
        colorNameMap.put("Color.white", Color.white);
        colorNameMap.put("Color.WHITE", Color.WHITE);
        colorNameMap.put("Color.yellow", Color.yellow);
        colorNameMap.put("Color.YELLOW", Color.YELLOW);
    }



    public void startElement(String uri, String local, String name, Attributes attrs) {
        if (name.equals("shape")) {
           createShape(attrs);
        } else if (name.equals("labelrenderer")) {
           createLabelRenderer(attrs);
        } else if (name.equals("containerlabelrenderer")) {
           createContainerLabelRenderer(attrs);
        } else if (name.equals("shaperenderer")) {
           createShapeRenderer(attrs);
        } else if (name.equals("slotChangeListener")) {
           lastSlotChangeListener = createSlotChangeListener(attrs);
        } else if (name.equals("trigger")) {
           createTrigger(attrs);
        } else if (name.equals("window"))
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
        } else if (name.equals("slotlistener")) {
            ShapeGraphic c = (ShapeGraphic) containerStack.get(containerStack.size()-1);
            String nm = attrs.getValue("name");
            SlotChangeListener l = slotListeners.get(nm);
            if (c!=null && l!=null)
                c.addSlotListener(l);
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

        if (name.equals("slotChangeListener"))
           lastSlotChangeListener = null;
        else if (name.equals("window"))
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

    public SlotChangeListener createSlotChangeListener(Attributes attrs) {
        String name = attrs.getValue("name");
        String slot = attrs.getValue("slot");
        SlotChangeListener sl = new SlotChangeListener(name, slot);
        slotListeners.add(name, sl);
        return sl;
    }

    public  void createTrigger(Attributes attrs) {
        if (lastSlotChangeListener == null)
            return;
        // <trigger value="busy" action="setrenderer" name="processedJobRenderer"/>
        // <trigger value="busy" action="setshape" shape="circle2"/>
        String value = attrs.getValue("value");
        String action = attrs.getValue("action");

        if (action.equals("setrenderer")) {
            String nm = attrs.getValue("name");
            ShapeRenderer sr = shapeRenderers.get(nm);
            lastSlotChangeListener.setRendererTrigger(nm, sr, value);
        }  else if (action.equals("setshape")) {
            String shp = attrs.getValue("shape");
            RectangularShape shape = shapes.get(shp);
            lastSlotChangeListener.setShapeTrigger(shp, shape, value);
        }  else if (action.equals("setlabelrenderer")) {
            String nm = attrs.getValue("name");
            LabelRenderer lblr = labelRenderers.get(nm);
            lastSlotChangeListener.setLabelRendererTrigger(nm, lblr, value);
        }
    }

    public  RectangularShape createShape(Attributes attrs) {
        // <shape name="circle1" class="java.awt.geom.Ellipse2D.Double" x="0" y="0" w="10" h="10"/>
        double x=Double.parseDouble(attrs.getValue("x"));
        double y=Double.parseDouble(attrs.getValue("y"));
        double w=Double.parseDouble(attrs.getValue("w"));
        double h=Double.parseDouble(attrs.getValue("h"));
        double arcw=0d, arch=0d;
        String klass= attrs.getValue("class");
        String name = attrs.getValue("name");
        RectangularShape shape = (RectangularShape) createInstance(klass);
        if (shape == null)
            throw new NullPointerException("could not create shape '"+name+"'");
        if (klass.endsWith("RoundRectangle")) {
            arcw = Double.parseDouble(attrs.getValue("arcw"));
            arch = Double.parseDouble(attrs.getValue("arch"));
            ((RoundRectangle)shape).setRoundRect(x,y,w,h,arcw,arch);
        } else
            shape.setFrame(x,y,w,h);
        shapes.add(name, shape);
        return shape;
    }

    public LabelRenderer createLabelRenderer(Attributes attrs) {
        //<labelrenderer name="defaultLabelRenderer" xoff="+2" yoff="+5" font="default" color="Color.blue"/>
        String x = attrs.getValue("xoff");
        String y = attrs.getValue("yoff");
        boolean negativeX = x.startsWith("-");
        String xo = (x.startsWith("+") ? x.substring(x.lastIndexOf("+")) : (x.startsWith("-") ? x.substring(x.lastIndexOf("-")) : x));
        String yo = (y.startsWith("+") ? y.substring(y.lastIndexOf("+")) : (y.startsWith("-") ? y.substring(y.lastIndexOf("-")) : y));
        double xoff = Double.parseDouble(xo);
        double yoff = Double.parseDouble(yo);
        String font = attrs.getValue("font");
        String col  = attrs.getValue("color");
        Color color = getColor(col);
        String name = attrs.getValue("name");

        LabelRenderer lblr= new LabelRenderer(name, xoff, yoff, null, color);
        labelRenderers.add(lblr);
        String mouseOver = attrs.getValue("mouseover");
        if (mouseOver != null && mouseOver.equals("true"))
            lblr.setMouseOverOnly(true);
        return lblr;
    }

    public LabelRenderer createContainerLabelRenderer(Attributes attrs) {
        double xoff = Double.parseDouble(attrs.getValue("xoff"));
        double yoff = Double.parseDouble(attrs.getValue("yoff"));
        String font = attrs.getValue("font");
        String col  = attrs.getValue("color");
        Color color = getColor(col);
        String name = attrs.getValue("name");
        ContainerLabelRenderer lblr= new ContainerLabelRenderer(name, xoff, yoff, null, color);
        labelRenderers.add(lblr);
        return lblr;
    }

    public static Color getColor(String colorDesc) {
        if (colorDesc == null)
            return null;

        Color color = null;
        if (colorDesc.startsWith("Color."))
            color = getJavaColor(colorDesc);
        else if (colorDesc.startsWith("#"))
            color = Color.decode(colorDesc);
        else if (colorDesc.startsWith("(") && colorDesc.endsWith(")"))
            color = getRGBColor(colorDesc);
        return color;
    }

    public static Color getJavaColor(String colorDesc) {
        Color c = (Color) colorNameMap.get(colorDesc);
        if (c == null) {
            // TODO: take care of cases such as Color.red.brighter
            ;
        }
        return c;
    }
    public static Color getRGBColor(String colorName) {
        int startIndex = colorName.indexOf("(");
        int endIndex = colorName.indexOf(")");
        StringTokenizer st = new StringTokenizer(colorName.substring(startIndex, endIndex),",");
        String rs = (st.hasMoreTokens() ? st.nextToken().trim() : "0");
        String gs = (st.hasMoreTokens() ? st.nextToken().trim() : "0");
        String bs = (st.hasMoreTokens() ? st.nextToken().trim() : "0");
        int r = Integer.parseInt( (rs.startsWith("(") ? rs.substring(rs.indexOf("(")+1) : rs));
        int g = Integer.parseInt( gs );
        int b = Integer.parseInt( (bs.endsWith(")") ? rs.substring(0,rs.indexOf(")")-1) : bs));
        return new Color(r,g,b);
    }

    public static Paint getPaint(String paintDesc) {
        Paint p = getColor(paintDesc);
        // for now return p, TODO: in future take care of textures and gradients
        return p;
    }


    public ShapeRenderer createShapeRenderer(Attributes attrs) {
       //<shaperenderer name="defaultRenderer" paint="Color.green" selectedpaint="Color.yellow" linewidth="2" bordered="true" filled="true"/>
        String name = attrs.getValue("name");
        Paint paint = getPaint(attrs.getValue("paint"));
        Paint selectedPaint = getPaint(attrs.getValue("selectedpaint"));
        Paint fillPaint = getPaint(attrs.getValue("fillpaint"));
        Paint selfillPaint = getPaint(attrs.getValue("selfillpaint"));
        int linewidth = Integer.parseInt(attrs.getValue("linewidth"));
        boolean bordered = "true".equals(attrs.getValue("bordered"));
        boolean filled = "true".equals(attrs.getValue("filled"));
        ShapeRenderer sh = new ShapeRenderer(name, paint, selectedPaint, fillPaint, selfillPaint, linewidth, bordered, filled);
        shapeRenderers.add(sh);
        return sh;
    }


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
        String parentRelationship=attrs.getValue("parentRelationship");
        return new FramePredicate(kind, name, parentRelationship);
    }

    protected  ShapeGraphic createComponent(Attributes attrs) {
        String classname = attrs.getValue("class");
        ShapeGraphic shape = (ShapeGraphic) createInstance(classname);
        shape.setId(attrs.getValue("id"));
        shape.setLabel(attrs.getValue("label"));
        String frameIdSlotName = attrs.getValue("idframeslot");
        String prototypeBool = attrs.getValue("prototype");
        String vis = attrs.getValue("visible");
        String shapeName = attrs.getValue("shape");
        if (shapeName != null)
            shape.setShapePrototype(shapes.get(shapeName));
        String labelrender = attrs.getValue("labelrender");
        String shaperender = attrs.getValue("shaperender");
        if (labelrender != null)
            shape.setLabelRenderer(labelRenderers.get(labelrender));
        if (shaperender != null)
            shape.setRenderer(shapeRenderers.get(shaperender));

        boolean visible = true;
        if (vis != null)
            visible = "true".equals(vis);
        //if (visible && (labelrender == null ||  shaperender == null)) {
        //}

        boolean isprototype = false;
        if (prototypeBool != null && prototypeBool.equals("true"))
            isprototype = true;
        if (frameIdSlotName != null)
            shape.setFrameIdSlotName(frameIdSlotName);
        shape.setPrototype(isprototype);
        shape.setVisible(visible);
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
        String vis = attrs.getValue("visible");
        String shapeName = attrs.getValue("shape");
        if (shapeName != null)
            container.setShapePrototype(shapes.get(shapeName));
        String labelrender = attrs.getValue("labelrender");
        String shaperender = attrs.getValue("shaperender");
        if (labelrender != null)
            container.setLabelRenderer(labelRenderers.get(labelrender));
        if (shaperender != null)
            container.setRenderer(shapeRenderers.get(shaperender));


        boolean visible = true;
        if (vis != null)
            visible = "true".equals(vis);
        boolean isprototype = false;
        if (prototypeBool != null && prototypeBool.equals("true"))
            isprototype = true;
        if (frameIdSlotName != null)
            container.setFrameIdSlotName(frameIdSlotName);
        container.setPrototype(isprototype);
        container.setVisible(visible);
        return container;
    }
}
