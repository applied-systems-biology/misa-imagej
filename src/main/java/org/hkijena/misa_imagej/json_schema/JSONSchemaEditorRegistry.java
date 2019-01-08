package org.hkijena.misa_imagej.json_schema;

import org.hkijena.misa_imagej.json_schema.editors.GenericJSONSchemaObjectEditor;

import java.util.Map;

/**
 * Manages available JSON schema object editors
 */
public class JSONSchemaEditorRegistry {

    private static Map<String, Class<JSONSchemaObjectEditor>> registeredEditors;

    private JSONSchemaEditorRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param serializationId
     * @param editorClass
     */
    public static void register(String serializationId, Class<JSONSchemaObjectEditor> editorClass) {
        registeredEditors.put(serializationId, editorClass);
    }

    /**
     * Creates an editor for the provided object
     * @param schemaObject
     * @return
     */
    public static JSONSchemaObjectEditor getEditorFor(JSONSchemaObject schemaObject) {
        if(schemaObject.properties != null && schemaObject.properties.containsKey("misa:serialization-id")) {
            String serializationId = schemaObject.properties.get("misa:serialization-id").default_value.toString();
            Class<JSONSchemaObjectEditor> result = registeredEditors.getOrDefault(serializationId, null);
            if(result != null) {
                try {
                    return result.getConstructor(JSONSchemaObject.class).newInstance(schemaObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        return new GenericJSONSchemaObjectEditor(schemaObject);
    }

    /**
     * Initializes default editors
     */
    public static void initialize() {

    }
}
