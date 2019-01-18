package org.hkijena.misa_imagej.parametereditor.json_schema.editors;

import org.hkijena.misa_imagej.parametereditor.json_schema.*;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Editor that can edit any JSON schema
 * This is used as fallback if the parameter is not serializeable or has no editor assigned to it
 */
public class GenericJSONSchemaObjectEditorUI extends JSONSchemaObjectEditorUI {

    public GenericJSONSchemaObjectEditorUI(JSONSchemaObject object) {
        super(object);
    }

    @Override
    public void populate(JSONSchemaEditorUI schemaEditorUI) {
        if(getJsonSchemaObject().type == JSONSchemaObjectType.jsonObject) {
            // Do not do anything. Instead create editors for the contained objects
            ArrayList<JSONSchemaObject> objects = new ArrayList<>(getJsonSchemaObject().properties.values());
            objects.sort(Comparator.comparingInt(JSONSchemaObject::getMaxDepth).thenComparing(JSONSchemaObject::getName));

            for(JSONSchemaObject obj : objects) {
                if(!schemaEditorUI.getObjectLimitEnabled() || obj.getMaxDepth() <= 1) {
                    JSONSchemaEditorRegistry.getEditorFor(obj).populate(schemaEditorUI);
                }
            }
        }
        else if(getJsonSchemaObject().enum_values != null) {
            // Create a combo box within this panel
            setLayout(new BorderLayout());
            JComboBox<Object> comboBox = new JComboBox<>();
            comboBox.setToolTipText(getJsonSchemaObject().description);
            DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();

            for(Object d : getJsonSchemaObject().enum_values) {
                model.addElement(d);
                if(d.equals(getJsonSchemaObject().default_value)) {
                    model.setSelectedItem(d);
                }
            }
            comboBox.setModel(model);
            add(comboBox, BorderLayout.CENTER);

            comboBox.addActionListener(actionEvent -> {
                getJsonSchemaObject().setValue(model.getSelectedItem());
            });

            schemaEditorUI.insertObjectEditorUI(this, true);
        }
        else if(getJsonSchemaObject().type == JSONSchemaObjectType.jsonString) {
            // Create a string editor
            setLayout(new BorderLayout());
            JTextField edit = new JTextField(getJsonSchemaObject().default_value != null ? (String)getJsonSchemaObject().default_value : "");
            edit.setToolTipText(getJsonSchemaObject().description);
            if(getJsonSchemaObject().default_value != null) {
                edit.setText((String)getJsonSchemaObject().default_value);
            }
            add(edit, BorderLayout.CENTER);

            edit.addActionListener(actionEvent -> {
                getJsonSchemaObject().setValue(edit.getText());
            });
            schemaEditorUI.insertObjectEditorUI(this, true);
        }
        else if(getJsonSchemaObject().type == JSONSchemaObjectType.jsonNumber) {
            // Create a spinner where the user can edit the value
            setLayout(new BorderLayout());
            JSpinner edit = new JSpinner();
            Dimension dim = edit.getPreferredSize();
            edit.setModel(new SpinnerNumberModel(0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 1));
            edit.setPreferredSize(dim);
            edit.setToolTipText(getJsonSchemaObject().description);

            if(getJsonSchemaObject().default_value != null) {
                edit.setValue(getJsonSchemaObject().default_value);
            }
            add(edit, BorderLayout.CENTER);

            edit.addChangeListener(changeEvent -> {
                getJsonSchemaObject().setValue(edit.getValue());
            });
            schemaEditorUI.insertObjectEditorUI(this, true);
        }
        else if(getJsonSchemaObject().type == JSONSchemaObjectType.jsonBoolean) {
            setLayout(new BorderLayout());
            // Create a checkbox
            JCheckBox checkBox = new JCheckBox();
            checkBox.setToolTipText(getJsonSchemaObject().description);
            checkBox.setText(getJsonSchemaObject().getName());

            if(getJsonSchemaObject().default_value != null) {
                checkBox.setSelected((boolean)getJsonSchemaObject().default_value);
            }
            add(checkBox, BorderLayout.CENTER);
            checkBox.addActionListener(actionEvent -> {
                getJsonSchemaObject().setValue(checkBox.isSelected());
            });

            schemaEditorUI.insertObjectEditorUI(this, false);
        }
        else {
            throw new UnsupportedOperationException("Unknown schema object type " + getJsonSchemaObject().type);
        }

    }

}
