package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.primitives.Ints;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MISAAttachmentViewerListUI extends JPanel {
    private JScrollPane scrollPane;
    private JPanel listPanel;

    private MISAAttachmentDatabase database;
    private int[] databaseIds;
    private int lastDisplayedId;

    public MISAAttachmentViewerListUI(MISAAttachmentDatabase database) {
        this.database = database;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
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

    public void setDatabaseIds(List<Integer> databaseIds) {
        this.databaseIds = Ints.toArray(databaseIds);
        this.lastDisplayedId = -1;
        listPanel.removeAll();
        listPanel.revalidate();
        listPanel.repaint();
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
        MISAAttachmentViewerUI viewer = new MISAAttachmentViewerUI(database.queryAttachmentAt(databaseIds[i]));
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
}
