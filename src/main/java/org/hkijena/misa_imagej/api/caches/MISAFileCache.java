package org.hkijena.misa_imagej.api.caches;

import org.hkijena.misa_imagej.api.MISAFilesystemEntry;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;

import java.util.ArrayList;
import java.util.List;

public class MISAFileCache extends MISACache {

    private List<String> extensions = new ArrayList<>();

    public MISAFileCache(MISASample sample, MISAFilesystemEntry filesystemEntry) {
        super(sample, filesystemEntry);

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
        if(getExtensions().isEmpty())
            return "File";
        else
            return String.join(", ", getExtensions()) + " File";
    }

    /**
     * Allowed extensions according to the pattern
     */
    public List<String> getExtensions() {
        return extensions;
    }
}
