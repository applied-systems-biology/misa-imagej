package org.hkijena.misa_imagej.filesystem;

import org.hkijena.misa_imagej.MISAModuleUI;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import javax.swing.*;

/**
 * Editor that allows setting up the imported filesystem
 */
public class ImportedFilesystemEditorUI extends JPanel {
    private MISAModuleUI app;
    private JSONSchemaObject jsonSchemaObject;

    public ImportedFilesystemEditorUI(MISAModuleUI app, JSONSchemaObject importedFilesystem, String name) {
        this.app = app;
        jsonSchemaObject = importedFilesystem.properties.get("children").properties.get(name);
        initialize();
    }

//    private void addEditor(MISAImportedData object, int row) {
//        UIUtils.backgroundColorJLabel(
//                UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this,
//                        object.getType().toString(), row, 0)),
//                object.getType().toColor());
//        UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this, "/" + object.getRelativePath().toString(), row, 1));
//        add(new MISAImporterSourceEditor(app, object), new GridBagConstraints() {
//            {
//                gridx = 2;
//                gridy = row;
//                gridwidth = 2;
//                anchor = GridBagConstraints.PAGE_START;
//                fill = GridBagConstraints.HORIZONTAL;
//                weightx = 1;
//            }
//        });
//    }

    private void initialize() {
//        setBorder(BorderFactory.createTitledBorder("Input"));
//        setLayout(new GridBagLayout());
//
//        int row = 0;
//
//        List<JSONSchemaObject> objects = jsonSchemaObject.flatten();
//        objects.sort(Comparator.comparing(JSONSchemaObject::getFilesystemDataType));
//
//        for (JSONSchemaObject object : objects) {
//           if(object.filesystemData != null) {
//               addEditor((MISAImportedData)object.filesystemData, row++);
//           }
//        }
    }
}
