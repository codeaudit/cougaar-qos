package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.visualizer.ShapeGraphic;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:49:58 AM
 * To change this template use File | Settings | File Templates.
 */

public class ShapeGraphicTableModel extends TBLModel {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
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

  @Override
public void clear() {
    graphic = null;
    fireTableDataChanged();
  }

  @Override
public String getColumnName(int col) {
    if (col > -1 && col < columnNames.length)
      return columnNames[col];
    return "";
  }

  @Override
public int getColumnCount() { return columnNames.length;}
  @Override
public int getRowCount() { return 0;}

  @Override
public Object getValueAt(int row, int col) {
      return null;

  }

  @Override
public Class getColumnClass(int col) {
      return String.class;
  }

  @Override
public boolean isCellEditable(int row, int col) {
    return false;
  }

  @Override
public void setValueAt(Object value, int row, int col) {
  }

}