package org.hkijena.misa_imagej.parametereditor.cache.caches;

import org.hkijena.misa_imagej.parametereditor.MISAFilesystemEntry;
import org.hkijena.misa_imagej.parametereditor.cache.MISACache;
import org.hkijena.misa_imagej.utils.swappers.OMETiffSwapper;

public class MISAOMETiffCache extends MISACache {

    private OMETiffSwapper tiffSwapper;

    public MISAOMETiffCache(MISAFilesystemEntry filesystemEntry) {
        super(filesystemEntry);
    }

    @Override
    public String getCacheTypeName() {
        return "OME TIFF";
    }

    public OMETiffSwapper getTiffSwapper() {
        return tiffSwapper;
    }

    public void setTiffSwapper(OMETiffSwapper tiffSwapper) {
        this.tiffSwapper = tiffSwapper;
    }
}
