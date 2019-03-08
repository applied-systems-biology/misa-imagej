package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonPrimitive;
import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Displays attachments from an attachment database
 */
public class MISAAttachmentViewerUI extends JPanel {

    private MISAAttachmentViewerListUI listUI;
    private MISAAttachment attachment;
    private JPanel headerPanel;
    private JPanel contentPanel;
    private Set<JLabel> propertyLabels = new HashSet<>();

    private static final int COLUMN_LABEL = 0;
    private static final int COLUMN_CONTENT = 1;

    public MISAAttachmentViewerUI(MISAAttachmentViewerListUI listUI, MISAAttachment attachment) {
        this.listUI = listUI;
        this.attachment = attachment;

        if (!attachment.hasData())
            attachment.load();

        initialize();
        refreshContents();

        attachment.getEventBus().register(this);
    }

    @Subscribe
    public void handleAttachmentDataLoadedEvent(MISAAttachment.DataLoadedEvent event) {
        refreshContents();
    }

    private void initialize() {
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setLayout(new BorderLayout());
        headerPanel = new JPanel();
        headerPanel.setBackground(Color.LIGHT_GRAY);
        headerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 4));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));

        JLabel titleLabel = new JLabel(attachment.getDocumentationTitle(),
                new MonochromeColorIcon(UIUtils.getIconFromResources("object-template.png"), attachment.toColor()),
                JLabel.LEFT);
        headerPanel.add(titleLabel);

        JTextField pathLabel = new JTextField();
        pathLabel.setEditable(false);
        pathLabel.setBorder(null);
        pathLabel.setOpaque(false);
        pathLabel.setText(attachment.getAttachmentFullPath());
        headerPanel.add(Box.createHorizontalStrut(8));
        headerPanel.add(pathLabel);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(Box.createHorizontalStrut(8));

        JButton loadAllLazy = new JButton(UIUtils.getIconFromResources("quickload.png"));
        UIUtils.makeFlatWithoutMargin(loadAllLazy);
        loadAllLazy.setToolTipText("Load all missing data");
        loadAllLazy.addActionListener(e -> attachment.loadAll());
        headerPanel.add(loadAllLazy);

        add(headerPanel, BorderLayout.NORTH);
        contentPanel = new JPanel(new GridBagLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    private void insertLabelFor(String propertyName, Icon icon, int row) {
        JLabel label = new JLabel(propertyName.substring(1), icon, JLabel.LEFT);
        contentPanel.add(label, new GridBagConstraints() {
            {
                gridx = COLUMN_LABEL;
                gridy = row;
                anchor = GridBagConstraints.NORTHWEST;
                fill = GridBagConstraints.NONE;
                insets = UIUtils.UI_PADDING;
//                weightx = 0.4;
            }
        });
        propertyLabels.add(label);
    }

    private void insertComponent(Component ui, int row) {
        contentPanel.add(ui, new GridBagConstraints() {
            {
                gridx = COLUMN_CONTENT;
                gridy = row;
                anchor = GridBagConstraints.EAST;
                insets = UIUtils.UI_PADDING;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
            }
        });
    }

    private void insertDisplayFor(MISAAttachment.Property property, int row) {

        if (property.hasValue()) {
            if (property instanceof MISAAttachment.MemoryProperty) {
                JsonPrimitive primitive = ((MISAAttachment.MemoryProperty) property).getValue();

                if (primitive.isString()) {
                    insertLabelFor(property.getPath(), UIUtils.getIconFromResources("text.png"), row);
                } else if (primitive.isBoolean()) {

                } else if (primitive.isNumber()) {
                    insertLabelFor(property.getPath(), UIUtils.getIconFromResources("number.png"), row);
                }
            }
        } else {
            insertLabelFor(property.getPath(), UIUtils.getIconFromResources("object.png"), row);
            JButton loadButton = new JButton("Load missing data", UIUtils.getIconFromResources("database.png"));
            loadButton.addActionListener(e -> property.loadValue());
            insertComponent(loadButton, row);
        }
    }

    public void refreshContents() {
        contentPanel.removeAll();
        propertyLabels.clear();

        List<MISAAttachment.Property> properties = new ArrayList<>(attachment.getProperties());
        properties.sort(Comparator.comparing(MISAAttachment.Property::getPath));

        for (int i = 0; i < properties.size(); ++i) {
            insertDisplayFor(properties.get(i), i);
        }

        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
            listUI.synchronizeViewerLabelProperties();
        });
    }

    public Set<JLabel> getPropertyLabels() {
        return Collections.unmodifiableSet(propertyLabels);
    }
}
