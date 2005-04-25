package org.cougaar.core.qos.frame.visualizer.test;

import org.cougaar.core.qos.frame.*;
import org.cougaar.core.util.UID;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 16, 2005
 * Time: 3:51:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
        static HashMap sets, changeKeepers;
        static PluginProxy plugin;

    public final static void main(String args[]) {
        TestFrameSetParser parser = new TestFrameSetParser();
        String frames = "c:/work/sassi_dev/simulator-frames.xml";
        String protos = "c:/work/sassi_dev/simulator-protos.xml";
        FrameSet frameSet = parser.parseFrameSetFiles("simulator", new String[]{protos,frames});
        //FrameSet frameSet = parser.parseFrameSetFile("simulator-frames", frames);
        changeKeepers = new HashMap();
	    sets = new HashMap();
        //sets.put("simulator-protos", protoSet);
        sets.put("simulator-frames", frameSet);


        plugin = new PluginProxy();
        plugin.start();
        plugin.execute();
    }




    public static Iterator getAddedList() {
        TestFrameSet s = (TestFrameSet)sets.get("simulator-frames");
        return s.getAllFrames().iterator();
    }

    public static void addChange(Frame frame, String slotName, Object newValue) {
        synchronized (changeKeepers) {
           ChangeKeeper k = (ChangeKeeper) changeKeepers.get(frame);
           if (k == null) {
               k = new ChangeKeeper(frame);
               changeKeepers.put(frame, k);
           }
           k.addChange(new Frame.Change(frame.getUID(), slotName, newValue));
        }
    }
    public static Iterator getChangedList() {
        synchronized (changeKeepers) {
            return changeKeepers.values().iterator();
        }
    }

    public static Collection getChangeReports(Frame frame) {
        synchronized (changeKeepers) {
            ChangeKeeper k = (ChangeKeeper) changeKeepers.get(frame);
            if (k != null)
                return k.getChangeReports();
        }
        return null;
    }
}

