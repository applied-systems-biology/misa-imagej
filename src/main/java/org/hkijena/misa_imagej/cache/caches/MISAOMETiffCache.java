package org.hkijena.misa_imagej.cache.caches;

import org.hkijena.misa_imagej.MISAFilesystemEntry;
import org.hkijena.misa_imagej.cache.MISACache;

public class MISAOMETiffCache extends MISACache {

    public MISAOMETiffCache(MISAFilesystemEntry filesystemEntry) {
        super(filesystemEntry);
    }

    @Override
    public String getCacheTypeName() {
        return "OME TIFF";
    }
}
