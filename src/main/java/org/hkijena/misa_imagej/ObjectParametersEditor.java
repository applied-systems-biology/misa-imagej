package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.json_schema.JSONSchemaEditor;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObjectEditor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class ObjectParametersEditor extends JPanel {

    private JSONSchemaEditor jsonSchemaEditor = null;

    public ObjectParametersEditor() {
        initialize();
    }

    private void initialize() {
        jsonSchemaEditor = new JSONSchemaEditor();

        setLayout(new BorderLayout());
        add(jsonSchemaEditor, BorderLayout.CENTER);
    }

    public void setSchema(MISAParameterSchema jsonSchema) {
        jsonSchemaEditor.setSchema(jsonSchema.getObjectParameters());
        jsonSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("objectNames")) {
                jsonSchemaEditor.refreshEditor();
            }
        });
    }

}
