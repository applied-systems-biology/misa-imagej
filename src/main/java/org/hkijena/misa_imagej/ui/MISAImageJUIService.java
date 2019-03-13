package org.hkijena.misa_imagej.ui;

import org.hkijena.misa_imagej.api.MISAImageJAPIService;
import org.hkijena.misa_imagej.ui.registries.*;

public interface MISAImageJUIService extends MISAImageJAPIService {
    MISADataSourceUIRegistry getDataSourceUIRegistry();
    MISAOutputCacheUIRegistry getOutputCacheUIRegistry();
    MISAAttachmentFilterUIRegistry getAttachmentFilterUIRegistry();
    MISATableAnalyzerUIOperationRegistry getTableAnalyzerUIOperationRegistry();
    MISAPlotBuilderRegistry getPlotBuilderRegistry();
}
