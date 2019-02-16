package org.hkijena.misa_imagej.ui.parametereditor.json_schema;

import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.ui.parametereditor.json_schema.editors.GenericJSONSchemaObjectEditorUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages available JSON schema object editors
 */
public class JSONSchemaEditorRegistry {

    private static Map<String, Class<? extends JSONSchemaObjectEditorUI>> registeredEditors = new HashMap<>();
    private static boolean isInitialized = false;

    private JSONSchemaEditorRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param serializationId
     * @param editorClass
     */
    public static void register(String serializationId, Class<JSONSchemaObjectEditorUI> editorClass) {
        registeredEditors.put(serializationId, editorClass);
    }

    /**
     * Creates an editor for the provided object
     * @param schemaObject
     * @return
     */
    public static JSONSchemaObjectEditorUI getEditorFor(JSONSchemaObject schemaObject) {
        if(isInitialized)
            initialize();
        if(schemaObject.getSerializationId() != null) {
            Class<? extends JSONSchemaObjectEditorUI> result = registeredEditors.getOrDefault(schemaObject.getSerializationId(), null);
            if(result != null) {
                try {
                    return result.getConstructor(JSONSchemaObject.class).newInstance(schemaObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        return new GenericJSONSchemaObjectEditorUI(schemaObject);
    }

    /**
     * Initializes default editors
     */
    public static void initialize() {
        isInitialized = true;
    }
}
