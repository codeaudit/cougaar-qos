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
import java.util.Enumeration;

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
    FrameModel frameModel;

    public DisplayWindow(FrameModel frameModel, File xmlFile) {
        super("");
        this.frameModel = frameModel;
        frameModel.setDisplayWindow(this);
        tabbedPane = new JTabbedPane();
        display = new Display(frameModel, xmlFile);
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
        FrameModel h;
        public SetFrameHelper(FrameModel frameModel) {h = frameModel;}
        public void run() {  setFrameHelper(h); }
    }
    /*
    class AddedFrames implements Runnable {
        Collection addedFramesEn;
        public AddedFrames(Collection e) { addedFramesEn = e; }
        public void run() { addFrames(addedFramesEn);}
    }
    class RemovedFrames implements Runnable {
        Collection removedFramesEn;
        public RemovedFrames(Collection e) { removedFramesEn = e; }
        public void run() { removeFrames(removedFramesEn);}
    } */
    class UpdateFrameView implements Runnable {
        public  UpdateFrameView(){}
        public void run() { updateFrameView();}
    }
    class TickOccured implements Runnable {
        TickEvent t;
        public TickOccured(TickEvent te) { t=te; }
        public void run() {  tickEventOccured(t); }
    }
    class StateChanged implements Runnable {
        ChangeEvent ev;
        public StateChanged(ChangeEvent e) {   ev = e; }
        public void run() { stateChanged(ev);}
    }


    public void setFrameHelper(FrameModel frameModel) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new SetFrameHelper(frameModel));
            return;
        }
        //this.frameModel = frameModel;
        //display.setFrameHelper(frameModel);
        //containerView.buildContainerTree(display.getRootContainer());
        frameView.buildFrameTree(frameModel);
    }
    /*
    public void addFrames(Collection newFrames) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new AddedFrames(newFrames));
            return;
        }
        display.addedFrames(newFrames);
    }

    public void removeFrames(Collection removedFrames) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new RemovedFrames(removedFrames));
            return;
        }
        display.removedFrames(removedFrames);
    }
    */

    public Display getDisplay() {
        return display;
    }

    public void updateFrameView() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new UpdateFrameView());
            return;
        }
        frameView.buildFrameTree(frameModel);
    }

    public ShapeGraphic findShape(org.cougaar.core.qos.frame.Frame f) {
        return frameModel.getGraphic(f); //display.findShape(f);
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
