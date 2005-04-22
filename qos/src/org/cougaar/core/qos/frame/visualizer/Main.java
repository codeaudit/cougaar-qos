package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.Surface;


import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;


import org.cougaar.core.qos.frame.visualizer.Display;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Mar 30, 2005
 * Time: 1:04:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {


    public static void createDisplay(String displConfig) {
        File f = new File(displConfig);
        Display d = new Display(f);
        //d.setFrames(org.cougaar.core.qos.frame.Frame.getFrames());
    }



//    public static void buildChildren(Collection frames, org.cougaar.core.qos.frame.visualizer.test.Frame parent) {
//        //System.out.println("buildChildren: "+parent.get("name"));
//        Collection relationships = getRelationshipFrames(frames, parent);
//        org.cougaar.core.qos.frame.Frame f, fk;
//
//        for (Iterator ii=relationships.iterator(); ii.hasNext();) {
//            f = (org.cougaar.core.qos.frame.visualizer.test.Frame)ii.next();
//            fk = findFrameByName(frames, (String) f.get("child-value"));
//            parent.add("contains"/*f.getPrototype().getName()*/, fk);
//            buildChildren(frames, fk);
//        }
//    }
/*
    public static void buildFrameHierarchy() {
        Collection frames = org.cougaar.core.qos.frame.visualizer.test.Frame.getFrames();
        org.cougaar.core.qos.frame.visualizer.test.Frame site = findFrame(frames, "site");
        buildChildren(frames, site);
    }

    protected static org.cougaar.core.qos.frame.visualizer.test.Frame findFrameByName(Collection frames, String name) {
        org.cougaar.core.qos.frame.visualizer.test.Frame f;
        for (Iterator ii=frames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.visualizer.test.Frame) ii.next();
            if (name.equals((String)f.get("name")))
               return f;
        }
        return null;
    }

    protected static org.cougaar.core.qos.frame.visualizer.test.Frame findFrame(Collection frames, String kind) {
        org.cougaar.core.qos.frame.visualizer.test.Frame f;
        for (Iterator ii=frames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.visualizer.test.Frame) ii.next();
            if (f.isa(kind))
               return f;
        }
        return null;
    }

    protected static Collection  findFrames(Collection frames, String kind) {
        ArrayList ch = new ArrayList();
        org.cougaar.core.qos.frame.visualizer.test.Frame f;
        for (Iterator ii=frames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.visualizer.test.Frame) ii.next();
            if (f.isa(kind))
               ch.add(f);
        }
        return ch;
    }

    protected static Collection getRelationshipFrames(Collection frames, org.cougaar.core.qos.frame.visualizer.test.Frame frame) {
        ArrayList ch = new ArrayList();
        org.cougaar.core.qos.frame.visualizer.test.Frame f;
        for (Iterator ii=frames.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.visualizer.test.Frame) ii.next();
            if (f.isa("relationship") && f.get("parent-value").equals(frame.get("name") ))
                ch.add(f);
        }
        return ch;
    }



    public static void printChildren(org.cougaar.core.qos.frame.visualizer.test.Frame parent) {
        org.cougaar.core.qos.frame.visualizer.test.Frame f, fk;
         System.out.print("===");
        //System.out.println(parent.get("name")+"\n*children:");
        Collection ch=parent.getChildren("contains");
        if (ch == null)
            return;
        for (Iterator ii=ch.iterator(); ii.hasNext();) {
            f = (org.cougaar.core.qos.frame.visualizer.test.Frame) ii.next();
            System.out.print(f.get("name")+", ");
            printChildren(f);
        }
    }
    public static void debugPrintFrames() {
        org.cougaar.core.qos.frame.visualizer.test.Frame f,fk;
        Collection frames = org.cougaar.core.qos.frame.visualizer.test.Frame.getFrames();
        org.cougaar.core.qos.frame.visualizer.test.Frame site = findFrame(frames, "site");

        for (Iterator ii=frames.iterator(); ii.hasNext();)
            printChildren((org.cougaar.core.qos.frame.visualizer.test.Frame)ii.next());
    }
*/
    public final static void main(String args[]) {
        //FrameProtoXmlReader protoReader = new FrameProtoXmlReader();
        //protoReader.parse(new File("simulator-protos.xml"));
        //FrameXmlReader frameReader = new FrameXmlReader();
        //frameReader.parse(new File("simulator-frames.xml"));

        //buildFrameHierarchy();
        //debugPrintFrames();
        //for (Iterator ii=org.cougaar.core.qos.frame.visualizer.test.Frame.getFrames().iterator(); ii.hasNext();)
          //  System.out.println(ii.next().toString());

        for (int i=0; i < args.length; i++)
            createDisplay(args[i]);

         //for (int i=0; i < args.length; i++)
           // createSiteView(args[i]);
    }


}
