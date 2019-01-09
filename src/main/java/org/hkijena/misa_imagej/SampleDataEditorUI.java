package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class SampleDataEditorUI extends JPanel {

    private MISAModuleUI app;
    private JList<String> sampleList;
    private JPanel sampleEditor;
    private MISAParameterSchema parameterSchema;

    public SampleDataEditorUI(MISAModuleUI app) {
        this.app = app;
        this.parameterSchema = app.getParameterSchema();
        initialize();
        updateObjectList();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        sampleEditor = new JPanel(new BorderLayout());

        JPanel sampleListPanel = new JPanel(new BorderLayout(8, 8));
        {
            // Management button on top
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

            // Add sample button
            JButton addSampleButton = new JButton(UIUtils.getIconFromResources("add.png"));
            addSampleButton.setToolTipText("Add new sample");
            addSampleButton.addActionListener(actionEvent -> addSample());
            buttonPanel.add(addSampleButton);

            // Batch-add button
            JButton batchAddSampleButton = new JButton(UIUtils.getIconFromResources("batch-add.png"));
            batchAddSampleButton.setToolTipText("Batch-add samples");
            buttonPanel.add(batchAddSampleButton);

            // Remove sample button
            JButton removeSampleButton = new JButton(UIUtils.getIconFromResources("delete.png"));
            removeSampleButton.setToolTipText("Remove current sample");
            removeSampleButton.addActionListener(actionEvent -> removeSample());
            buttonPanel.add(removeSampleButton);

            sampleListPanel.add(buttonPanel, BorderLayout.NORTH);

            // Add the sample list to the panel
            sampleList = new JList<>(new DefaultListModel<>());
            sampleListPanel.add(sampleList, BorderLayout.CENTER);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sampleListPanel, new JScrollPane(sampleEditor));
        add(splitPane, BorderLayout.CENTER);
        sampleEditor.setLayout(new GridBagLayout());

        // Add events
        sampleList.addListSelectionListener(listSelectionEvent -> {
            // Set current sample globally
            parameterSchema.setCurrentSample(sampleList.getSelectedValue());
        });
        parameterSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("samples")) {
                updateObjectList();
            }
            else if(propertyChangeEvent.getPropertyName().equals("currentSample")) {
                if(parameterSchema.getCurrentSample() != null)
                    sampleList.setSelectedValue(parameterSchema.getCurrentSample().name, true);
                setCurrentSample(app.getParameterSchema().getCurrentSample());
            }
        });
    }

    private void setCurrentSample(MISASample sample) {

        if(sample == null)
            return;

        sampleEditor.removeAll();
        sampleEditor.setLayout(new GridBagLayout());

        sampleEditor.add(new CacheEditorUI("Input data", sample.getImportedCaches()), new GridBagConstraints() {
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

        sampleEditor.add(new CacheEditorUI("Output data", sample.getExportedCaches()), new GridBagConstraints() {
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
    }

    private void addSample() {
        String result = JOptionPane.showInputDialog(this, "Sample name", "Add sample", JOptionPane.PLAIN_MESSAGE);
        if(result != null && !result.isEmpty()) {
            parameterSchema.addSample(result);
        }
    }

    private void removeSample() {
        if(JOptionPane.showConfirmDialog(this,
                "Do you really want to remove this sample?",
                "Remove sample", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            parameterSchema.removeSample(parameterSchema.getCurrentSample().name);
        }
    }

    private void updateObjectList() {
        DefaultListModel<String> model = (DefaultListModel<String>) sampleList.getModel();
        model.clear();
        for(String name : parameterSchema.getSampleNames()) {
            model.addElement(name);
        }
        if(model.size() > 0) {
            sampleList.setSelectedIndex(0);
        }
        else {
            sampleEditor.removeAll();
            sampleEditor.revalidate();
        }
    }
}
