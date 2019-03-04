package org.hkijena.misa_imagej.ui.registries;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.extension.outputcaches.GenericMISAOutputCacheUI;
import org.hkijena.misa_imagej.ui.workbench.MISAOutputCacheUI;

import java.util.HashMap;
import java.util.Map;

public class MISAOutputCacheUIRegistry {

    private Map<Class<? extends MISACache>, Class<? extends MISAOutputCacheUI>> registeredCacheEditors = new HashMap<>();

    public MISAOutputCacheUIRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param cacheClass
     */
    public void register(Class<? extends MISACache> cacheClass, Class<? extends MISAOutputCacheUI> editorClass) {
        registeredCacheEditors.put(cacheClass, editorClass);
    }

    /**
     * Creates an UI editor for the cache
     * @return
     */
    public MISAOutputCacheUI getEditorFor(MISAOutput misaOutput, MISACache cache) {
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
}
