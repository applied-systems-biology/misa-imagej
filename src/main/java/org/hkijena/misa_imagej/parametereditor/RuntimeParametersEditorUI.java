package org.hkijena.misa_imagej.parametereditor;

import org.hkijena.misa_imagej.api.parameterschema.MISAParameterSchema;
import org.hkijena.misa_imagej.parametereditor.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class RuntimeParametersEditorUI extends JPanel {

    private MISAParameterSchema parameterSchema;

    public RuntimeParametersEditorUI(MISAModuleParameterEditorUI app) {
        this.parameterSchema = app.getParameterSchema();
        initialize();
    }

    private void initialize() {
        JSONSchemaEditorUI jsonSchemaEditorUI = new JSONSchemaEditorUI();

        setLayout(new BorderLayout());
        add(jsonSchemaEditorUI, BorderLayout.CENTER);

        jsonSchemaEditorUI.setSchema(parameterSchema.getRuntimeParameters());
    }

}
