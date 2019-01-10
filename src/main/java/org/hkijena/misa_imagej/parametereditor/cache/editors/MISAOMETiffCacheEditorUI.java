package org.hkijena.misa_imagej.parametereditor.cache.editors;

import org.hkijena.misa_imagej.parametereditor.cache.MISACache;
import org.hkijena.misa_imagej.parametereditor.cache.MISACacheEditorUI;
import org.hkijena.misa_imagej.parametereditor.cache.caches.MISAOMETiffCache;

/**
 * Editor for OME Tiff caches
 */
public class MISAOMETiffCacheEditorUI extends MISACacheEditorUI {

    private MISAOMETiffCache cache;

    public MISAOMETiffCacheEditorUI(MISACache cache) {
        super(cache);
        this.cache = (MISAOMETiffCache)cache;
    }

    @Override
    protected void initializeImporterUI() {

    }

}
