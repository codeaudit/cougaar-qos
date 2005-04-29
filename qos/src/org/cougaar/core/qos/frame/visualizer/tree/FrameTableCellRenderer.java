package org.cougaar.core.qos.frame.visualizer.tree;

import org.cougaar.core.qos.frame.SlotDescription;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 27, 2005
 * Time: 1:33:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class FrameTableCellRenderer extends JLabel implements TableCellRenderer {
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    FrameTableModel model;
    Font plainFont=null, boldFont=null, boldItalic=null, italicFont=null;
    boolean enableTooltips = true;


    public FrameTableCellRenderer(boolean isBordered, boolean enableTooltips, FrameTableModel model) {
        this.isBordered = isBordered;
        this.enableTooltips = enableTooltips;
        this.model = model;
        if (plainFont == null || boldFont == null || italicFont == null) {
            plainFont = getFont().deriveFont(Font.PLAIN);
            boldFont  = getFont().deriveFont(Font.BOLD);
            boldItalic = getFont().deriveFont(Font.ITALIC | Font.BOLD);
            italicFont= getFont().deriveFont(Font.ITALIC);
	    }
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
        setText(value.toString());
        SlotDescription sd = model.getSlotDescription(row);

        Font font = (model.isReadOnly(row) ? (column==0 ? boldItalic : italicFont) : (column==0 ? boldFont : plainFont));
        boolean slotDef = model.isSlotDefinition(row);
        boolean inherited = (sd != null ? !sd.is_overridden : model.isInherited(row));
        setBackground( (slotDef ? ColorDefs.slotDefinitionColor : (inherited ? ColorDefs.inheritedSlotColor :  ColorDefs.localSlotColor)));
        setFont(font);
        if (enableTooltips) {

            String ttip = null;
            if (sd != null)
                ttip = "Prototype: "+sd.prototype+ " "+ (sd.is_overridden ? "overridden" : "inherited");
            setToolTipText(ttip);
        }
        if (isBordered)
           drawBorder(isSelected, table);
        return this;
    }

    void drawBorder(boolean isSelected, JTable table) {
         if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null)
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getSelectionBackground());
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null)
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getBackground());
                setBorder(unselectedBorder);
            }
        }
    }
}