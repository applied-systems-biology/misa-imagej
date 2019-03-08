package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonPrimitive;
import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;
import org.hkijena.misa_imagej.utils.ui.ReadOnlyToggleButtonModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

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
        loadAllLazy.addActionListener(e -> loadMissingData());
        headerPanel.add(loadAllLazy);

        add(headerPanel, BorderLayout.NORTH);
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        add(contentPanel, BorderLayout.CENTER);
    }

    private void loadMissingData() {
        MISAAttachmentExpanderDialogUI dialog = new MISAAttachmentExpanderDialogUI(attachment);
        dialog.setModal(true);
        dialog.pack();
        dialog.setSize(400,300);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.startOperation();
        dialog.setVisible(true);
    }

    private void insertLabelFor(String propertyName, Icon icon, JPanel row) {
        JLabel label = new JLabel(propertyName != null ? propertyName.substring(1) : null, icon, JLabel.LEFT);
        propertyLabels.add(label);
        row.add(label, BorderLayout.WEST);
    }

    private void insertComponent(Component ui, JPanel row) {
        row.add(ui, BorderLayout.CENTER);
    }

    private void insertDisplayFor(MISAAttachment.Property property) {

        JPanel row = new JPanel(new BorderLayout(4,4));
        row.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

        if (property.hasValue()) {
            if (property instanceof MISAAttachment.MemoryProperty) {
                JsonPrimitive primitive = ((MISAAttachment.MemoryProperty) property).getValue();

                if (primitive.isString()) {
                    insertLabelFor(property.getPath(), UIUtils.getIconFromResources("text.png"), row);
                    if(primitive.getAsString().contains("\n")) {
                        JTextArea textArea = new JTextArea(primitive.getAsString());
                        textArea.setEditable(false);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(100,200));
                        insertComponent(scrollPane, row);
                    }
                    else {
                        JTextField component = new JTextField(primitive.getAsString());
                        component.setEditable(false);
                        insertComponent(component, row);
                    }
                } else if (primitive.isBoolean()) {
                    JCheckBox component = new JCheckBox(property.getPath().substring(1));
                    component.setModel(new ReadOnlyToggleButtonModel(primitive.getAsBoolean()));
                    insertLabelFor(null, null, row);
                    insertComponent(component, row);
                } else if (primitive.isNumber()) {
                    insertLabelFor(property.getPath(), UIUtils.getIconFromResources("number.png"), row);
                    JTextField component = new JTextField(primitive.getAsNumber() + "");
                    component.setEditable(false);
                    insertComponent(component, row);
                }
            }
            else if(property instanceof MISAAttachment.LazyProperty) {
                insertLabelFor(property.getPath(), UIUtils.getIconFromResources("object.png"), row);
                JLabel component = new JLabel(((MISAAttachment.LazyProperty) property).getDocumentationTitle(),
                        new MonochromeColorIcon(UIUtils.getIconFromResources("object-template.png"), ((MISAAttachment.LazyProperty) property).toColor()),
                        JLabel.LEFT);
                insertComponent(component, row);
            }
        } else {
            insertLabelFor(property.getPath(), UIUtils.getIconFromResources("object.png"), row);
            JButton loadButton = new JButton("Load missing data", UIUtils.getIconFromResources("database.png"));
            loadButton.addActionListener(e -> property.loadValue());
            insertComponent(loadButton, row);
        }

        contentPanel.add(row);
    }

    public void refreshContents() {
        contentPanel.removeAll();
        propertyLabels.clear();

        List<MISAAttachment.Property> properties = new ArrayList<>(attachment.getProperties());
        properties.sort(Comparator.comparing(MISAAttachment.Property::getPath));

        for (int i = 0; i < properties.size(); ++i) {
            insertDisplayFor(properties.get(i));
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
