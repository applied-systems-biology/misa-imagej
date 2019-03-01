package org.hkijena.misa_imagej.ui.outputcaches;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.caches.MISAOMETiffCache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.ui.outputcaches.editors.GenericMISAOutputCacheUI;
import org.hkijena.misa_imagej.ui.outputcaches.editors.OMETiffOutputCacheUI;

import java.util.HashMap;
import java.util.Map;

public class MISAOutputCacheUIRegistry {

    private static Map<Class<? extends MISACache>, Class<? extends MISAOutputCacheUI>> registeredCacheEditors = new HashMap<>();
    private static boolean isInitialized = false;

    private MISAOutputCacheUIRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param cacheClass
     */
    public static void register(Class<? extends MISACache> cacheClass, Class<? extends MISAOutputCacheUI> editorClass) {
        registeredCacheEditors.put(cacheClass, editorClass);
    }

    /**
     * Creates an UI editor for the cache
     * @return
     */
    public static MISAOutputCacheUI getEditorFor(MISAOutput misaOutput, MISACache cache) {
        if(!isInitialized)
            initialize();
        Class<? extends MISAOutputCacheUI> result = registeredCacheEditors.getOrDefault(cache.getClass(), null);
        if(result == null)
            return new GenericMISAOutputCacheUI(misaOutput, cache);
        else {
            try {
                return result.getConstructor(MISAOutput.class, MISACache.class).newInstance(misaOutput, cache);
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

        register(MISAOMETiffCache.class, OMETiffOutputCacheUI.class);

        isInitialized = true;
    }

}
