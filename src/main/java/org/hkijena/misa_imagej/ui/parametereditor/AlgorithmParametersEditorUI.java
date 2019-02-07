package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.ui.parametereditor.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class AlgorithmParametersEditorUI extends JPanel {

    private MISAModuleInstance parameterSchema;

    public AlgorithmParametersEditorUI(MISAModuleInstanceUI app) {
        this.parameterSchema = app.getModuleInstance();
        initialize();
    }

    private void initialize() {
        JSONSchemaEditorUI jsonSchemaEditorUI = new JSONSchemaEditorUI();

        setLayout(new BorderLayout());
        add(jsonSchemaEditorUI, BorderLayout.CENTER);
        jsonSchemaEditorUI.setSchema(parameterSchema.getAlgorithmParameters());
    }

}
