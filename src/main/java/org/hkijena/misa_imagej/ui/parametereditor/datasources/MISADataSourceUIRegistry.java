package org.hkijena.misa_imagej.ui.parametereditor.datasources;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.datasources.MISAFolderLinkDataSource;
import org.hkijena.misa_imagej.api.datasources.MISAOMETiffDataSource;
import org.hkijena.misa_imagej.api.datasources.MISAPipelineNodeDataSource;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.editors.GenericMISADataSourceUI;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.editors.MISAFolderLinkDataSourceUI;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.editors.MISAOMETiffDataSourceUI;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.editors.MISAPipelineNodeDataSourceUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Organizes available cache types
 */
public class MISADataSourceUIRegistry {


    private static Map<Class<? extends MISADataSource>, Class<? extends MISADataSourceUI>> registeredCacheEditors = new HashMap<>();
    private static boolean isInitialized = false;

    private MISADataSourceUIRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param cacheClass
     */
    public static void register(Class<? extends MISADataSource> cacheClass, Class<? extends MISADataSourceUI> editorClass) {
        registeredCacheEditors.put(cacheClass, editorClass);
    }

    /**
     * Creates an UI editor for the cache
     * @param source
     * @return
     */
    public static MISADataSourceUI getEditorFor(MISADataSource source) {
        if(!isInitialized)
            initialize();
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

    /**
     * Initializes default editors
     */
    public static void initialize() {

        register(MISAOMETiffDataSource.class, MISAOMETiffDataSourceUI.class);
        register(MISAFolderLinkDataSource.class, MISAFolderLinkDataSourceUI.class);
        register(MISAPipelineNodeDataSource.class, MISAPipelineNodeDataSourceUI.class);

        isInitialized = true;
    }

}
