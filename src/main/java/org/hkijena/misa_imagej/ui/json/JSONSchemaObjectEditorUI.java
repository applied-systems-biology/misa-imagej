package org.hkijena.misa_imagej.ui.json;

import org.hkijena.misa_imagej.api.json.JSONSchemaObject;

import javax.swing.*;

/**
 * Base class of all JSON schema object editors
 */
public abstract class JSONSchemaObjectEditorUI extends JPanel {

    private JSONSchemaObject jsonSchemaObject;

    public JSONSchemaObjectEditorUI(JSONSchemaObject jsonSchemaObject) {
        this.jsonSchemaObject = jsonSchemaObject;
    }

    public JSONSchemaObject getJsonSchemaObject() {
        return jsonSchemaObject;
    }

    public abstract void populate(JSONSchemaEditorUI schemaEditorUI);
}
