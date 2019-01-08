package org.hkijena.misa_imagej.json_schema;

import javax.swing.*;

/**
 * Base class of all JSON schema object editors
 */
public class JSONSchemaObjectEditorUI extends JPanel {

    private JSONSchemaObject jsonSchemaObject;

    public JSONSchemaObjectEditorUI(JSONSchemaObject jsonSchemaObject) {
        this.jsonSchemaObject = jsonSchemaObject;
    }

    public JSONSchemaObject getJsonSchemaObject() {
        return jsonSchemaObject;
    }
}
