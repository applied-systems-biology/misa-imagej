package org.hkijena.misa_imagej.parametereditor;

import org.apache.commons.collections.ListUtils;
import org.hkijena.misa_imagej.api.cache.MISACache;
import org.hkijena.misa_imagej.parametereditor.cache.MISACacheEditorUI;
import org.hkijena.misa_imagej.parametereditor.cache.MISACacheUIRegistry;
import org.hkijena.misa_imagej.api.cache.MISACacheIOType;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.ColorIcon;
import org.jdesktop.swingx.JXTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.hkijena.misa_imagej.utils.UIUtils.UI_PADDING;

public class SampleDataEditorUI extends JPanel {

    private JPanel sampleEditor;
    private JTree cacheList;
    private MISAParameterSchema parameterSchema;
    private JXTextField objectFilter;

    private CacheListEntry currentCacheList;

    private int cacheEditorRows = 0;
    private MISACacheIOType editorLastIOType;

    public SampleDataEditorUI(MISAModuleParameterEditorUI app) {
        this.parameterSchema = app.getParameterSchema();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // List of caches
        JPanel cacheListPanel = new JPanel(new BorderLayout(8, 8));
        cacheList = new JTree();
        cacheList.setCellRenderer(new CacheListEntryTreeCellRenderer());
        cacheListPanel.add(cacheList, BorderLayout.CENTER);

        cacheList.addTreeSelectionListener(treeSelectionEvent -> {
            if(cacheList.getLastSelectedPathComponent() != null) {
                DefaultMutableTreeNode nd = (DefaultMutableTreeNode)cacheList.getLastSelectedPathComponent();
                setCurrentCacheList((CacheListEntry)nd.getUserObject());
            }
        });

        // Create editor
        JPanel editPanel = new JPanel(new BorderLayout());
        {
            // Create a toolbar with view options
            JToolBar toolBar = new JToolBar();

            toolBar.add(Box.createHorizontalGlue());

            objectFilter = new JXTextField("Filter ...");
            objectFilter.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    refreshEditor();
                }

                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    refreshEditor();
                }

                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    refreshEditor();
                }
            });
            toolBar.add(objectFilter);

            JButton clearFilterButton = new JButton(UIUtils.getIconFromResources("clear.png"));
            clearFilterButton.addActionListener(actionEvent -> objectFilter.setText(""));
            toolBar.add(clearFilterButton);

            editPanel.add(toolBar, BorderLayout.NORTH);

            // Add the scroll layout here
            sampleEditor = new JPanel(new GridBagLayout());
            editPanel.add(new JScrollPane(sampleEditor), BorderLayout.CENTER);
        }

        // Editor for the current data
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cacheListPanel, editPanel);
        add(splitPane, BorderLayout.CENTER);

        parameterSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("currentSample")) {
                setCurrentSample(parameterSchema.getCurrentSample());
            }
        });
    }

    private void setCurrentSample(MISASample sample) {
        if(sample != null) {
            // Create tree nodes
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new CacheListEntry("'" + sample.name + "' data",
                    ListUtils.union(sample.getImportedCaches(), sample.getExportedCaches())));

            // Imported data
            DefaultMutableTreeNode importedNode = new DefaultMutableTreeNode(new CacheListEntry("Input", sample.getImportedCaches()));
            for(MISACache cache : sample.getImportedCaches()) {
                importedNode.add(new DefaultMutableTreeNode(new CacheListEntry(cache.getRelativePathName(), Arrays.asList(cache))));
            }
            rootNode.add(importedNode);

            // Exported data
            DefaultMutableTreeNode exportedNode = new DefaultMutableTreeNode(new CacheListEntry("Output", sample.getExportedCaches()));
            for(MISACache cache : sample.getExportedCaches()) {
                exportedNode.add(new DefaultMutableTreeNode(new CacheListEntry(cache.getRelativePathName(), Arrays.asList(cache))));
            }
            rootNode.add(exportedNode);


            cacheList.setModel(new DefaultTreeModel(rootNode));
            setCurrentCacheList((CacheListEntry)rootNode.getUserObject());
        }
        else {
            cacheList.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("No properties to edit")));
            setCurrentCacheList(null);
        }
    }

    private void setCurrentCacheList(CacheListEntry entry) {
        currentCacheList = entry;
        refreshEditor();
    }

    public void refreshEditor() {
        sampleEditor.removeAll();
        sampleEditor.setLayout(new GridBagLayout());
        editorLastIOType = null;
        cacheEditorRows = 0;

        if(currentCacheList != null) {
            for(MISACache cache : currentCacheList.caches) {
                if(cache.getIOType() != editorLastIOType) {
                    final boolean first = editorLastIOType == null;
                    JLabel description;

                    switch(cache.getIOType()) {
                        case Imported:
                            description = new JLabel("Input data");
                            break;
                        case Exported:
                            description = new JLabel("Output data");
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsupported data!");
                    }
                    description.setIcon(UIUtils.getIconFromResources("cache.png"));
                    description.setFont(description.getFont().deriveFont(14.0f));
                    sampleEditor.add(description, new GridBagConstraints() {
                        {
                            anchor = GridBagConstraints.WEST;
                            gridx = 0;
                            gridy = cacheEditorRows++;
                            gridwidth = 2;
                            weightx = 0;
                            insets = new Insets(first ? 8 : 24,4,8,4);
                        }
                    });
                    editorLastIOType = cache.getIOType();
                }
                MISACacheUIRegistry.getEditorFor(cache).populate(this);
            }

            sampleEditor.add(new JPanel(), new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 2;
                    gridy = cacheEditorRows++;
                    fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
                    weightx = 0;
                    weighty = 1;
                }
            });
        }

        sampleEditor.revalidate();
        sampleEditor.repaint();
    }

    public CacheListEntry getCurrentCacheList() {
        return currentCacheList;
    }

    public void insertCacheEditorUI(MISACacheEditorUI ui) {
        if(objectFilter.getText() != null && !objectFilter.getText().isEmpty()) {
            String searchText = ui.getCache().getCacheTypeName().toLowerCase() + ui.getCache().getRelativePathName().toLowerCase();
            if(!searchText.contains(objectFilter.getText().toLowerCase())) {
                return;
            }
        }

        JLabel description = new JLabel(ui.getCache().getCacheTypeName());
        description.setIcon(UIUtils.getIconFromColor(ui.getCache().toColor()));
        sampleEditor.add(description, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.WEST;
                gridx = 0;
                gridy = cacheEditorRows;
                weightx = 0;
                insets = UI_PADDING;
            }
        });

        JTextField internalPath = new JTextField(ui.getCache().getRelativePathName());
        internalPath.setEditable(false);
        internalPath.setBorder(null);
        sampleEditor.add(internalPath, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.WEST;
                gridx = 1;
                gridy = cacheEditorRows;
                weightx = 0;
                insets = UI_PADDING;
                fill = GridBagConstraints.HORIZONTAL;
            }
        });

        sampleEditor.add(ui, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.WEST;
                gridx = 2;
                gridy = cacheEditorRows;
                insets = UI_PADDING;
                weightx = 1;
                fill = GridBagConstraints.HORIZONTAL;
            }
        });

        ++cacheEditorRows;
    }

    private class CacheListEntry {
        public List<MISACache> caches;
        public String name;

        public CacheListEntry() {
        }

        public CacheListEntry(String name, List<MISACache> caches) {
            this.caches = caches;
            this.name = name;

            // Sort, so imported items are on top
            this.caches.sort(Comparator.comparing(MISACache::getIOType).thenComparing(MISACache::getRelativePath).thenComparing(MISACache::getCacheTypeName));
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class CacheListEntryTreeCellRenderer extends JLabel implements TreeCellRenderer {

        private ColorIcon icon;

        public CacheListEntryTreeCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            icon = new ColorIcon(16, 16, Color.BLACK);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree jTree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            if(jTree.getFont() != null) {
                setFont(jTree.getFont());
            }

            Object o = ((DefaultMutableTreeNode)value).getUserObject();
            if(o instanceof CacheListEntry) {
                setText(o.toString());
                CacheListEntry entry = (CacheListEntry)o;
                if(entry.caches.size() == 1) {
                    icon.setColor(entry.caches.get(0).toColor());
                    setIcon(icon);
                }
                else {
                    setIcon(null);
                }
            }
            else {
                setText(o.toString());
                setIcon(null);
            }

            // Update status
            if(selected) {
                setBackground(new Color(184, 207, 229));
            }
            else {
                setBackground(new Color(255,255,255));
            }

            return this;
        }
    }
}
