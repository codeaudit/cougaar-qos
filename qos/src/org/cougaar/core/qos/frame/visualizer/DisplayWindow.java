package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.util.ViewConfigParser;
import org.cougaar.core.qos.frame.visualizer.test.TickEvent;
import org.cougaar.core.qos.frame.visualizer.tree.ContainerTreeView;
import org.cougaar.core.qos.frame.visualizer.tree.FrameTreeView;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 12:43:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayWindow extends JFrame implements ChangeListener  {
    Display display;
    ContainerTreeView containerView;
    FrameTreeView frameView;
    JTabbedPane tabbedPane;
    FrameHelper frameHelper;

    public DisplayWindow(File xmlFile) {
        super("");
        tabbedPane = new JTabbedPane();
        display = new Display(xmlFile);
	    Component controlPanel = display.getControlPanel();
        JPanel displPanel = new JPanel(new BorderLayout());
        displPanel.add(display, BorderLayout.CENTER);
        displPanel.add(controlPanel, BorderLayout.SOUTH);
        containerView = new ContainerTreeView();
        frameView = new FrameTreeView();

        tabbedPane.addTab("Graphic", displPanel);
        tabbedPane.addTab("View Tree", containerView);
        tabbedPane.addTab("Frames", frameView);

        ViewConfigParser.WindowSpec w = display.getWindowSpec();
        String title = w.getTitle();
        Dimension d = w.getSize();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowDeiconified(WindowEvent e) {  /*display.startClock(); display.animating.start();*/ }
            public void windowIconified(WindowEvent e) {
               /* if (display.animating != null) {
                    display.animating.stop();
                }
                display.bimg = null;; */
            }
        });
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
	    //getContentPane().add(controlPanel, BorderLayout.SOUTH);
        pack();
        setSize(d);
        setVisible(true);
        display.addChangeListener(this);
        display.reset();
    }

    class SetFrameHelper implements Runnable {
        FrameHelper h;
        public SetFrameHelper(FrameHelper helper) {
           h = helper;
        }
        public void run() {
            setFrameHelper(h);
        }
    }
    class UpdateFrameView implements Runnable {
        public  UpdateFrameView(){}
        public void run() {
             updateFrameView();
        }
    }
    class TickOccured implements Runnable {
        TickEvent t;
        public TickOccured(TickEvent te) {
            t=te;
        }
        public void run() {
            tickEventOccured(t);
        }
    }
    class StateChanged implements Runnable {
        ChangeEvent ev;
        public StateChanged(ChangeEvent e) {
            ev = e;
        }
        public void run() {
            stateChanged(ev);
        }
    }


    public void setFrameHelper(FrameHelper helper) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new SetFrameHelper(helper));
            return;
        } 
        this.frameHelper = helper;
	    display.setFrameHelper(helper);
	    //containerView.buildContainerTree(display.getRootContainer());
        frameView.buildFrameTree(helper);
    }

    public void updateFrameView() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new UpdateFrameView());
            return;
        }
        frameView.buildFrameTree(frameHelper);
    }

    public ShapeGraphic findShape(org.cougaar.core.qos.frame.Frame f) {
	    return display.findShape(f);
    }

    public void tickEventOccured(TickEvent tick) {
       if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new TickOccured(tick));
            return;
        }
	    display.tickEventOccured(tick);
    }


    public void stateChanged(ChangeEvent e) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new StateChanged(e));
            return;
        }
        if (e.getSource() == display) {
            containerView.buildContainerTree(display.getRootContainer());
        }
    }

    /*
    public void setFrames(Collection frames) {
        //display.setFrames(frames);
        //containerView.buildContainerTree(display.getRootContainer());
        //frameView.buildFrameTree(frames, "simulator");// TODO fix the name
	}*/

}
