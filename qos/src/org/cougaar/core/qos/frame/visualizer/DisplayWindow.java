package org.cougaar.core.qos.frame.visualizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.cougaar.core.qos.frame.visualizer.tree.ContainerTreeView;
import org.cougaar.core.qos.frame.visualizer.tree.FrameTreeView;
import org.cougaar.core.qos.frame.visualizer.util.HTMLTreeWriter;
import org.cougaar.core.qos.frame.visualizer.util.ViewConfigParser;
import org.cougaar.core.service.ThreadService;

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

    public DisplayWindow(FrameModel frameModel, URL xmlFile, ThreadService tsvc) {
        super("");
        this.frameModel = frameModel;
        //frameModel.setDisplayWindow(this);
        tabbedPane = new JTabbedPane();
        display = new Display(frameModel, xmlFile, tsvc);
        Component controlPanel = display.getControlPanel();
        JPanel displPanel = new JPanel(new BorderLayout());
        displPanel.add(display, BorderLayout.CENTER);
        displPanel.add(controlPanel, BorderLayout.SOUTH);
        containerView = new ContainerTreeView(frameModel, display);
        frameView = new FrameTreeView(frameModel);
        frameSnapshotView = new FrameSnapshotView(frameView, containerView);

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
        setTitle(title);
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
        ContainerTreeView containerView;
        JLabel htmlView;
        JRadioButton frameTree, containerTree;
        boolean showFrameView =  true;

        public FrameSnapshotView(FrameTreeView frameView, ContainerTreeView containerView) {
            super(new BorderLayout());
            this.frameTreeView = frameView;
            this.containerView = containerView;

            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalGlue());

            frameTree =  new JRadioButton(new FrameTreeAction());
            frameTree.setSelected(true);
            containerTree = new JRadioButton(new ContainerTreeAction());

            ButtonGroup group = new ButtonGroup();
            group.add(frameTree);
            group.add(containerTree);

            buttonBox.add(frameTree);
            buttonBox.add(containerTree);
            buttonBox.add(new JButton(new RefreshAction()));
            add(buttonBox, BorderLayout.NORTH);

            htmlView = new JLabel();
            JScrollPane sp = new JScrollPane(htmlView);
            add(sp, BorderLayout.CENTER);
        }

        public void doRefresh() {
            //TreeNode root = frameTreeView.getRootNode();
            StringWriter sw = new StringWriter();
            PrintWriter w = new PrintWriter(sw);
            if (showFrameView)
               frameTreeView.rebuildTree();
            else
                containerView.rebuildTree();

            HTMLTreeWriter.write(w, (showFrameView ? frameTreeView.getRootNode() : containerView.getRootNode()), 5,5);
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

        class ContainerTreeAction extends AbstractAction {
           public ContainerTreeAction() {
               super("View Tree");
           }
           public void actionPerformed(ActionEvent e) {
               FrameSnapshotView.this.showFrameView = false;
           }
        }
        class FrameTreeAction extends AbstractAction {
           public FrameTreeAction() {
               super("Frame Tree");
           }
           public void actionPerformed(ActionEvent e) {
               FrameSnapshotView.this.showFrameView = true;
           }
        }

    }

}
