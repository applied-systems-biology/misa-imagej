package org.hkijena.misa_imagej.ui.workbench.objectbrowser;

import com.google.common.primitives.Ints;
import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MISAAttachmentViewerListUI extends JPanel {
    private JScrollPane scrollPane;
    private JXPanel listPanel;
    private JLabel statsLabel;

    private MISAAttachmentDatabase database;
    private int[] databaseIds;
    private int lastDisplayedId;
    private List<MISAAttachment> attachments = new ArrayList<>();

    public MISAAttachmentViewerListUI(MISAAttachmentDatabase database) {
        this.database = database;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        JButton loadAllMissingDataButton = new JButton("Load all missing data", UIUtils.getIconFromResources("quickload.png"));
        loadAllMissingDataButton.addActionListener(e -> loadAllMissingData());
        toolBar.add(loadAllMissingDataButton);

        JButton unloadButton = new JButton("Unload all data", UIUtils.getIconFromResources("eye-slash.png"));
        unloadButton.addActionListener(e -> reloadData());
        toolBar.add(unloadButton);

        JButton exportButton = new JButton("Export", UIUtils.getIconFromResources("save.png"));
        exportButton.addActionListener(e -> exportData());
        toolBar.add(exportButton);

        toolBar.add(Box.createHorizontalGlue());
        statsLabel = new JLabel();
        toolBar.add(statsLabel);

        add(toolBar, BorderLayout.NORTH);

        listPanel = new JXPanel();
        listPanel.setScrollableWidthHint(ScrollableSizeHint.FIT);
        listPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);
        listPanel.setLayout(new GridBagLayout());
//        listPanel.setBorder(BorderFactory.createLineBorder(Color.RED));

        JPanel internalPanel = new JPanel(new BorderLayout());
        internalPanel.add(listPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(internalPanel);
        scrollPane.setViewportView(listPanel);
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            addItemIfNeeded();
        });
    }

    private void exportData() {
        if(databaseIds != null && databaseIds.length > 0) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export as *.json");
            if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                MISAAttachmentSaverDialogUI dialog = new MISAAttachmentSaverDialogUI(fileChooser.getSelectedFile().toPath(), database, databaseIds);
                dialog.setModal(true);
                dialog.pack();
                dialog.setSize(400,300);
                dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
                dialog.startOperation();
                dialog.setVisible(true);
            }
        }
    }

    private void loadAllMissingData() {
        MISAAttachmentExpanderDialogUI dialog = new MISAAttachmentExpanderDialogUI(attachments);
        dialog.setModal(true);
        dialog.pack();
        dialog.setSize(400,300);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.startOperation();
        dialog.setVisible(true);
    }

    public void setDatabaseIds(List<Integer> databaseIds) {
        this.databaseIds = Ints.toArray(databaseIds);
        reloadData();
    }

    private void reloadData() {
        this.lastDisplayedId = -1;
        this.attachments.clear();
        listPanel.removeAll();
        listPanel.revalidate();
        listPanel.repaint();
        statsLabel.setText(databaseIds.length + " objects");
        if(databaseIds.length > 0) {
            addItem(0);
        }
    }

    private void addItemIfNeeded() {
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        if(scrollBar.getMaximum() <= scrollBar.getVisibleAmount() || (scrollBar.getValue() + scrollBar.getVisibleAmount()) > (scrollBar.getMaximum() * 0.9)) {
            addItem();
        }
    }

    private void addItem() {
        if(databaseIds != null) {
            if(lastDisplayedId < databaseIds.length - 1) {
                addItem(lastDisplayedId + 1);
            }
        }
    }

    private void addItem(int i) {
        MISAAttachment attachment = database.queryAttachmentAt(databaseIds[i]);
        attachments.add(attachment);
        MISAAttachmentViewerUI viewer = new MISAAttachmentViewerUI(this, attachment);
        lastDisplayedId = i;
        viewer.setAlignmentY(Component.TOP_ALIGNMENT);
        listPanel.add(viewer, new GridBagConstraints() {
            {
                gridx = 0;
                gridy = i;
                fill = GridBagConstraints.HORIZONTAL;
                anchor = GridBagConstraints.NORTHWEST;
                insets = UIUtils.UI_PADDING;
                weightx = 1;
            }
        });
        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> addItemIfNeeded());
    }

    public void synchronizeViewerLabelProperties() {
        int preferredWidth = 0;
        for(int i = 0; i < listPanel.getComponentCount(); ++i) {
            if(listPanel.getComponent(i) instanceof MISAAttachmentViewerUI) {
                for(Component component : ((MISAAttachmentViewerUI) listPanel.getComponent(i)).getPropertyLabels()) {
                    preferredWidth = Math.max(preferredWidth, component.getPreferredSize().width);
                }
            }
        }
        for(int i = 0; i < listPanel.getComponentCount(); ++i) {
            if (listPanel.getComponent(i) instanceof MISAAttachmentViewerUI) {
                for(JLabel component : ((MISAAttachmentViewerUI) listPanel.getComponent(i)).getPropertyLabels()) {
                    component.setPreferredSize(new Dimension(preferredWidth, component.getPreferredSize().height));
                    component.setToolTipText(preferredWidth + "");
                    SwingUtilities.invokeLater(() -> {
                        component.revalidate();
                        component.repaint();
                    });
                }
            }
        }

    }
}
