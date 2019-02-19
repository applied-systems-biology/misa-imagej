package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.ui.components.MISASampleComboBox;
import org.hkijena.misa_imagej.ui.json.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class MISASampleParametersUI extends JPanel {

    private org.hkijena.misa_imagej.api.MISAModuleInstance moduleInstance;
    private JSONSchemaEditorUI jsonSchemaEditorUI = null;
    private MISASampleComboBox sampleComboBox;

    public MISASampleParametersUI(MISAModuleInstance app) {
        this.moduleInstance = app;
        initialize();
        refreshEditor();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        sampleComboBox = new MISASampleComboBox(moduleInstance);
        sampleComboBox.addItemListener(e -> refreshEditor());

        jsonSchemaEditorUI = new JSONSchemaEditorUI(sampleComboBox);

        add(jsonSchemaEditorUI, BorderLayout.CENTER);

        // Add events
        moduleInstance.getEventBus().register(this);
    }

    private void refreshEditor() {
        if(sampleComboBox.getSelectedItem() instanceof MISASample) {
            jsonSchemaEditorUI.setSchema(sampleComboBox.getCurrentSample().getParameters());
        }
        else {
            jsonSchemaEditorUI.setSchema(null);
        }
    }
}