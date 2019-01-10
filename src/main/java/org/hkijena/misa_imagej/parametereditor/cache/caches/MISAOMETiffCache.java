package org.hkijena.misa_imagej.parametereditor.cache.caches;

import org.hkijena.misa_imagej.parametereditor.MISAFilesystemEntry;
import org.hkijena.misa_imagej.parametereditor.cache.MISACache;

public class MISAOMETiffCache extends MISACache {

    public MISAOMETiffCache(MISAFilesystemEntry filesystemEntry) {
        super(filesystemEntry);
    }

    @Override
    public String getCacheTypeName() {
        return "OME TIFF";
    }
}
