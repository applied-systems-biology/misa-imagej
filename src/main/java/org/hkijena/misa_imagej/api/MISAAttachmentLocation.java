package org.hkijena.misa_imagej.api;

import java.nio.file.Path;

/**
 * Location of a MISACache attachment
 */
public class MISAAttachmentLocation {
    /**
     * Internal path within the cache
     * This is required, as a MISACache can create sub-caches that are not exposed to the parameter schema
     */
    public Path subCachePath;

    /**
     * Index of the attachment
     */
    public String attachmentIndex;

    @Override
    public int hashCode() {
        return subCachePath.hashCode() >> 13 + attachmentIndex.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof MISAAttachmentLocation) {
            return subCachePath.equals(((MISAAttachmentLocation)o).subCachePath) &&
                    attachmentIndex.equals(((MISAAttachmentLocation)o).attachmentIndex);
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return attachmentIndex + " @ " + subCachePath;
    }
}
