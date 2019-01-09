package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.json_schema.JSONSchemaEditorUI;

import javax.swing.*;
import java.awt.*;

public class SampleParametersEditorUI extends JPanel {

    private MISAParameterSchema parameterSchema;
    private JSONSchemaEditorUI jsonSchemaEditorUI = null;

    public SampleParametersEditorUI(MISAModuleUI app) {
        this.parameterSchema = app.getParameterSchema();
        initialize();
    }

    private void initialize() {

        JComboBox<MISASample> sampleList = new JComboBox<>();
        parameterSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("samples")) {
                DefaultComboBoxModel<MISASample> model = new DefaultComboBoxModel<>();
                for(MISASample sample : parameterSchema.getSamples()) {
                    model.addElement(sample);
                }
                sampleList.setModel(model);
            }
            else if(propertyChangeEvent.getPropertyName().equals("currentSample")) {
                if(parameterSchema.getCurrentSample() != null) {
                    sampleList.setSelectedItem(parameterSchema.getCurrentSample());
                    sampleList.setEnabled(true);
                }
                else {
                    sampleList.setEnabled(false);
                }
            }
        });
        sampleList.addItemListener(itemEvent -> {
            if(parameterSchema.getSamples().size() > 0 && sampleList.getSelectedItem() != null) {
                parameterSchema.setCurrentSample(((MISASample)sampleList.getSelectedItem()).name);
            }
        });

        jsonSchemaEditorUI = new JSONSchemaEditorUI(sampleList);

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
