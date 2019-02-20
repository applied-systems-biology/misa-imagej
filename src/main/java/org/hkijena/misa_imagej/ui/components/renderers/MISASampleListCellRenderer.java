package org.hkijena.misa_imagej.ui.components.renderers;

import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;

import javax.swing.*;
import java.awt.*;

public class MISASampleListCellRenderer extends JLabel implements ListCellRenderer<MISASample> {

    private MonochromeColorIcon icon = new MonochromeColorIcon(UIUtils.getIconFromResources("sample-template.png"));

    public MISASampleListCellRenderer() {
        this.setIcon(icon);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MISASample> list, MISASample value, int index, boolean isSelected, boolean cellHasFocus) {

        if(value == null) {
            setText("Nothing selected");
            return this;
        }

        setText(value.getName());
        icon.setColor(value.toColor());

        if(isSelected || cellHasFocus) {
            setBackground(new Color(184, 207, 229));
        }
        else {
            setBackground(new Color(255,255,255));
        }

        return this;
    }
}
