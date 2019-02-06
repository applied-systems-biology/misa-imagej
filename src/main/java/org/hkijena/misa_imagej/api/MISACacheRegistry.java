package org.hkijena.misa_imagej.api;

import org.hkijena.misa_imagej.api.caches.MISAExportedAttachmentsCache;
import org.hkijena.misa_imagej.api.caches.MISAFileCache;
import org.hkijena.misa_imagej.api.caches.MISAOMETiffCache;

import java.util.HashMap;
import java.util.Map;

public class MISACacheRegistry {
    private static Map<String, Class<? extends MISACache>> registeredCaches = new HashMap<>();
    private static boolean isInitialized = false;

    private MISACacheRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param serializationId
     * @param cacheClass
     */
    public static void register(String serializationId, Class<? extends MISACache> cacheClass) {
        registeredCaches.put(serializationId, cacheClass);
    }

    /**
     * Creates a cache for a filesystem entry
     * @param filesystemEntry
     * @return
     */
    public static MISACache getCacheFor(MISASample sample, MISAFilesystemEntry filesystemEntry) {
        if(!isInitialized)
            initialize();

        MISACache tmp = new MISACache(sample, filesystemEntry);
        String patternId = tmp.getPatternSerializationID();
        String descriptionId = tmp.getDescriptionSerializationID();

        Class<? extends MISACache> result = null;

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
            return result.getConstructor(MISASample.class, MISAFilesystemEntry.class).newInstance(sample, filesystemEntry);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes default editors
     */
    public static void initialize() {

        register("misa_ome:descriptions/ome-tiff", MISAOMETiffCache.class);
        register("misa:descriptions/file", MISAFileCache.class);
        register("misa:descriptions/exported-attachments", MISAExportedAttachmentsCache.class);

        isInitialized = true;
    }
}
