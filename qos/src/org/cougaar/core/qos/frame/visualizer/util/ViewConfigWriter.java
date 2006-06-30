package org.cougaar.core.qos.frame.visualizer.util;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import org.cougaar.core.qos.frame.visualizer.Display;
import org.cougaar.core.qos.frame.visualizer.DisplayWindow;
import org.cougaar.core.qos.frame.visualizer.LabelRenderers;
import org.cougaar.core.qos.frame.visualizer.ShapeContainer;
import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;
import org.cougaar.core.qos.frame.visualizer.ShapeRenderers;
import org.cougaar.core.qos.frame.visualizer.Shapes;
import org.cougaar.core.qos.frame.visualizer.layout.ShapeLayout;
import org.cougaar.core.qos.frame.visualizer.test.FramePredicate;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 15, 2005
 * Time: 11:45:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class ViewConfigWriter {


  public ViewConfigWriter() {
  }

  public boolean generate(DisplayWindow window, String filename, int indentation, int offset) {
        return generate(window, new File(filename), indentation, offset);
  }
  public boolean generate(DisplayWindow window, File file, int indentation, int offset) {
      try {
        FileWriter fwriter = new FileWriter(file);
        PrintWriter writer = new PrintWriter(fwriter);
        SpecFormatWriter spec = new SpecAFormatWriter();
        spec.write(window, writer, indentation, offset);
        writer.close();
      } catch (IOException ee) {
          ee.printStackTrace();
          return false;
      }
      return true;
  }


   abstract class SpecFormatWriter extends FormatWriter {
        public SpecFormatWriter() {}
        public abstract boolean write(DisplayWindow window, PrintWriter w, int indentation, int offset);
    }


   class SpecAFormatWriter extends SpecFormatWriter {

        public boolean write(DisplayWindow window, PrintWriter w, int indentation, int offset) {
            //<window title="SiteA Viewer" w="800" h="700">
            Dimension d=window.getSize();
            write(w, indentation, "<window title=\""+window.getTitle()+"\" w=\""+d.width+"\" h=\""+d.height+"\"");
            indentation+=offset;

            Display display = window.getDisplay();
            Shapes shapes = display.getShapes();
            LabelRenderers labelRenderers = display.getLabelRenderers();
            ShapeRenderers shapeRenderers = display.getShapeRenderers();
            SlotChangeListeners slotListeners = display.getSlotListeners();

            // write out shapes
            String shapeStr[] = shapes.toXML();
            writeLines(w, indentation, shapeStr);
            w.println("\n\n");
            // write out label renderers
            String lblStr[] = labelRenderers.toXML();
            writeLines(w, indentation, lblStr);
            w.println("\n\n");
            // write out shape renderers
            String srStr[] = shapeRenderers.toXML();
            writeLines(w, indentation, srStr);
            w.println("\n\n");

            // write out slot change listener defs
            writeSlotListenerDefs(w, slotListeners.getAll(), indentation, offset);
            w.println("\n\n");

            // write out the containers
            ShapeContainer root = display.getRootContainer();
            writeContainerSpec(root, w, indentation, offset);

            write(w, indentation, "</window>");
            return true;
        }
      /*
      <container id="job-queue" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeContainer" prototype="true" shape="RoundRect1" labelrender="queueLabelRenderer" shaperender="queueRenderer">
           <layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalBoxLayout" left="5" right="5" bottom="5" top="25" hpadding="10" vpadding="10"/>
           <framepredicate isa="queue"  parentRelationship="contains"/>

           <component id="job" idframeslot="name" class="org.cougaar.core.qos.frame.visualizer.ShapeGraphic" prototype="true" shape="circle1" labelrender="jobLabelRenderer" shaperender="waitingJobRenderer">
               <framepredicate isa="job"  parentRelationship="ServicedAt"/>
               <slotlistener name="jobWatcher"/>
           </component>
       </container>
      */
      void writeContainerSpec(ShapeGraphic g, PrintWriter w, int indentation, int offset) {
          if (g.hasFrame())
            return;
          FramePredicate fp = g.getFramePredicate();
          String tag = ((! (g instanceof ShapeContainer) ) ? "component" : "container");

          String str1 = "<"+tag+" id=\""+(fp!=null ? fp.getKind() : g.getId())+"\" idframeslot=\""+g.getFrameIdSlotName()+
                  "\" class=\""+g.getClass().getName()+"\" prototype=\""+g.isPrototype()+"\" shape=\"circle1\" labelrender=\"jobLabelRenderer\" shaperender=\"waitingJobRenderer\">";
          w.println();
          write(w, indentation+offset, str1);
          if (g instanceof ShapeContainer)
              writeShapeLayout(w, ((ShapeContainer)g).getLayout(), indentation+offset);
          if (fp != null)
              writePredcate(w, fp, indentation);
          writeSlotListeners(w, g.getSlotListeners(), indentation+offset);

          if (g.isContainer()){
              ShapeContainer container = (ShapeContainer) g;
              // write prototypes
              ShapeGraphic gr;
              for (Iterator ii=container.getPrototypes().iterator(); ii.hasNext();) {
                  gr = (ShapeGraphic) ii.next();
                  writeContainerSpec(gr, w, indentation+offset, offset);
              }
              //w.println("\n");
              // write children (w/o a frame -- these are instantiated based on the prototypes)
              for (Iterator ii=container.getChildren().iterator(); ii.hasNext();) {
                  gr = (ShapeGraphic) ii.next();
                  writeContainerSpec(gr, w, indentation+offset, offset);
                  //w.println("\n");
              }
          }
          //w.println("\n");
          write(w, indentation+offset, "</"+tag+">");
      }

      //<layout class="org.cougaar.core.qos.frame.visualizer.layout.HorizontalBoxLayout" left="5" right="5" bottom="5" top="25" hpadding="10" vpadding="10"/>
      public void writeShapeLayout(PrintWriter w, ShapeLayout l, int indentation) {
          if (l != null) {
              String str = "<layout class=\""+l.getClass().getName()+"\" left=\""+l.left()+"\" right=\""+l.right()+"\" bottom=\""+l.bottom()+"\" top=\""+l.top()+"\" hpadding=\""+l.hpadding()+"\" vpadding=\""+l.vpadding()+"\"/>";
              write(w, indentation, str);
          }
      }
       //<framepredicate isa="job"  parentRelationship="ServicedAt"/>
      public void writePredcate(PrintWriter w, FramePredicate fp, int indentation) {
          if (fp != null) {
              String str = "<framepredicate isa=\""+fp.getKind()+"\" parentRelationship=\""+fp.getParentRelationship()+"\"/>";
              write(w, indentation, str);
          }
      }
       //<slotlistener name="jobWatcher"/>
       public void writeSlotListeners(PrintWriter w, Collection l, int indentation) {
           if (l != null && l.size() > 0) {
               SlotChangeListener sl;
               for (Iterator ii=l.iterator(); ii.hasNext();) {
                   sl = (SlotChangeListener) ii.next();
                   write(w, indentation, "<slotlistener name=\""+sl.getName()+"\"/>");
               }
           }
       }


       public void writeSlotListenerDefs(PrintWriter w, Collection l, int indentation, int offset) {
           SlotChangeListener sl;
           for (Iterator ii=l.iterator(); ii.hasNext();) {
               sl = (SlotChangeListener) ii.next();
               w.println(sl.toXML(indentation, offset));
           }
       }
   }

}