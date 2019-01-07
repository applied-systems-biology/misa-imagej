package org.hkijena.misa_imagej.json_schema;

import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class JSONSchemaObjectEditor extends JPanel {

    private JSONSchemaObject jsonSchemaObject;

    public JSONSchemaObjectEditor(JSONSchemaObject object) {
        jsonSchemaObject = object;
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
                JSONSchemaObjectEditor editor = new JSONSchemaObjectEditor(obj);
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
        if(jsonSchemaObject.description != null && !jsonSchemaObject.description.isEmpty()) {
            add(new JLabel(jsonSchemaObject.description), new GridBagConstraints() {
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

        if(jsonSchemaObject.type.equals("object")) {
            setBorder(BorderFactory.createTitledBorder(jsonSchemaObject.getName()));

            ArrayList<JSONSchemaObject> objects = new ArrayList<>(jsonSchemaObject.properties.values());
            Collections.sort(objects, Comparator.comparingInt(JSONSchemaObject::getMaxDepth));

            for(JSONSchemaObject obj : objects) {
                createUI(obj, row++);
            }
        }
        else {
            createUI(jsonSchemaObject, row++);
        }


        UIUtils.addFillerGridBagComponent(this, row);
    }

}
