package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAParameterSchema;
import org.hkijena.misa_imagej.ui.parametereditor.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class SampleParametersEditorUI extends JPanel {

    private MISAParameterSchema parameterSchema;
    private JSONSchemaEditorUI jsonSchemaEditorUI = null;

    public SampleParametersEditorUI(MISAModuleParameterEditorUI app) {
        this.parameterSchema = app.getParameterSchema();
        initialize();
    }

    private void initialize() {
        jsonSchemaEditorUI = new JSONSchemaEditorUI();

        setLayout(new BorderLayout());
        add(jsonSchemaEditorUI, BorderLayout.CENTER);

        // Add events
        parameterSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("currentSample")) {
                if(parameterSchema.getCurrentSample() != null) {
                    jsonSchemaEditorUI.setSchema(parameterSchema.getCurrentSample().getParameters());
                }
                else {
                    jsonSchemaEditorUI.setSchema(null);
                }
            }
        });
    }
}
