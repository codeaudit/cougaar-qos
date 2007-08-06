/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
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
 *  The main window class of the RSS visualization.
 */
public class MainWindow 
    extends JFrame 
{

    private static final Border RaisedBorder = 
	BorderFactory.createRaisedBevelBorder();

    private static final Dimension Size = new Dimension(200, 200);
    private static final Point Location = new Point(20, 20);

    private ResourceContextTreeModel dataTreeModel;
    private JTree dataTree;

    public MainWindow(String title)
    {
	super(title);
        addWindowListener( new CloseListener());
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
    

    private JPanel makeButtonBar()
    {
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

    private void refresh()
    {
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
 
