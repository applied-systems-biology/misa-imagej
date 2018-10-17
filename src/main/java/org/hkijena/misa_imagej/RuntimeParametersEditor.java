package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.json_schema.JSONSchemaEditor;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import javax.swing.*;
import java.awt.*;

public class RuntimeParametersEditor extends JPanel {

    private JSONSchemaEditor jsonSchemaEditor = null;

    public RuntimeParametersEditor() {
        initialize();
    }

    private void initialize() {
        jsonSchemaEditor = new JSONSchemaEditor();

        setLayout(new BorderLayout());
        add(jsonSchemaEditor, BorderLayout.CENTER);
    }

    public void setSchema(MISAParameterSchema jsonSchema) {
        jsonSchemaEditor.setSchema(jsonSchema.getRuntimeParameters());
    }

}
