package org.hkijena.misa_imagej.cache;

import org.hkijena.misa_imagej.MISAFilesystemEntry;
import org.hkijena.misa_imagej.cache.editors.GenericMISACacheEditorUI;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Organizes available cache types
 */
public class MISACacheRegistry {

    private static Map<String, Class<MISACache>> registeredCaches = new HashMap<>();
    private static Map<Class<MISACache>, Class<MISACacheEditorUI>> registeredCacheEditors = new HashMap<>();
    private static boolean isInitialized = false;

    private MISACacheRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param serializationId
     * @param cacheClass
     */
    public static void register(String serializationId, Class<MISACache> cacheClass, Class<MISACacheEditorUI> editorClass) {
        registeredCaches.put(serializationId, cacheClass);
        registeredCacheEditors.put(cacheClass, editorClass);
    }


    /**
     * Creates a cache for a filesystem entry
     * @param filesystemEntry
     * @return
     */
    public static MISACache getCacheFor(MISAFilesystemEntry filesystemEntry) {
        if(!isInitialized)
            initialize();

        MISACache tmp = new MISACache(filesystemEntry);
        String patternId = tmp.getPatternSerializationID();
        String descriptionId = tmp.getDescriptionSerializationID();

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
            return result.getConstructor(MISAFilesystemEntry.class).newInstance(filesystemEntry);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an UI editor for the cache
     * @param cache
     * @return
     */
    public static MISACacheEditorUI getEditorFor(MISACache cache) {
        if(!isInitialized)
            initialize();
        Class<MISACacheEditorUI> result = registeredCacheEditors.getOrDefault(cache.getClass(), null);
        if(result == null)
            return new GenericMISACacheEditorUI(cache);
        else {
            try {
                return result.getConstructor(MISACache.class).newInstance(cache);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Initializes default editors
     */
    public static void initialize() {
        isInitialized = true;
    }

}
