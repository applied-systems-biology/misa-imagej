package org.hkijena.misa_imagej.ui.workbench;

import com.google.gson.JsonPrimitive;
import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Displays attachments from an attachment database
 */
public class MISAAttachmentViewerUI extends JPanel {

    private MISAAttachment attachment;

    private static final int COLUMN_LABEL = 0;
    private static final int COLUMN_CONTENT = 1;

    public MISAAttachmentViewerUI(MISAAttachment attachment) {
        this.attachment = attachment;

        initialize();
        refreshContents();
    }

    private void initialize() {
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setLayout(new GridBagLayout());
    }

    private void insertLabelFor(String propertyName, Icon icon, int row) {
        JLabel label = new JLabel(propertyName.substring(1), icon, JLabel.LEFT);
        add(label, new GridBagConstraints() {
            {
                gridx = COLUMN_LABEL;
                gridy = row;
                anchor = GridBagConstraints.NORTHWEST;
                fill = GridBagConstraints.HORIZONTAL;
                insets = UIUtils.UI_PADDING;
                weightx = 0.4;
            }
        });
    }

    private void insertSubUI(MISAAttachmentViewerUI ui, int row) {
        add(ui, new GridBagConstraints() {
            {
                gridx = COLUMN_CONTENT;
                gridy = row;
                anchor = GridBagConstraints.EAST;
                insets = UIUtils.UI_PADDING;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 0.6;
            }
        });
    }

    private void insertDisplayFor(MISAAttachment.Property property, int row) {

        if(property.hasValue()) {
            if(property instanceof MISAAttachment.MemoryProperty) {
                JsonPrimitive primitive = ((MISAAttachment.MemoryProperty) property).getValue();

                if (primitive.isString()) {
                    insertLabelFor(property.getPath(), UIUtils.getIconFromResources("text.png"), row);
                } else if (primitive.isBoolean()) {

                } else if (primitive.isNumber()) {
                    insertLabelFor(property.getPath(), UIUtils.getIconFromResources("number.png"), row);
                }
            }
        }
    }

    public void refreshContents() {
        removeAll();

        if(!attachment.hasData())
            attachment.load();

        List<MISAAttachment.Property> properties = new ArrayList<>(attachment.getProperties());
        properties.sort(Comparator.comparing(MISAAttachment.Property::getPath));

        for (int i = 0; i < properties.size(); ++i) {
            insertDisplayFor(properties.get(i), i);
        }

        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }
}
