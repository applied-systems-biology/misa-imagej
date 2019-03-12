package org.hkijena.misa_imagej.ui;

import org.hkijena.misa_imagej.api.MISAImageJAPIService;
import org.hkijena.misa_imagej.ui.registries.MISAAttachmentFilterUIRegistry;
import org.hkijena.misa_imagej.ui.registries.MISADataSourceUIRegistry;
import org.hkijena.misa_imagej.ui.registries.MISAOutputCacheUIRegistry;
import org.hkijena.misa_imagej.ui.registries.MISATableAnalyzerUIOperationRegistry;

public interface MISAImageJUIService extends MISAImageJAPIService {
    MISADataSourceUIRegistry getDataSourceUIRegistry();
    MISAOutputCacheUIRegistry getOutputCacheUIRegistry();
    MISAAttachmentFilterUIRegistry getAttachmentFilterUIRegistry();
    MISATableAnalyzerUIOperationRegistry getTableAnalyzerUIOperationRegistry();
}
