package org.hkijena.misa_imagej.cache;

import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Organizes available cache types
 */
public class MISACacheRegistry {

    private static Map<String, Class<MISACache>> registeredCaches = new HashMap<>();
    private static boolean isInitialized = false;

    private MISACacheRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param serializationId
     * @param cacheClass
     */
    public static void register(String serializationId, Class<MISACache> cacheClass) {
        registeredCaches.put(serializationId, cacheClass);
    }

    /**
     * Creates an editor for the provided object
     * @param schemaObject
     * @return
     */
    public static MISACache getCacheFor(JSONSchemaObject schemaObject, MISADataIOType ioType) {
        if(!isInitialized)
            initialize();

        String patternId = null;
        String descriptionId = null;

        if(schemaObject.hasPropertyFromPath("metadata", "pattern")) {
            patternId = schemaObject.getPropertyFromPath("metadata", "pattern").serializationId;
        }
        if(schemaObject.hasPropertyFromPath("metadata", "description")) {
            descriptionId = schemaObject.getPropertyFromPath("metadata", "description").serializationId;
        }

        Class<MISACache> result = null;

        if(descriptionId != null) {
            // The preferred way: Decide via description id
            result = registeredCaches.getOrDefault(descriptionId, null);
        }
        if(result == null && patternId != null) {
            // Also possible: A (possibly) more generic way via pattern
            result = registeredCaches.getOrDefault(patternId, null);
        }
        if(result == null) {
            result = MISACache.class;
        }

        try {
            return result.getConstructor(JSONSchemaObject.class, MISADataIOType.class).newInstance(schemaObject, ioType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes default editors
     */
    public static void initialize() {
        isInitialized = true;
    }

}
