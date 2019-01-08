package org.hkijena.misa_imagej.json_schema.editors;

import org.hkijena.misa_imagej.json_schema.JSONSchemaEditorRegistry;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObjectEditor;
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
public class GenericJSONSchemaObjectEditor extends JSONSchemaObjectEditor {

    public GenericJSONSchemaObjectEditor(JSONSchemaObject object) {
        super(object);
        initialize();
    }

    private void createUI(JSONSchemaObject obj, int row) {
        if(obj.enum_values != null) {
            UIUtils.createDescriptionLabelUI(this, obj, row, 0);
            JComboBox<Object> comboBox = new JComboBox<>();
            comboBox.setToolTipText(obj.description);
            DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();

            for(Object d : obj.enum_values) {
                model.addElement(d);
                if(d.equals(obj.default_value)) {
                    model.setSelectedItem(d);
                }
            }
            comboBox.setModel(model);
            add(comboBox, new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 1;
                    gridy = row;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                    insets = UIUtils.UI_PADDING;
                }
            });
            comboBox.addActionListener(actionEvent -> {
                obj.setValue(model.getSelectedItem());
            });
        }
        else if(obj.type.equals("string")) {
            UIUtils.createDescriptionLabelUI(this, obj, row, 0);
            JTextField edit = new JTextField(obj.default_value != null ? (String)obj.default_value : "");
            edit.setToolTipText(obj.description);
            add(edit, new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 1;
                    gridy = row;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                    insets = UIUtils.UI_PADDING;
                }
            });
            if(obj.default_value != null) {
                edit.setText((String)obj.default_value);
            }
            edit.addActionListener(actionEvent -> {
                obj.setValue(edit.getText());
            });
        }
        else if(obj.type.equals("number")) {
            UIUtils.createDescriptionLabelUI(this, obj, row, 0);
            JSpinner edit = new JSpinner();
            Dimension dim = edit.getPreferredSize();
            edit.setModel(new SpinnerNumberModel(0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 1));
            edit.setPreferredSize(dim);
            edit.setToolTipText(obj.description);
            add(edit, new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 1;
                    gridy = row;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                    insets = UIUtils.UI_PADDING;
                }
            });
            if(obj.default_value != null) {
                edit.setValue(obj.default_value);
            }
            edit.addChangeListener(changeEvent -> {
                obj.setValue(edit.getValue());
            });
        }
        else if(obj.type.equals("boolean")) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setToolTipText(obj.description);
            checkBox.setText(obj.getName());
            add(checkBox, new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 1;
                    gridy = row;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                    gridwidth = 2;
                    insets = UIUtils.UI_PADDING;
                }
            });
            if(obj.default_value != null) {
                checkBox.setSelected((boolean)obj.default_value);
            }
            checkBox.addActionListener(actionEvent -> {
                obj.setValue(checkBox.isSelected());
            });
        }
        else if(obj.type.equals("object")) {
            if(obj.getMaxDepth() <= 1) {
                JSONSchemaObjectEditor editor = JSONSchemaEditorRegistry.getEditorFor(obj);
                add(editor, new GridBagConstraints() {
                    {
                        anchor = GridBagConstraints.PAGE_START;
                        gridx = 0;
                        gridy = row;
                        fill = GridBagConstraints.HORIZONTAL;
                        weightx = 1;
                        gridwidth = 3;
                        insets = UIUtils.UI_PADDING;
                    }
                });
            }
            else {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createTitledBorder(obj.getName()));
                panel.add(new JLabel("Select " + obj.getName() + " in the tree on the left side to show its parameters"), BorderLayout.CENTER);
                add(panel, new GridBagConstraints() {
                    {
                        anchor = GridBagConstraints.PAGE_START;
                        gridx = 0;
                        gridy = row;
                        fill = GridBagConstraints.HORIZONTAL;
                        weightx = 1;
                        gridwidth = 3;
                        insets = UIUtils.UI_PADDING;
                    }
                });
            }
        }

        // Add indicator for null values (that we don't want)
        if(!obj.type.equals("object")) {

            JLabel missingValue = new JLabel();
            add(missingValue, new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 2;
                    gridy = row;
                    insets = UIUtils.UI_PADDING;
                }
            });
            missingValue.setForeground(Color.RED);
            if(!obj.hasValue()) {
                missingValue.setText("Please provide value");
            }
            obj.addPropertyChangeListener(propertyChangeEvent -> {
                if(!obj.hasValue()) {
                    missingValue.setText("Please provide value");
                }
                else {
                    missingValue.setText("");
                }
            });
        }
    }

    private void initialize() {

        setLayout(new GridBagLayout());
        int row = 0;

        // Optional description of the current object
        if(getJsonSchemaObject().description != null && !getJsonSchemaObject().description.isEmpty()) {
            add(new JLabel(getJsonSchemaObject().description), new GridBagConstraints() {
                {
                    gridx = 0;
                    gridy = 0;
                    gridwidth = 2;
                    anchor = GridBagConstraints.PAGE_START;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                }
            });
            ++row;
        }

        if(getJsonSchemaObject().type.equals("object")) {
            setBorder(BorderFactory.createTitledBorder(getJsonSchemaObject().getName()));

            ArrayList<JSONSchemaObject> objects = new ArrayList<>(getJsonSchemaObject().properties.values());
            Collections.sort(objects, Comparator.comparingInt(JSONSchemaObject::getMaxDepth));

            for(JSONSchemaObject obj : objects) {
                createUI(obj, row++);
            }
        }
        else {
            createUI(getJsonSchemaObject(), row++);
        }


        UIUtils.addFillerGridBagComponent(this, row);
    }

}
