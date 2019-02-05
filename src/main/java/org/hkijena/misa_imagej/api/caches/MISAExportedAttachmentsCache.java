package org.hkijena.misa_imagej.api.caches;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISAFilesystemEntry;

public class MISAExportedAttachmentsCache extends MISACache {
    public MISAExportedAttachmentsCache(MISAFilesystemEntry filesystemEntry) {
        super(filesystemEntry);
    }

    @Override
    public String getCacheTypeName() {
        return "Exported attachments";
    }
}
