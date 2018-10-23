package misa_imagej_template;

import misa_imagej_template.json_schema.JSONSchemaEditor;

import javax.swing.*;
import java.awt.*;

public class AlgorithmParametersEditor extends JPanel {

    private JSONSchemaEditor jsonSchemaEditor = null;

    public AlgorithmParametersEditor() {
        initialize();
    }

    private void initialize() {
        jsonSchemaEditor = new JSONSchemaEditor();

        setLayout(new BorderLayout());
        add(jsonSchemaEditor, BorderLayout.CENTER);
    }

    public void setSchema(MISAParameterSchema jsonSchema) {
        jsonSchemaEditor.setSchema(jsonSchema.getAlgorithmParameters());
    }

}
