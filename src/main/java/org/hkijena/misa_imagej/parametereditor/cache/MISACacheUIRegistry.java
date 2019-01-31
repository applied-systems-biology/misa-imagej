package org.hkijena.misa_imagej.parametereditor.cache;

import org.hkijena.misa_imagej.api.cache.MISACache;
import org.hkijena.misa_imagej.api.cache.caches.MISAFileCache;
import org.hkijena.misa_imagej.api.cache.caches.MISAOMETiffCache;
import org.hkijena.misa_imagej.parametereditor.cache.editors.GenericMISACacheEditorUI;
import org.hkijena.misa_imagej.parametereditor.cache.editors.MISAOMETiffCacheEditorUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Organizes available cache types
 */
public class MISACacheUIRegistry {


    private static Map<Class<? extends MISACache>, Class<? extends MISACacheEditorUI>> registeredCacheEditors = new HashMap<>();
    private static boolean isInitialized = false;

    private MISACacheUIRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param cacheClass
     */
    public static void register(Class<? extends MISACache> cacheClass, Class<? extends MISACacheEditorUI> editorClass) {
        registeredCacheEditors.put(cacheClass, editorClass);
    }

    /**
     * Creates an UI editor for the cache
     * @param cache
     * @return
     */
    public static MISACacheEditorUI getEditorFor(MISACache cache) {
        if(!isInitialized)
            initialize();
        Class<? extends MISACacheEditorUI> result = registeredCacheEditors.getOrDefault(cache.getClass(), null);
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

        register(MISAOMETiffCache.class, MISAOMETiffCacheEditorUI.class);
        register(MISAFileCache.class, GenericMISACacheEditorUI.class);

        isInitialized = true;
    }

}
