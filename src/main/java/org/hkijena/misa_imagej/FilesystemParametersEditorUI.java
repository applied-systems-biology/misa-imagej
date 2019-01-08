package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.filesystem.ExportedFilesystemEditorUI;
import org.hkijena.misa_imagej.filesystem.ImportedFilesystemEditorUI;

import javax.swing.*;
import java.awt.*;

public class FilesystemParametersEditorUI extends JPanel {

    private MISAModuleUI app;
    private JList<String> objectList;
    private JPanel objectEditor;
    private MISAParameterSchema parameterSchema = null;
    private String currentObject = null;

    public FilesystemParametersEditorUI(MISAModuleUI app) {
        this.app = app;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        objectEditor = new JPanel(new BorderLayout());
        objectList = new JList<>(new DefaultListModel<>());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objectList, new JScrollPane(objectEditor));
        add(splitPane, BorderLayout.CENTER);

        objectList.addListSelectionListener(listSelectionEvent -> {
            editObject(objectList.getSelectedValue());
        });

        objectEditor.setLayout(new GridBagLayout());
    }

    private void editObject(String name) {
        if(name == null)
            return;
        currentObject = name;

        objectEditor.removeAll();
        objectEditor.setLayout(new GridBagLayout());

        JButton removeObject = new JButton("Remove sample");
        removeObject.addActionListener(actionEvent -> removeSample());
        objectEditor.add(removeObject, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.WEST;
                gridx = 0;
                gridy = 0;
                insets = UIUtils.UI_PADDING;
            }
        });

        objectEditor.add(new JSeparator(), new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 0;
                gridy = 1;
                gridwidth = 2;
                fill = GridBagConstraints.HORIZONTAL;
                insets = UIUtils.UI_PADDING;
            }
        });

        objectEditor.add(new ImportedFilesystemEditorUI(app, parameterSchema.getImportedFilesystemSchema(), name), new GridBagConstraints() {
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

        objectEditor.add(new ExportedFilesystemEditorUI(parameterSchema.getExportedFilesystemSchema(), name), new GridBagConstraints() {
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

        objectEditor.add(new JPanel(), new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 0;
                gridy = 4;
                fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
                weightx = 1;
                weighty = 1;
            }
        });

        objectEditor.revalidate();
    }

    private void removeSample() {
        if(JOptionPane.showConfirmDialog(this, "Do you really want to remove this sample?", "Remove sample", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            parameterSchema.removeObject(currentObject);
        }
    }

    private void updateObjectList() {
        DefaultListModel<String> model = (DefaultListModel<String>)objectList.getModel();
        model.clear();
        for(String name : parameterSchema.getObjectNames()) {
            model.addElement(name);
        }
        if(model.size() > 0) {
            objectList.setSelectedIndex(0);
        }
        else {
            objectEditor.removeAll();
            objectEditor.revalidate();
        }
    }

    public void setSchema(MISAParameterSchema jsonSchema) {
        parameterSchema = jsonSchema;
        updateObjectList();
        jsonSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("objectNames")) {
                updateObjectList();
            }
        });
    }
}
