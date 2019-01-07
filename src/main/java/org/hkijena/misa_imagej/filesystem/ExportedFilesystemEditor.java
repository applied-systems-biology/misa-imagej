package org.hkijena.misa_imagej.filesystem;

import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.data.MISAExportedDataAction;
import org.hkijena.misa_imagej.data.MISAExportedData;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.awt.*;

/**
 * Editor that allows changing the the temporary location used to export the data
 */
public class ExportedFilesystemEditor extends JPanel {
    private JSONSchemaObject jsonSchemaObject;

    public ExportedFilesystemEditor(JSONSchemaObject exportedFilesystem, String name) {
        jsonSchemaObject = exportedFilesystem.getPropertyFromPath("children", name);
        initialize();
    }

    private void addEditor(MISAExportedData object, int row) {
        UIUtils.backgroundColorJLabel(
                UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this,
                        object.getType().toString(), row, 0)),
                object.getType().toColor());
        UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this,
                "/" + object.getRelativePath().toString(), row, 1));

        JComboBox<MISAExportedDataAction> comboBox = new JComboBox<>(MISAExportedDataAction.values());
        add(comboBox, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 2;
                gridy = row;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                insets = UIUtils.UI_PADDING;
            }
        });
        comboBox.setSelectedItem(object.getExportedDataAction());

        comboBox.addActionListener(actionEvent -> {
            object.setExportedDataAction((MISAExportedDataAction)comboBox.getSelectedItem());
        });
    }

    private void initialize() {
        setBorder(BorderFactory.createTitledBorder("Output"));
        setLayout(new GridBagLayout());

        int row = 0;
        List<JSONSchemaObject> objects = jsonSchemaObject.flatten();
        objects.sort(Comparator.comparing(JSONSchemaObject::getFilesystemDataType));

        for (JSONSchemaObject object : objects) {
            if(object.filesystemData != null)
                addEditor((MISAExportedData)object.filesystemData, row++);
        }
    }

}
