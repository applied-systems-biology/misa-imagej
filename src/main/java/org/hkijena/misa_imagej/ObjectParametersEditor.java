package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.json_schema.JSONSchemaEditor;

import javax.swing.*;
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
