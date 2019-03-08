package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MISAAttachmentViewerListUI extends JPanel {
    private JScrollPane scrollPane;
    private JPanel listPanel;
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

        toolBar.add(Box.createHorizontalGlue());
        statsLabel = new JLabel();
        toolBar.add(statsLabel);

        add(toolBar, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new GridBagLayout());
//        listPanel.setBorder(BorderFactory.createLineBorder(Color.RED));

        JPanel internalPanel = new JPanel(new BorderLayout());
        internalPanel.add(listPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(internalPanel);
        add(scrollPane, BorderLayout.CENTER);

        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {

//            System.out.println(scrollPane.getVerticalScrollBar().getValue() + " / " + scrollPane.getVerticalScrollBar().getMaximum()
//                    + " <- " + scrollPane.getVerticalScrollBar().getVisibleAmount() );
            addItemIfNeeded();
        });
    }

    private void loadAllMissingData() {
        for(MISAAttachment attachment : attachments) {
            attachment.loadAll();
        }
    }

    public void setDatabaseIds(List<Integer> databaseIds) {
        this.databaseIds = Ints.toArray(databaseIds);
        this.lastDisplayedId = -1;
        this.attachments.clear();
        listPanel.removeAll();
        listPanel.revalidate();
        listPanel.repaint();
        statsLabel.setText(databaseIds.size() + " objects");
        if(!databaseIds.isEmpty()) {
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
        System.out.println(preferredWidth);
        for(int i = 0; i < listPanel.getComponentCount(); ++i) {
            if (listPanel.getComponent(i) instanceof MISAAttachmentViewerUI) {
                for(Component component : ((MISAAttachmentViewerUI) listPanel.getComponent(i)).getPropertyLabels()) {
                    component.setPreferredSize(new Dimension(preferredWidth, component.getPreferredSize().height));
                }
            }
        }
        SwingUtilities.invokeLater(() -> {
            listPanel.revalidate();
            listPanel.repaint();
        });
    }
}
