package org.cougaar.core.qos.frame.visualizer.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.PrototypeFrame;
import org.cougaar.core.qos.frame.SlotDescription;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 15, 2005
 * Time: 10:49:58 AM
 * To change this template use File | Settings | File Templates.
 */

public class FrameTableModel extends TBLModel {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   org.cougaar.core.qos.frame.Frame frame;
    ArrayList frameSlotNames;
    Properties frameSlots;
    HashMap localSlots;
    HashMap inheritedSlots;
    HashMap slotDefs;
    HashMap slotDescriptions;


  public FrameTableModel(){
    super();
    frame = null;
    frameSlots = new Properties();
    frameSlotNames = new ArrayList();
    localSlots = new HashMap();
    inheritedSlots = new HashMap();
    slotDefs = new HashMap();
    slotDescriptions = new HashMap();
    setColumns(new String[] {"slot","value"},new int[]{100,150});
  }


    public boolean isReadOnly() {
        return (frame != null && frame instanceof PrototypeFrame);
    }

    public boolean isReadOnly(int row) {
        if (isReadOnly())
            return true;
        SlotDescription slotDesc = getSlotDescription(row);
        return (slotDesc != null ? !slotDesc.is_writable : false);
    }

    public SlotDescription getSlotDescription(int row) {
        String slotName = (String) frameSlotNames.get(row);
        return getSlotDescription(slotName);
    }

    public SlotDescription getSlotDescription(String slotName) {
         return (SlotDescription) slotDescriptions.get(slotName);
    }


  public void set(org.cougaar.core.qos.frame.Frame f) {
    this.frame = f;
    this.frameSlots = (f instanceof DataFrame ? ((DataFrame)frame).getAllSlots() : frame.getLocalSlots());
    if (f instanceof PrototypeFrame) {
        Properties sdefs = ((PrototypeFrame)f).getSlotDefinitions();
        String name;
        if (sdefs != null) {
             for (Enumeration ee=sdefs.propertyNames(); ee.hasMoreElements();) {
                 name = (String) ee.nextElement();
                 if (frameSlots.get(name) == null)  {
                     Object v = f.getValue(name);
                     frameSlots.put(name, (v != null ? v : ""));//sdefs.getProperty(name));
                 }
                 slotDefs.put(name, "");//sdefs.getProperty(name));
             }
        }
    }


    Properties locSlots = frame.getLocalSlots();
    this.frameSlotNames = new ArrayList();

    String name;
    for (Enumeration ee= frameSlots.propertyNames(); ee.hasMoreElements();)  {
        name = (String) ee.nextElement();
        frameSlotNames.add(name);
        if (locSlots.get(name) == null && inheritedSlots.get(name)==null)
            inheritedSlots.put(name, frameSlots.get(name));
        else if (locSlots.get(name) != null && localSlots.get(name)==null)
            localSlots.put(name, frameSlots.get(name));

    }
    if (frame instanceof DataFrame) {
            SlotDescription sd;
            for (Iterator ii=((DataFrame)frame).slotDescriptions().values().iterator(); ii.hasNext();) {
                sd = (SlotDescription) ii.next();
                slotDescriptions.put(sd.name, sd);
                //System.out.println(sd.name+" Prototype: "+sd.prototype+ " "+ (sd.is_overridden ? "overridden" : "inherited"));
            }
    }
    if (table != null)
        setupColumns(table);

    fireTableStructureChanged();
  }

  public boolean isInherited(int row) {
       return isInherited((String) frameSlotNames.get(row));
  }

  public boolean isSlotDefinition(int row) {
      String slotName = (String) frameSlotNames.get(row);
      return slotDefs.get(slotName) != null;
  }

  public boolean isInherited(String slotName) {
      return inheritedSlots.get(slotName) != null;
  }

  @Override
public void clear() {
    frame = null;
    frameSlots.clear();
    frameSlotNames.clear();
    localSlots.clear();
    inheritedSlots.clear();
    slotDefs.clear();
    slotDescriptions.clear();
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
public int getRowCount() { return frameSlotNames.size();}

  @Override
public Object getValueAt(int row, int col) {
      if (row >-1 && row < frameSlotNames.size()) {
        Object val= ( col == 0 ? frameSlotNames.get(row) : frameSlots.get(frameSlotNames.get(row)));
	if (val == null || val.equals(DataFrame.NIL))
	    return "";
	return val;
      }
    return null;
  }

  @Override
public Class getColumnClass(int col) {
      return Object.class;
  }

  @Override
public boolean isCellEditable(int row, int col) {
    return (col == 0 ? false : !isReadOnly(row));
  }

  @Override
public void setValueAt(Object value, int row, int col) {
    if (row >-1 && row < frameSlotNames.size() && !isReadOnly(row)) {
        frameSlots.setProperty((String)frameSlotNames.get(row), (String) value);
        //frame.setValue((String)frameSlotNames.get(row), value);
    }
  }

}
