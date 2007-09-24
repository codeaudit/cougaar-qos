/*
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright> 
 */
package org.cougaar.qos.qrs.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JTree;
import javax.swing.border.Border;

/**
 * The main window class of the RSS visualization.
 */
public class MainWindow extends JFrame {

    private static final Border RaisedBorder = BorderFactory.createRaisedBevelBorder();

    private static final Dimension Size = new Dimension(200, 200);
    private static final Point Location = new Point(20, 20);

    private final ResourceContextTreeModel dataTreeModel;
    private final JTree dataTree;

    public MainWindow(String title) {
        super(title);
        addWindowListener(new CloseListener());
        setSize(Size);
        setLocation(Location);
        setBackground(Color.lightGray);
        UIManager.put("Label.foreground", Color.black);

        dataTreeModel = new ResourceContextTreeModel();
        dataTree = new ResourceContextTree(dataTreeModel);
        final JScrollPane dataSP = new JScrollPane();
        dataSP.getViewport().add(dataTree);
        dataSP.setBorder(RaisedBorder);

        getContentPane().add("Center", dataSP);
        getContentPane().add("North", makeButtonBar());

        setVisible(true);

    }

    private JPanel makeButtonBar() {
        JPanel panel = new JPanel();
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        panel.add(refresh);
        return panel;
    }

    private void refresh() {
        dataTreeModel.update();
        dataTree.repaint();
    }

    private class CloseListener extends WindowAdapter {
        public void windowClosed(WindowEvent e) {
            // ?
        }

        public void windowClosing(WindowEvent e) {
            setVisible(false);
            dispose();
        }
    }

}
