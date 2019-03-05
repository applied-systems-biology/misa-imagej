package org.hkijena.misa_imagej.ui.components.renderers;

import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MISASampleTableCellRender extends JLabel implements TableCellRenderer {

    private MonochromeColorIcon icon = new MonochromeColorIcon(UIUtils.getIconFromResources("sample-template.png"));

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(value instanceof MISASample) {
            setText(((MISASample) value).getName());
            icon.setColor(((MISASample) value).toColor());
            setIcon(icon);

            if(isSelected || hasFocus) {
                setBackground(new Color(184, 207, 229));
            }
            else {
                setBackground(new Color(255,255,255));
            }
        }
        else {
            setText(null);
            setIcon(null);
        }
        return this;
    }
}
