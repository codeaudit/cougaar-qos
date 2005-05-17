package org.cougaar.core.qos.frame.visualizer;

import org.cougaar.core.qos.frame.visualizer.util.ViewConfigParser;
import org.cougaar.core.qos.frame.visualizer.util.ViewConfigWriter;
import org.cougaar.core.qos.frame.visualizer.util.HTMLTreeWriter;
import org.cougaar.core.qos.frame.visualizer.util.TreeWriter;
import org.cougaar.core.qos.frame.visualizer.tree.ContainerTreeView;
import org.cougaar.core.qos.frame.visualizer.tree.FrameTreeView;
import org.cougaar.core.qos.frame.visualizer.event.TickEvent;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 12:43:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayWindow extends JFrame { //implements ChangeListener  {
    Display display;
    ContainerTreeView containerView;
    FrameTreeView frameView;
    FrameSnapshotView frameSnapshotView;
    JTabbedPane tabbedPane;
    FrameModel frameModel;

    public DisplayWindow(FrameModel frameModel, URL xmlFile) {
        super("");
        this.frameModel = frameModel;
        //frameModel.setDisplayWindow(this);
        tabbedPane = new JTabbedPane();
        display = new Display(frameModel, xmlFile);
        Component controlPanel = display.getControlPanel();
        JPanel displPanel = new JPanel(new BorderLayout());
        displPanel.add(display, BorderLayout.CENTER);
        displPanel.add(controlPanel, BorderLayout.SOUTH);
        containerView = new ContainerTreeView(frameModel, display);
        frameView = new FrameTreeView(frameModel);
        frameSnapshotView = new FrameSnapshotView(frameView);

        tabbedPane.addTab("Graphic", displPanel);
        tabbedPane.addTab("View Tree", containerView);
        tabbedPane.addTab("Frames", frameView);
        tabbedPane.addTab("Snapshots", frameSnapshotView);

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
        //display.addChangeListener(this);
        display.reset();

        //ViewConfigWriter writer = new ViewConfigWriter();
        //writer.generate(this, "testingSpec.xml", 5, 5);
    }

    public Display getDisplay() {
        return display;
    }

    public ShapeGraphic findShape(org.cougaar.core.qos.frame.Frame f) {
        return frameModel.getGraphic(f); //display.findShape(f);
    }



    class FrameSnapshotView extends JPanel {
        FrameTreeView frameTreeView;
        JLabel htmlView;

        public FrameSnapshotView(FrameTreeView frameView) {
            super(new BorderLayout());
            this.frameTreeView = frameView;
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalGlue());
            buttonBox.add(new JButton(new RefreshAction()));
            add(buttonBox, BorderLayout.NORTH);

            htmlView = new JLabel();
            JScrollPane sp = new JScrollPane(htmlView);
            add(sp, BorderLayout.CENTER);
        }

        public void doRefresh() {
            TreeNode root = frameTreeView.getRootNode();
            StringWriter sw = new StringWriter();
            PrintWriter w = new PrintWriter(sw);
            HTMLTreeWriter.write(w, root, 5,5);
            htmlView.setText(sw.toString());
        }

        class RefreshAction extends AbstractAction {
            public RefreshAction() {
                super("Refresh");
            }
            public void actionPerformed(ActionEvent e) {
                doRefresh();
            }
        }

    }

}
