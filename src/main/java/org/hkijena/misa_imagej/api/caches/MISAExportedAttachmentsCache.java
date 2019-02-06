package org.hkijena.misa_imagej.api.caches;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISAFilesystemEntry;
import org.hkijena.misa_imagej.api.MISASample;

public class MISAExportedAttachmentsCache extends MISACache {

    public MISAExportedAttachmentsCache(MISASample sample, MISAFilesystemEntry filesystemEntry) {
        super(sample, filesystemEntry);
    }

    @Override
    public String getCacheTypeName() {
        return "Exported attachments";
    }
}
