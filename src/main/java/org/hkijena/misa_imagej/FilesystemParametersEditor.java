package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.filesystem.ExportedFilesystemEditor;
import org.hkijena.misa_imagej.filesystem.ImportedFilesystemEditor;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import javax.swing.*;
import java.awt.*;

public class FilesystemParametersEditor extends JPanel {

    private JList<String> objectList;
    private JPanel objectEditor;
    private MISAParameterSchema parameterSchema = null;
    private String currentObject = null;

    public FilesystemParametersEditor() {
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

        objectEditor.add(new ImportedFilesystemEditor(parameterSchema.getImportedFilesystemSchema(), name), new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 1;
                gridy = 0;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                gridwidth = 2;
                insets = UIHelper.UI_PADDING;
            }
        });

        objectEditor.add(new ExportedFilesystemEditor(parameterSchema.getExportedFilesystemSchema()), new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 1;
                gridy = 1;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                gridwidth = 2;
                insets = UIHelper.UI_PADDING;
            }
        });

        objectEditor.add(new JPanel(), new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 1;
                gridy = 2;
                fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
                weightx = 1;
                weighty = 1;
            }
        });
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
