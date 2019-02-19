package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.ui.json.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class AlgorithmParametersEditorUI extends JPanel {

    private org.hkijena.misa_imagej.api.MISAModuleInstance parameterSchema;

    public AlgorithmParametersEditorUI(MISAModuleInstance app) {
        this.parameterSchema = app;
        initialize();
    }

    private void initialize() {
        JSONSchemaEditorUI jsonSchemaEditorUI = new JSONSchemaEditorUI();

        setLayout(new BorderLayout());
        add(jsonSchemaEditorUI, BorderLayout.CENTER);
        jsonSchemaEditorUI.setSchema(parameterSchema.getAlgorithmParameters());
    }

}
