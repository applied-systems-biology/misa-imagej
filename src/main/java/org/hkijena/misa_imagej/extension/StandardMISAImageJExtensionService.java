package org.hkijena.misa_imagej.extension;

import org.hkijena.misa_imagej.MISAImageJExtensionService;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.extension.attachments.MISALocatable;
import org.hkijena.misa_imagej.extension.attachments.MISALocation;
import org.hkijena.misa_imagej.extension.attachments.MISAOMEPlanesLocation;
import org.hkijena.misa_imagej.extension.caches.MISAExportedAttachmentsCache;
import org.hkijena.misa_imagej.extension.caches.MISAFileCache;
import org.hkijena.misa_imagej.extension.caches.MISAOMETiffCache;
import org.hkijena.misa_imagej.extension.datasources.*;
import org.hkijena.misa_imagej.extension.outputcaches.OMETiffOutputCacheUI;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

@Plugin(type = MISAImageJExtensionService.class)
public class StandardMISAImageJExtensionService extends AbstractService implements MISAImageJExtensionService {

    @Override
    public void register(MISAImageJRegistryService registryService) {
        registryService.getCacheRegistry().register("misa_ome:descriptions/ome-tiff", MISAOMETiffCache.class);
        registryService.getCacheRegistry().register("misa:descriptions/file", MISAFileCache.class);
        registryService.getCacheRegistry().register("misa:descriptions/exported-attachments", MISAExportedAttachmentsCache.class);

        registryService.getSerializableRegistry().register("misa:attachments/location", MISALocation.class);
        registryService.getSerializableRegistry().register("misa:attachments/locatable", MISALocatable.class);
        registryService.getSerializableRegistry().register("misa_ome:attachments/planes-location", MISAOMEPlanesLocation.class);

        registryService.getDataSourceUIRegistry().register(MISAOMETiffDataSource.class, MISAOMETiffDataSourceUI.class);
        registryService.getDataSourceUIRegistry().register(MISAFolderLinkDataSource.class, MISAFolderLinkDataSourceUI.class);
        registryService.getDataSourceUIRegistry().register(MISAPipelineNodeDataSource.class, MISAPipelineNodeDataSourceUI.class);

        registryService.getOutputCacheUIRegistry().register(MISAOMETiffCache.class, OMETiffOutputCacheUI.class);
    }
}
