package org.cougaar.core.qos.frame.visualizer.tree;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.*;
import java.util.Vector;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Enumeration;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:49:58 AM
 * To change this template use File | Settings | File Templates.
 */

public class FrameTableModel extends TBLModel {
    org.cougaar.core.qos.frame.Frame frame;
    ArrayList frameSlotNames;
    Properties frameSlots;


  public FrameTableModel(){
    super();
    frame = null;
    frameSlots = new Properties();
    frameSlotNames = new ArrayList();
    setColumns(new String[] {"slot","value"},new int[]{100,150});
  }

  public void set(org.cougaar.core.qos.frame.Frame frame) {
    this.frame = frame;
    this.frameSlots = frame.getLocalSlots();
    this.frameSlotNames = new ArrayList();
    for (Enumeration ee= frameSlots.propertyNames(); ee.hasMoreElements();)
        frameSlotNames.add(ee.nextElement());
    if (table != null)
        setupColumns(table);

    fireTableStructureChanged();
  }

  public void clear() {
    frame = null;
    frameSlots.clear();
    frameSlotNames.clear();
    fireTableDataChanged();
  }

  public String getColumnName(int col) {
    if (col > -1 && col < columnNames.length)
      return columnNames[col];
    return "";
  }

  public int getColumnCount() { return columnNames.length;}
  public int getRowCount() { return frameSlotNames.size();}

  public Object getValueAt(int row, int col) {
    if (row >-1 && row < frameSlotNames.size())
       return ( col == 0 ? frameSlotNames.get(row) : frameSlots.getProperty((String) frameSlotNames.get(row)));
    return null;
  }

  public Class getColumnClass(int col) {
      return String.class;
  }

  public boolean isCellEditable(int row, int col) {
    return (col == 0 ? false : true);
  }

  public void setValueAt(Object value, int row, int col) {
    if (row >-1 && row < frameSlotNames.size()) {
        frameSlots.setProperty((String)frameSlotNames.get(row), (String) value);
        frame.setValue((String)frameSlotNames.get(row), value);
    }
  }

}