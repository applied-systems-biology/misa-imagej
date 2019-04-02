/*
 * Copyright by Ruman Gerst
 * Research Group Applied Systems Biology - Head: Prof. Dr. Marc Thilo Figge
 * https://www.leibniz-hki.de/en/applied-systems-biology.html
 * HKI-Center for Systems Biology of Infection
 * Leibniz Institute for Natural Product Research and Infection Biology - Hans Knöll Insitute (HKI)
 * Adolf-Reichwein-Straße 23, 07745 Jena, Germany
 *
 * This code is licensed under BSD 2-Clause
 * See the LICENSE file provided with this code for the full license.
 */

package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISACacheIOType;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.ui.components.MISACacheTreeUI;
import org.hkijena.misa_imagej.ui.components.MISASampleComboBox;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static org.hkijena.misa_imagej.utils.UIUtils.UI_PADDING;

public class MISACacheBrowserUI extends JPanel {

    private JPanel sampleBrowser;
    private MISACacheTreeUI cacheList;
    private MISAOutput misaOutput;
    private JXTextField objectFilter;
    private MISASampleComboBox sampleComboBox;

    private int cacheEditorRows = 0;

    public MISACacheBrowserUI() {
        initialize();
        refreshEditor();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // List of caches
        JPanel cacheListPanel = new JPanel(new BorderLayout());

        // Add the sample selection
        sampleComboBox = new MISASampleComboBox();
        sampleComboBox.getEventBus().register(this);
        cacheListPanel.add(sampleComboBox, BorderLayout.NORTH);

        // Add the cache list
        cacheList = new MISACacheTreeUI();
        cacheListPanel.add(new JScrollPane(cacheList) {
            {
                setMinimumSize(new Dimension(128, 0));
            }
        }, BorderLayout.CENTER);

        cacheList.getEventBus().register(this);

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
            sampleBrowser = new JPanel(new GridBagLayout());
            editPanel.add(new JScrollPane(sampleBrowser), BorderLayout.CENTER);
        }

        // Editor for the current data
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cacheListPanel, editPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    @Subscribe
    public void handleChangedCurrentCacheListEvent(MISACacheTreeUI.ChangedCurrentCacheListEvent event) {
        refreshEditor();
    }

    @Subscribe
    public void handleCurrentSampleChangedEvent(MISASampleComboBox.SelectionChangedEvent event) {
        cacheList.setSample(event.getSample());
    }

    public void refreshEditor() {
        sampleBrowser.removeAll();
        sampleBrowser.setLayout(new GridBagLayout());
        MISACacheIOType editorLastIOType = null;
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
                    sampleBrowser.add(description, new GridBagConstraints() {
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

                insertCacheBrowserUI(MISAImageJRegistryService.getInstance().getOutputCacheUIRegistry().getEditorFor(misaOutput, cache));
            }

            sampleBrowser.add(new JPanel(), new GridBagConstraints() {
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

        sampleBrowser.revalidate();
        sampleBrowser.repaint();
    }

    private void insertCacheBrowserUI(MISAOutputCacheUI ui) {
        if(objectFilter.getText() != null && !objectFilter.getText().isEmpty()) {
            String searchText = ui.getCache().getCacheTypeName().toLowerCase() + ui.getCache().getRelativePathName().toLowerCase();
            if(!searchText.contains(objectFilter.getText().toLowerCase())) {
                return;
            }
        }

        JLabel description = new JLabel(ui.getCache().getCacheTypeName());
        description.setIcon(UIUtils.getIconFromColor(ui.getCache().toColor()));
        description.setToolTipText(ui.getCache().getCacheTooltip());
        sampleBrowser.add(description, new GridBagConstraints() {
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
        internalPath.setToolTipText(ui.getCache().getTooltip());
        sampleBrowser.add(internalPath, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.WEST;
                gridx = 1;
                gridy = cacheEditorRows;
                weightx = 0;
                insets = UI_PADDING;
                fill = GridBagConstraints.HORIZONTAL;
            }
        });

        sampleBrowser.add(ui, new GridBagConstraints() {
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

    public void setMisaOutput(MISAOutput misaOutput) {
        this.misaOutput = misaOutput;
        this.sampleComboBox.setModuleInstance(misaOutput.getModuleInstance());
        this.cacheList.setSample(this.sampleComboBox.getCurrentSample());

        refreshEditor();
    }
}
