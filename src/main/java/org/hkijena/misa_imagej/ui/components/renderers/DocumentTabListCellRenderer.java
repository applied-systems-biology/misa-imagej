package org.hkijena.misa_imagej.ui.components.renderers;

import org.hkijena.misa_imagej.utils.ui.DocumentTabPane;

import javax.swing.*;
import java.awt.*;

public class DocumentTabListCellRenderer extends JLabel implements ListCellRenderer<DocumentTabPane.DocumentTab> {

    public DocumentTabListCellRenderer() {
        setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends DocumentTabPane.DocumentTab> list, DocumentTabPane.DocumentTab value, int index, boolean isSelected, boolean cellHasFocus) {
        if(value != null) {
            setText(value.getTitle());
            setIcon(value.getIcon());
        }
        else {
            setText(null);
            setIcon(null);
        }
        return this;
    }
}
