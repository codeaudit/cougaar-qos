package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

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

public class ShapeGraphicTableModel extends TBLModel {
    ShapeGraphic graphic;



  public ShapeGraphicTableModel(){
    super();
    graphic = null;

    setColumns(new String[] {"property","value"},new int[]{100,150});
  }

  public void set(ShapeGraphic g) {
    this.graphic = g;
    if (table != null)
        setupColumns(table);
    fireTableStructureChanged();
  }

  public void clear() {
    graphic = null;
    fireTableDataChanged();
  }

  public String getColumnName(int col) {
    if (col > -1 && col < columnNames.length)
      return columnNames[col];
    return "";
  }

  public int getColumnCount() { return columnNames.length;}
  public int getRowCount() { return 0;}

  public Object getValueAt(int row, int col) {
      return null;

  }

  public Class getColumnClass(int col) {
      return String.class;
  }

  public boolean isCellEditable(int row, int col) {
    return false;
  }

  public void setValueAt(Object value, int row, int col) {
  }

}