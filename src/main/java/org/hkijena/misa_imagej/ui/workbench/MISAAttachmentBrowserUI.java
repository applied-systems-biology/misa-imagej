package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MISAAttachmentBrowserUI extends JPanel {

    public static final String OBJECT_VIEW_CACHE = "CACHE";
    public static final String OBJECT_VIEW_TYPES = "TYPES";

    private MISAOutput misaOutput;
    private MISAAttachmentDatabase attachmentDatabase;
    private JPanel filterList;
    private JTree objectViewTree;

    private JToggleButton toggleAutosyncFilters;
    private ButtonGroup viewToggle;

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
            StringSelection stringSelection = new StringSelection(attachmentDatabase.getQuerySQL("*", ""));
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
//        JButton test = new JButton("Test");
//        panel.add(test, BorderLayout.CENTER);
//        test.addActionListener(e -> test.setText("" + attachmentDatabase.getDatasetCount()));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initializeBrowserPanel(), initializeViewerPanel());
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel initializeBrowserPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        toggleAutosyncFilters = new JToggleButton(UIUtils.getIconFromResources("cog.png"));
        toggleAutosyncFilters.setSelected(true);
        toggleAutosyncFilters.setToolTipText("Automatically update object browser");
        toolBar.add(toggleAutosyncFilters);

        JButton syncFilters = new JButton("Update", UIUtils.getIconFromResources("refresh.png"));
        syncFilters.addActionListener(e -> updateObjectBrowser());
        toolBar.add(syncFilters);

        toolBar.add(Box.createHorizontalGlue());


        viewToggle = new ButtonGroup();

        JToggleButton viewAsCachesToggle = new JToggleButton(UIUtils.getIconFromResources("database.png"));
        viewAsCachesToggle.setToolTipText("Display attachments by their data");
        viewAsCachesToggle.setActionCommand(OBJECT_VIEW_CACHE);
        viewAsCachesToggle.addActionListener(e -> updateObjectBrowser());
        viewToggle.add(viewAsCachesToggle);
        toolBar.add(viewAsCachesToggle);

        JToggleButton viewAsObjectsToggle = new JToggleButton(UIUtils.getIconFromResources("object.png"));
        viewAsObjectsToggle.setToolTipText("Display attachments by their object type");
        viewAsObjectsToggle.setActionCommand(OBJECT_VIEW_TYPES);
        viewAsObjectsToggle.addActionListener(e -> updateObjectBrowser());
        viewToggle.add(viewAsObjectsToggle);
        toolBar.add(viewAsObjectsToggle);
        viewAsObjectsToggle.setSelected(true);

        panel.add(toolBar, BorderLayout.NORTH);

        objectViewTree = new JTree();
        panel.add(objectViewTree, BorderLayout.CENTER);

        return panel;
    }

    private void updateObjectBrowser() {
        if(viewToggle.getSelection().getActionCommand().equals(OBJECT_VIEW_CACHE))
            createObjectBrowserModelByCache();
        else
            createObjectBrowserModelByType();
    }

    private void createObjectBrowserModelByCache() {
    }

    private void createObjectBrowserModelByType() {
        ResultSet resultSet = attachmentDatabase.query("id, \"serialization-id\", \"property\", cache, sample",
                "order by \"serialization-id\"");
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");

        String lastSerializationNamespace = null;
        String lastSerializationId = null;

        DefaultMutableTreeNode currentSerializationNamespaceNode = null;
        DefaultMutableTreeNode currentSerializationIdNode = null;

        try {
            while(resultSet.next()) {
                int id = resultSet.getInt(1);
                String serializationId = resultSet.getString(2);
                String property = resultSet.getString(3);
                String cache = resultSet.getString(4);
                String sample = resultSet.getString(5);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        objectViewTree.setModel(new DefaultTreeModel(rootNode));
    }

    private JPanel initializeViewerPanel() {
        JPanel panel = new JPanel();
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

    @Subscribe
    public void handleFilterChangedEvent(MISAAttachmentDatabase.UpdatedFiltersEvent event) {
        if(toggleAutosyncFilters.isSelected()) {
            updateObjectBrowser();
        }
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
