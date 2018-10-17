package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UIHelper {

    public static final Insets UI_PADDING = new Insets(4,4,4,4);

    public static JLabel createDescriptionLabelUI(JPanel panel, String text, int row, int column) {
        JLabel description = new JLabel(text);
        panel.add(description, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.WEST;
                gridx = column;
                gridy = row;
                insets = UI_PADDING;
            }
        });
        return description;
    }

    public static JLabel createDescriptionLabelUI(JPanel panel, JSONSchemaObject obj, int row, int column) {
        JLabel description = createDescriptionLabelUI(panel, obj.getName(), row, column);
        description.setToolTipText(obj.description);
        return description;
    }

    public static JLabel backgroundColorJLabel(JLabel label, Color color) {
        label.setOpaque(true);
        label.setBackground(color);

        // Set the text color to white if needed
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));

        double v = (max + min) / 2.0;
        if(v < 0.5) {
            label.setForeground(Color.WHITE);
        }

        return label;
    }

    public static JLabel borderedJLabel(JLabel label) {
        label.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        return label;
    }

    public static void addFillerGridBagComponent(JComponent component, int row) {
        component.add(new JPanel(), new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 0;
                gridy = row;
                fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
                weightx = 1;
                weighty = 1;
            }
        });
    }

}
