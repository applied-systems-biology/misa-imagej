package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class AlgorithmParametersEditorUI extends JPanel {

    private MISAParameterSchema parameterSchema;

    public AlgorithmParametersEditorUI(MISAModuleUI app) {
        this.parameterSchema = app.getParameterSchema();
        initialize();
    }

    private void initialize() {
        JSONSchemaEditorUI jsonSchemaEditorUI = new JSONSchemaEditorUI();

        setLayout(new BorderLayout());
        add(jsonSchemaEditorUI, BorderLayout.CENTER);
        jsonSchemaEditorUI.setSchema(parameterSchema.getAlgorithmParameters());
    }

}
