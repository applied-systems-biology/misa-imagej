package org.hkijena.misa_imagej.extension;

import org.hkijena.misa_imagej.MISAImageJExtensionService;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.extension.attachmentfilters.*;
import org.hkijena.misa_imagej.extension.caches.MISAExportedAttachmentsCache;
import org.hkijena.misa_imagej.extension.caches.MISAFileCache;
import org.hkijena.misa_imagej.extension.caches.MISAOMETiffCache;
import org.hkijena.misa_imagej.extension.datasources.*;
import org.hkijena.misa_imagej.extension.outputcaches.OMETiffOutputCacheUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

@Plugin(type = MISAImageJExtensionService.class)
public class StandardMISAImageJExtensionService extends AbstractService implements MISAImageJExtensionService {

    @Override
    public void register(MISAImageJRegistryService registryService) {
        registryService.getCacheRegistry().register("misa_ome:descriptions/ome-tiff", MISAOMETiffCache.class);
        registryService.getCacheRegistry().register("misa:descriptions/file", MISAFileCache.class);
        registryService.getCacheRegistry().register("misa:descriptions/exported-attachments", MISAExportedAttachmentsCache.class);

//        registryService.getSerializableRegistry().register("misa:attachments/location", MISALocation.class);
//        registryService.getSerializableRegistry().register("misa:attachments/locatable", MISALocatable.class);
//        registryService.getSerializableRegistry().register("misa_ome:attachments/planes-location", MISAOMEPlanesLocation.class);

        registryService.getDataSourceUIRegistry().register(MISAOMETiffDataSource.class, MISAOMETiffDataSourceUI.class);
        registryService.getDataSourceUIRegistry().register(MISAFolderLinkDataSource.class, MISAFolderLinkDataSourceUI.class);
        registryService.getDataSourceUIRegistry().register(MISAPipelineNodeDataSource.class, MISAPipelineNodeDataSourceUI.class);

        registryService.getOutputCacheUIRegistry().register(MISAOMETiffCache.class, OMETiffOutputCacheUI.class);

        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentSampleFilter.class, MISAAttachmentSampleFilterUI.class,
                "Filter by sample", UIUtils.getIconFromResources("sample.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentCacheFilter.class, MISAAttachmentCacheFilterUI.class,
                "Filter by data", UIUtils.getIconFromResources("database.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentTypeFilter.class, MISAAttachmentTypeFilterUI.class,
                "Filter by object type", UIUtils.getIconFromResources("object.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentRootTypeFilter.class, MISAAttachmentRootTypeFilterUI.class,
                "Filter only direct attachments", UIUtils.getIconFromResources("object.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentSQLFilter.class, MISAAttachmentSQLFilterUI.class,
                "Filter by SQL", UIUtils.getIconFromResources("cog.png"));

    }
}
