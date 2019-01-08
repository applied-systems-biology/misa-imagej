package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class ObjectParametersEditorUI extends JPanel {

    private JSONSchemaEditorUI jsonSchemaEditorUI = null;

    public ObjectParametersEditorUI() {
        initialize();
    }

    private void initialize() {
        jsonSchemaEditorUI = new JSONSchemaEditorUI();

        setLayout(new BorderLayout());
        add(jsonSchemaEditorUI, BorderLayout.CENTER);
    }

    public void setSchema(MISAParameterSchema jsonSchema) {
        jsonSchemaEditorUI.setSchema(jsonSchema.getObjectParameters());
        jsonSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("objectNames")) {
                jsonSchemaEditorUI.refreshEditor();
            }
        });
    }

}
