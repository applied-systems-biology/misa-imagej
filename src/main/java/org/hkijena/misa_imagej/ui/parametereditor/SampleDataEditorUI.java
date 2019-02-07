package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.MISACacheIOType;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static org.hkijena.misa_imagej.utils.UIUtils.UI_PADDING;

public class SampleDataEditorUI extends JPanel {

    private JPanel sampleEditor;
    private CacheListTree cacheList;
    private MISAModuleInstance parameterSchema;
    private JXTextField objectFilter;

    private int cacheEditorRows = 0;
    private MISACacheIOType editorLastIOType;

    public SampleDataEditorUI(MISAModuleInstanceUI app) {
        this.parameterSchema = app.getModuleInstance();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // List of caches
        JPanel cacheListPanel = new JPanel(new BorderLayout(8, 8));
        cacheList = new CacheListTree();
        cacheListPanel.add(cacheList, BorderLayout.CENTER);

        cacheList.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("currentCacheList")) {
                refreshEditor();
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
       cacheList.setSample(sample);
    }

    public void refreshEditor() {
        sampleEditor.removeAll();
        sampleEditor.setLayout(new GridBagLayout());
        editorLastIOType = null;
        cacheEditorRows = 0;

        if(cacheList.getCurrentCacheList() != null) {
            for(MISACache cache : cacheList.getCurrentCacheList().caches) {
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

                insertCacheEditorUI(new MISACacheUI(cache));
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

    private void insertCacheEditorUI(MISACacheUI ui) {
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

}
