package org.hkijena.misa_imagej;

import org.apache.commons.collections.ListUtils;
import org.hkijena.misa_imagej.cache.MISACache;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SampleDataEditorUI extends JPanel {

    private JPanel sampleEditor;
    private JTree cacheList;
    private MISAParameterSchema parameterSchema;

    public SampleDataEditorUI(MISAModuleUI app) {
        this.parameterSchema = app.getParameterSchema();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        sampleEditor = new JPanel(new BorderLayout());

        // List of caches
        JPanel cacheListPanel = new JPanel(new BorderLayout(8, 8));
        cacheList = new JTree();
        cacheListPanel.add(cacheList, BorderLayout.CENTER);

        cacheList.addTreeSelectionListener(treeSelectionEvent -> {
            if(cacheList.getLastSelectedPathComponent() != null) {
                DefaultMutableTreeNode nd = (DefaultMutableTreeNode)cacheList.getLastSelectedPathComponent();
                setCurrentCacheList((CacheListEntry)nd.getUserObject());
            }
        });

        // Editor for the current data
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cacheListPanel, new JScrollPane(sampleEditor));
        add(splitPane, BorderLayout.CENTER);
        sampleEditor.setLayout(new GridBagLayout());

        parameterSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("currentSample")) {
                if(parameterSchema.getCurrentSample() != null) {
                    setCurrentSample(parameterSchema.getCurrentSample());
                }
                else {
                    sampleEditor.removeAll();
                    sampleEditor.revalidate();
                }
            }
        });
    }

    private void setCurrentSample(MISASample sample) {
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

    private void setCurrentCacheList(CacheListEntry entry) {
        List<MISACache> imported = new ArrayList<>();
        List<MISACache> exported = new ArrayList<>();

        for(MISACache cache : entry.caches) {
            switch(cache.getIOType()) {
                case Imported:
                    imported.add(cache);
                    break;
                case Exported:
                    exported.add(cache);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        sampleEditor.removeAll();
        sampleEditor.setLayout(new GridBagLayout());

        if(!imported.isEmpty()) {
            sampleEditor.add(new CacheEditorUI("Input data", imported), new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 0;
                    gridy = 2;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                    gridwidth = 2;
                    insets = UIUtils.UI_PADDING;
                }
            });
        }
        if(!exported.isEmpty()) {
            sampleEditor.add(new CacheEditorUI("Output data", exported), new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 0;
                    gridy = 3;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                    gridwidth = 2;
                    insets = UIUtils.UI_PADDING;
                }
            });
        }

        // Vertical fill space
        sampleEditor.add(new JPanel(), new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 0;
                gridy = 4;
                fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
                weightx = 1;
                weighty = 1;
            }
        });

        sampleEditor.revalidate();
        sampleEditor.repaint();
    }

    private class CacheListEntry {
        public List<MISACache> caches;
        public String name;

        public CacheListEntry() {

        }

        public CacheListEntry(String name, List<MISACache> caches) {
            this.caches = caches;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
