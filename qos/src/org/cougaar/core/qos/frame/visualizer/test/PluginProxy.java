package org.cougaar.core.qos.frame.visualizer.test;

import org.cougaar.core.qos.frame.visualizer.*;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.Frame;
import org.cougaar.core.qos.frame.RelationFrame;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.util.ConfigFinder;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 23, 2005
 * Time: 3:31:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginProxy {
    public boolean changed = false;
    public boolean newFramesPresent;

    private String frameSetName;
    private DisplayWindow pluginDisplay;
    private ArrayList frameCache;


    FrameHelper helper;
    static String TICK_EVENT_LABEL = "TICK";
    int tickNumber = 0;


    public void start()
    {
        frameSetName = "simulator";
        frameCache = new ArrayList();
        newFramesPresent = false;
        changed = false;

        String specFileName = "c:/work/sassi_dev/specA.xml";
        File xml_file = new File(specFileName);//ConfigFinder.getInstance().locateFile(specFileName);
        pluginDisplay = new DisplayWindow(xml_file);
        //SwingUtilities.invokeLater(new CreateWindowHelper(xml_file));
    }
    /*
    class CreateWindowHelper implements Runnable {
        File xmlFile;
        public CreateWindowHelper(File xmlFile){
            FrameVisualizerPlugin.CreateWindowHelper.this.xmlFile = xmlFile;
        }
        public void run() {
            pluginDisplay = new DisplayWindow(xmlFile);
        }
    } */

    public static void p(String msg) {
        System.out.println(msg);
    }


    public void execute()
    {
        //if (changed)
	      //  p("There are changes.");

	// New Frames
	Iterator en = Main.getAddedList();
	FrameSet frameSet = null;
	while (en.hasNext()) {
	    Frame frame = (Frame) en.next();
	    if (frameSet == null)
		frameSet = frame.getFrameSet();
	    p("Observed added "+frame);
	    // Handle new Frame
	    frameCache.add(frame);
	    newFramesPresent = true;
	}

	if (helper == null || newFramesPresent) {
	    p("setting frames on the display");
	    helper = new FrameHelper(frameCache, frameSet);
	    pluginDisplay.setFrameHelper(helper);
	    newFramesPresent = false;
	}

  
	// Changed Frames
	en = Main.getChangedList();
	ArrayList transitions = new ArrayList();
    ChangeKeeper k;
    Frame frame;
	while (en.hasNext()) {
	    k = (ChangeKeeper) en.next();
        frame = k.getFrame();
	    p("Observed changed "+k.getFrame());

	    Collection changes = Main.getChangeReports(frame);
	    // A collection of Frame.Change instances.
	    if (changes != null) {
            if (frame.isa("relationship"))
                transitions.addAll(processRelationshipChanges((RelationFrame)frame, changes.iterator()));
        }
	}
	if (transitions.size() > 0) {
	    //pluginDisplay.updateFrameView();
	    pluginDisplay.tickEventOccured(new TickEvent(this, tickNumber++, TICK_EVENT_LABEL, transitions));
    }

   /*
	// Remove Frames.  Won't happen.
	en = Main.getRemovedList();
	while (en.hasMoreElements()) {
	    Frame frame = (Frame) en.nextElement();
	    p("Observed removed "+frame);
	}
    */
    }




    protected Collection processRelationshipChanges(RelationFrame frame, Iterator changes) {
        String slotName;
        Object value;
        ShapeGraphic child, parent;
        Frame fch,fp;
        ArrayList transitions = new ArrayList();

        while (changes.hasNext()) {
            Frame.Change change = (Frame.Change) changes.next();
            // Handle change to existing frame
            slotName = change.getSlotName();
            value    = change.getValue();

            //p("frame "+frame+"  changed   slot="+slotName+"  value="+value+" child="+frame.getValue("child-value"));

            //ff = helper.getParentAndChild(frame);
            fch = frame.relationshipParent();
            fp  = frame.relationshipChild();
                //p("processRelationshipChanges parentFrame="+fp+"  childFrame="+ff[1]);

            parent = pluginDisplay.findShape(fp);
            child  = pluginDisplay.findShape(fch);
            if (parent == null || child == null) {
                //p("did not find shapes");
                continue;
            }
            if (child.getParent() == null || child.getParent().getId().equals(parent.getId())) {
                //p("error: old parent = "+child.getParent()+" new parent="+parent+", ignoring...");
                continue;
            }
            transitions.add(new Transition(child, child.getParent(), (ShapeContainer)parent));
        }
        return transitions;
    }

}
