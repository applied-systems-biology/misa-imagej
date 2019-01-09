package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.MISAModuleUI;
import org.hkijena.misa_imagej.cache.MISACache;
import org.hkijena.misa_imagej.cache.MISACacheRegistry;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Editor that allows setting up the imported filesystem
 */
public class CacheEditorUI extends JPanel {
    private MISAModuleUI app;
    private JSONSchemaObject jsonSchemaObject;

    public CacheEditorUI(MISAModuleUI app, JSONSchemaObject importedFilesystem, String name) {
        this.app = app;
        jsonSchemaObject = importedFilesystem.properties.get("children").properties.get(name);
        initialize();
    }

    private void addEditor(MISACache cache, int row) {
        UIUtils.backgroundColorJLabel(
                UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this,
                        cache.getCacheTypeName(), row, 0)),
                new Color(206, 212, 255));
        UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this, "/" + cache.getFilesystemEntry().getInternalPath().toString(), row, 1));
        add(MISACacheRegistry.getEditorFor(cache), new GridBagConstraints() {
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

//        for(MISACache cache : app.getParameterSchema().getImportedData()) {
//            addEditor(cache, row++);
//        }
//        for(MISACache cache : app.getParameterSchema().getExportedData()) {
//            addEditor(cache, row++);
//        }
    }
}
