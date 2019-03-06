package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
        updateObjectBrowser();
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
        for (Class<? extends MISAAttachmentFilter> filterClass : MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry().getFilterTypes()) {
            JMenuItem item = MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry().createMenuItem(filterClass, attachmentDatabase);
            itemList.add(item);
        }
        itemList.sort(Comparator.comparing(JMenuItem::getText));
        for (JMenuItem item : itemList) {
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
        objectViewTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {
                if (treeExpansionEvent.getPath().getLastPathComponent() instanceof ObjectBrowserTreeNode) {
                    ObjectBrowserTreeNode node = (ObjectBrowserTreeNode) treeExpansionEvent.getPath().getLastPathComponent();
                    node.loadDatabaseEntries((DefaultTreeModel)objectViewTree.getModel());
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {

            }
        });
        panel.add(new JScrollPane(objectViewTree), BorderLayout.CENTER);

        return panel;
    }

    private void updateObjectBrowser() {
        if (viewToggle.getSelection().getActionCommand().equals(OBJECT_VIEW_CACHE))
            createObjectBrowserModelByCache();
        else
            createObjectBrowserModelByType();
    }

    private void createObjectBrowserModelByCache() {
    }

    private void createObjectBrowserModelByType() {
//        ResultSet resultSet = attachmentDatabase.query("id, \"serialization-id\", \"property\", cache, sample", Collections.emptyList(),
//                "order by \"serialization-id\" asc, cache asc, sample asc");
//        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
//
//        String[] lastCategories = new String[4];
//        DefaultMutableTreeNode[] lastNodes = new DefaultMutableTreeNode[4];
//
//        try {
//            while(resultSet.next()) {
//                int id = resultSet.getInt(1);
//                String serializationId = resultSet.getString(2);
//                String property = resultSet.getString(3);
//                String cache = resultSet.getString(4);
//                String sample = resultSet.getString(5);
//                String serializationNamespace = serializationId.substring(0, serializationId.indexOf(":"));
//
//                String[] categories = { serializationNamespace, serializationId, cache, sample };
//
//                for(int i = 0; i < 4; ++i) {
//                    if(!categories[i].equals(lastCategories[i])) {
//                        lastNodes[i] = new DefaultMutableTreeNode(categories[i]);
//                        if(i == 0)
//                            rootNode.add(lastNodes[i]);
//                        else
//                            lastNodes[i-1].add(lastNodes[i]);
//                        lastCategories[i] = categories[i];
//
//                        for(int j = i + 1; j < 4; ++j) {
//                            lastCategories[j] = null;
//                        }
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//
//        objectViewTree.setModel(new DefaultTreeModel(rootNode));
        ObjectBrowserTreeNode node = new ObjectBrowserTreeNode(attachmentDatabase, new ObjectBrowserTreeNode.Role[]{
                ObjectBrowserTreeNode.Role.SerializationNamespace,
                ObjectBrowserTreeNode.Role.SerializationId,
                ObjectBrowserTreeNode.Role.Cache,
                ObjectBrowserTreeNode.Role.Sample,
                ObjectBrowserTreeNode.Role.Property
        }, new String[5]);
        DefaultTreeModel model = new DefaultTreeModel(node);
        objectViewTree.setModel(model);
        node.loadDatabaseEntries(model);
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
        if (toggleAutosyncFilters.isSelected()) {
            updateObjectBrowser();
        }
    }

    private void recreateFilterList() {
        filterList.removeAll();
        for (MISAAttachmentFilter filter : attachmentDatabase.getFilters()) {
            MISAAttachmentFilterUI ui = MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry().createUIFor(filter);
            ui.setMaximumSize(new Dimension(ui.getMaximumSize().width, ui.getPreferredSize().height));
            filterList.add(ui);
        }
        filterList.add(Box.createVerticalGlue());
        this.revalidate();
        this.repaint();
    }
}
