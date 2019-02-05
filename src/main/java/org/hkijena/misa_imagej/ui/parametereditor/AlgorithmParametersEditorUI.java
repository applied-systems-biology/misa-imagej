package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAParameterSchema;
import org.hkijena.misa_imagej.ui.parametereditor.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class AlgorithmParametersEditorUI extends JPanel {

    private MISAParameterSchema parameterSchema;

    public AlgorithmParametersEditorUI(MISAModuleParameterEditorUI app) {
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
