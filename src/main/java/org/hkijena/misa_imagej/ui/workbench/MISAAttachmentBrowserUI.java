package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MISAAttachmentBrowserUI extends JPanel {

    private MISAOutput misaOutput;
    private MISAAttachmentDatabase attachmentDatabase;
    private JPanel filterList;

    public MISAAttachmentBrowserUI(MISAOutput misaOutput) {
        this.misaOutput = misaOutput;
        this.attachmentDatabase = misaOutput.createAttachmentDatabase();
        initialize();

        attachmentDatabase.getEventBus().register(this);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initializeFilterPanel(), initializeContentPanel());
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel initializeFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        JButton addFilterButton = new JButton("Add filter", UIUtils.getIconFromResources("filter.png"));
        JPopupMenu addFilterMenu = UIUtils.addPopupMenuToComponent(addFilterButton);
        List<JMenuItem> itemList = new ArrayList<>();
        for(Class<? extends MISAAttachmentFilter> filterClass : MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry().getFilterTypes()) {
            JMenuItem item = MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry().createMenuItem(filterClass, attachmentDatabase);
            itemList.add(item);
        }
        itemList.sort(Comparator.comparing(JMenuItem::getText));
        for(JMenuItem item : itemList) {
            addFilterMenu.add(item);
        }
        toolBar.add(addFilterButton);
        toolBar.add(Box.createHorizontalStrut(150));
        toolBar.add(Box.createHorizontalGlue());

        JButton copySQLButton = new JButton(UIUtils.getIconFromResources("copy.png"));
        copySQLButton.setToolTipText("Copy filters as SQL query");
        copySQLButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(attachmentDatabase.getQuerySQL("*"));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        toolBar.add(copySQLButton);

        panel.add(toolBar, BorderLayout.NORTH);
        filterList = new JPanel();
        filterList.setLayout(new BoxLayout(filterList, BoxLayout.PAGE_AXIS));
        panel.add(new JScrollPane(filterList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel initializeContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton test = new JButton("Test");
        panel.add(test, BorderLayout.CENTER);
        test.addActionListener(e -> test.setText("" + attachmentDatabase.getDatasetCount()));
        return panel;
    }

    @Subscribe
    public void handleFilterAddedEvent(MISAAttachmentDatabase.AddedFilterEvent event) {
        recreateFilterList();
    }

    @Subscribe
    public void handleFilterRemovedEvent(MISAAttachmentDatabase.RemovedFilterEvent event) {
        recreateFilterList();
    }

    private void recreateFilterList() {
        filterList.removeAll();
        for(MISAAttachmentFilter filter : attachmentDatabase.getFilters()) {
            MISAAttachmentFilterUI ui = MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry().createUIFor(filter);
            ui.setMaximumSize(new Dimension(ui.getMaximumSize().width, ui.getPreferredSize().height));
            filterList.add(ui);
        }
        filterList.add(Box.createVerticalGlue());
        this.revalidate();
        this.repaint();
    }
}
