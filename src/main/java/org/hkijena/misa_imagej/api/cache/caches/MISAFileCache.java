package org.hkijena.misa_imagej.api.cache.caches;

import org.hkijena.misa_imagej.parametereditor.MISAFilesystemEntry;
import org.hkijena.misa_imagej.api.cache.MISACache;

import java.util.ArrayList;
import java.util.List;

public class MISAFileCache extends MISACache {

    /**
     * Allowed extensions according to the pattern
     */
    public List<String> extensions = new ArrayList<>();

    public MISAFileCache(MISAFilesystemEntry filesystemEntry) {
        super(filesystemEntry);

        // Try to extract the extensions from the pattern
        if(filesystemEntry.metadata.hasPropertyFromPath("pattern", "extensions")) {
            Object object = filesystemEntry.metadata.getPropertyFromPath("pattern", "extensions").default_value;
            if(object instanceof List) {
                extensions = (List<String>)object;
            }
        }
    }

    @Override
    public String getCacheTypeName() {
        if(extensions.isEmpty())
            return "File";
        else
            return String.join(", ", extensions) + " File";
    }
}
