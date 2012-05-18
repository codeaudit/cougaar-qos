package org.cougaar.core.qos.frame.visualizer.tree;

import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:49:58 AM
 * To change this template use File | Settings | File Templates.
 */

public abstract class TBLModel extends AbstractTableModel {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   JTable table;
    String columnNames[] = null;
    int    columnWidths[]= null;

  public TBLModel(){
    super();
    setColumns(new String[] {"",""},new int[]{100,150});
  }

  public void setTable(JTable table) {
    this.table = table;
  }

  public void setColumns(String colNames[], int colWidths[]) {
    columnNames = colNames;
    columnWidths= colWidths;
  }

  @Override
public String getColumnName(int col) {
    if (col > -1 && col < columnNames.length)
      return columnNames[col];
    return "";
  }

  public int getColumnCount() {
       return columnNames.length;
  }

  public abstract void clear();
  public abstract int getRowCount();
  public abstract Object getValueAt(int row, int col);
  @Override
public abstract Class getColumnClass(int col);
  @Override
public abstract boolean isCellEditable(int row, int col);
  @Override
public abstract void setValueAt(Object value, int row, int col);

  public void setupColumns(JTable table) {
    //Utils.p("setupColumns");
    TableColumnModel cm = table.getColumnModel();
    TableColumn col;
    int i, w, colCount = cm.getColumnCount();
    DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox());
    ((JCheckBox)editor.getComponent()).setHorizontalAlignment(SwingConstants.CENTER);
    for (i=0; i < colCount; i++) {
      col = cm.getColumn(i);
      if (columnWidths != null) {
	w = 10;//getColWidth(table, i);
	if (columnWidths.length > i)
	  col.setPreferredWidth(columnWidths[i] > w ? columnWidths[i]: w);
      }
    }

    int totalWidth = 0;
    for (i=0; i < columnWidths.length; i++) {
      totalWidth+=columnWidths[i];
      //Utils.p("width["+i+"] = "+columnWidths[i]);
    }

    if (totalWidth > 0) {

      int th = table.getHeight();
      Dimension d = new Dimension(totalWidth, th);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      table.setPreferredScrollableViewportSize(d);
      //table.setPreferredSize(d);
      //if (TableDisplay.instance != null && TableDisplay.instance.scrollPane != null)
      //TableDisplay.instance.scrollPane.setPreferredSize(d);
    }
  }
}