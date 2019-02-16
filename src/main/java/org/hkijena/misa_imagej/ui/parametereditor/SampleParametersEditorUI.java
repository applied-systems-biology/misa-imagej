package org.hkijena.misa_imagej.ui.parametereditor;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.ui.parametereditor.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class SampleParametersEditorUI extends JPanel {

    private MISAModuleInstance parameterSchema;
    private JSONSchemaEditorUI jsonSchemaEditorUI = null;

    public SampleParametersEditorUI(MISAModuleInstanceUI app) {
        this.parameterSchema = app.getModuleInstance();
        initialize();
    }

    private void initialize() {
        jsonSchemaEditorUI = new JSONSchemaEditorUI();

        setLayout(new BorderLayout());
        add(jsonSchemaEditorUI, BorderLayout.CENTER);

        // Add events
        parameterSchema.getEventBus().register(this);
    }

    @Subscribe
    public void handleCurrentSampleChanged(MISAModuleInstance.ChangedCurrentSampleEvent event) {
        if(parameterSchema.getCurrentSample() != null) {
            jsonSchemaEditorUI.setSchema(parameterSchema.getCurrentSample().getParameters());
        }
        else {
            jsonSchemaEditorUI.setSchema(null);
        }
    }
}
