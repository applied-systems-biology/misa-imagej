package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.api.registries.MISACacheRegistry;
import org.hkijena.misa_imagej.api.registries.MISASerializableRegistry;
import org.hkijena.misa_imagej.ui.registries.MISADataSourceUIRegistry;
import org.hkijena.misa_imagej.ui.registries.MISAOutputCacheUIRegistry;
import org.scijava.InstantiableException;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

@Plugin(type = MISAImageJService.class)
public class MISAImageJRegistryService extends AbstractService implements MISAImageJService {

    private static MISAImageJRegistryService instance;

    public static MISAImageJRegistryService getInstance() {
        return instance;
    }

    public static void instantiate(PluginService pluginService) {
        try {
            instance = (MISAImageJRegistryService) pluginService.getPlugin(MISAImageJRegistryService.class).createInstance();
            instance.discover(pluginService);
        } catch (InstantiableException e) {
            throw new RuntimeException(e);
        }
    }

    private MISADataSourceUIRegistry dataSourceUIRegistry;
    private MISAOutputCacheUIRegistry outputCacheUIRegistry;
    private MISACacheRegistry cacheRegistry;
    private MISASerializableRegistry serializableRegistry;

    public MISAImageJRegistryService() {
        dataSourceUIRegistry = new MISADataSourceUIRegistry();
        outputCacheUIRegistry = new MISAOutputCacheUIRegistry();
        cacheRegistry = new MISACacheRegistry();
        serializableRegistry = new MISASerializableRegistry();
    }

    @Override
    public MISADataSourceUIRegistry getDataSourceUIRegistry() {
        return dataSourceUIRegistry;
    }

    @Override
    public MISAOutputCacheUIRegistry getOutputCacheUIRegistry() {
        return outputCacheUIRegistry;
    }

    @Override
    public MISACacheRegistry getCacheRegistry() {
        return cacheRegistry;
    }

    @Override
    public MISASerializableRegistry getSerializableRegistry() {
        return serializableRegistry;
    }

    private void discover(PluginService pluginService) {
        for(PluginInfo<MISAImageJExtensionService> info : pluginService.getPluginsOfType(MISAImageJExtensionService.class)) {
            try {
                MISAImageJExtensionService service = (MISAImageJExtensionService)info.createInstance();
                service.register(this);
            } catch (InstantiableException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
