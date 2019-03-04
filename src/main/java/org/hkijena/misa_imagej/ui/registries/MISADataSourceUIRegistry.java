package org.hkijena.misa_imagej.ui.registries;

import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.ui.datasources.GenericMISADataSourceUI;
import org.hkijena.misa_imagej.ui.datasources.MISADataSourceUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Organizes available cache types
 */
public class MISADataSourceUIRegistry {


    private Map<Class<? extends MISADataSource>, Class<? extends MISADataSourceUI>> registeredCacheEditors = new HashMap<>();

    public MISADataSourceUIRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param cacheClass
     */
    public void register(Class<? extends MISADataSource> cacheClass, Class<? extends MISADataSourceUI> editorClass) {
        registeredCacheEditors.put(cacheClass, editorClass);
    }

    /**
     * Creates an UI editor for the cache
     * @param source
     * @return
     */
    public MISADataSourceUI getEditorFor(MISADataSource source) {
        Class<? extends MISADataSourceUI> result = registeredCacheEditors.getOrDefault(source.getClass(), null);
        if(result == null)
            return new GenericMISADataSourceUI(source);
        else {
            try {
                return result.getConstructor(MISADataSource.class).newInstance(source);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

}
