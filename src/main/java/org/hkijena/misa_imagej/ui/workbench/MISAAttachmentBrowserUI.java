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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MISAAttachmentBrowserUI extends JPanel {

    public static final String OBJECT_VIEW_CACHE = "CACHE";
    public static final String OBJECT_VIEW_TYPES = "TYPES";

    private MISAOutput misaOutput;
    private MISAAttachmentDatabase attachmentDatabase;
    private JPanel filterList;
    private JTree objectViewTree;
    private ObjectBrowserTreeSnapshot objectBrowserTreeSnapshot;
    private MISAAttachmentViewerListUI objectView;
    private MISAAttachmentTableBuilderUI objectTableBuilder;

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
            StringSelection stringSelection = new StringSelection(attachmentDatabase.getQuerySQL("*", Collections.emptyList(), ""));
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
        objectView = new MISAAttachmentViewerListUI(attachmentDatabase);
        objectTableBuilder = new MISAAttachmentTableBuilderUI(attachmentDatabase);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.addTab("Object list", UIUtils.getIconFromResources("object.png"), objectView);
        tabbedPane.addTab("Table", UIUtils.getIconFromResources("table.png"), objectTableBuilder);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initializeBrowserPanel(), tabbedPane);
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

        JButton copySql = new JButton(UIUtils.getIconFromResources("copy.png"));
        copySql.setToolTipText("Copy as SQL query");
        copySql.addActionListener(e-> {
            if(objectViewTree.getSelectionPath() != null &&
                    objectViewTree.getSelectionPath().getLastPathComponent() instanceof ObjectBrowserTreeNode) {
                ObjectBrowserTreeNode node = (ObjectBrowserTreeNode) objectViewTree.getSelectionPath().getLastPathComponent();
                StringSelection stringSelection = new StringSelection(attachmentDatabase.getQuerySQL("*", node.getFilters(), ""));
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        toolBar.add(copySql);

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
        objectViewTree.setCellRenderer(new ObjectBrowserTreeNodeCellRenderer(attachmentDatabase));
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
        objectViewTree.addTreeSelectionListener(e -> updateObjectView());
        objectBrowserTreeSnapshot = new ObjectBrowserTreeSnapshot(objectViewTree);
        panel.add(new JScrollPane(objectViewTree), BorderLayout.CENTER);

        return panel;
    }

    private void updateObjectBrowser() {
        objectBrowserTreeSnapshot.createSnapshot();
        if (viewToggle.getSelection().getActionCommand().equals(OBJECT_VIEW_CACHE))
            createObjectBrowserModelByCache();
        else
            createObjectBrowserModelByType();
        objectBrowserTreeSnapshot.restoreSnapshot();
    }

    private void createObjectBrowserModelByCache() {
        ObjectBrowserTreeNode node = new ObjectBrowserTreeNode(attachmentDatabase, new ObjectBrowserTreeNode.Role[]{
                ObjectBrowserTreeNode.Role.Sample,
                ObjectBrowserTreeNode.Role.Cache,
                ObjectBrowserTreeNode.Role.SubCache,
                ObjectBrowserTreeNode.Role.SerializationId,
                ObjectBrowserTreeNode.Role.Property
        }, new String[5]);
        DefaultTreeModel model = new DefaultTreeModel(node);
        objectViewTree.setModel(model);
        node.loadDatabaseEntries(model);
    }

    private void createObjectBrowserModelByType() {
        ObjectBrowserTreeNode node = new ObjectBrowserTreeNode(attachmentDatabase, new ObjectBrowserTreeNode.Role[]{
                ObjectBrowserTreeNode.Role.SerializationNamespace,
                ObjectBrowserTreeNode.Role.SerializationId,
                ObjectBrowserTreeNode.Role.CacheAndSubCache,
                ObjectBrowserTreeNode.Role.Sample,
                ObjectBrowserTreeNode.Role.Property
        }, new String[5]);
        DefaultTreeModel model = new DefaultTreeModel(node);
        objectViewTree.setModel(model);
        node.loadDatabaseEntries(model);
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

    private void updateObjectView() {
        if(objectViewTree.getSelectionPath() != null && objectViewTree.getSelectionPath().getLastPathComponent() instanceof ObjectBrowserTreeNode) {
            ObjectBrowserTreeNode node = (ObjectBrowserTreeNode)objectViewTree.getSelectionPath().getLastPathComponent();
            List<Integer> ids = node.getSelectedDatabaseIndices();
            objectView.setDatabaseIds(ids);
            objectTableBuilder.setDatabaseIds(ids);
        }
    }
}
