package org.hkijena.misa_imagej.filesystem;

import org.hkijena.misa_imagej.UIHelper;
import org.hkijena.misa_imagej.data.MISAImportedData;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Editor that allows setting up the imported filesystem
 */
public class ImportedFilesystemEditor extends JPanel {
    private JSONSchemaObject jsonSchemaObject;

    public ImportedFilesystemEditor(JSONSchemaObject importedFilesystem, String name) {
        jsonSchemaObject = importedFilesystem.properties.get("children").properties.get(name);
        initialize();
    }

    private void addEditor(MISAImportedData object, int row) {
        UIHelper.backgroundColorJLabel(
                UIHelper.borderedJLabel(UIHelper.createDescriptionLabelUI(this,
                        object.getType().toString(), row, 0)),
                object.getType().toColor());
        UIHelper.borderedJLabel(UIHelper.createDescriptionLabelUI(this, "/" + object.getRelativePath().toString(), row, 1));
        add(new ImporterSourceEditor(jsonSchemaObject), new GridBagConstraints() {
            {
                gridx = 2;
                gridy = row;
                gridwidth = 2;
                anchor = GridBagConstraints.PAGE_START;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
            }
        });
    }

    private void initialize() {
        setBorder(BorderFactory.createTitledBorder("Input"));
        setLayout(new GridBagLayout());

        int row = 0;

        List<JSONSchemaObject> objects = jsonSchemaObject.flatten();
        objects.sort(Comparator.comparing(JSONSchemaObject::getFilesystemDataType));

        for (JSONSchemaObject object : objects) {
           if(object.filesystemData != null) {
               addEditor((MISAImportedData)object.filesystemData, row++);
           }
        }
    }
}
