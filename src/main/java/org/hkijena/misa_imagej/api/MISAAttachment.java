package org.hkijena.misa_imagej.api;

/**
 * Wrapper around attached data that allows backtracking to its cache
 */
public class MISAAttachment {
    private MISAAttachmentLocation location;
    private MISASerializable value;
    private MISACache cache;

    public MISAAttachment(MISAAttachmentLocation location, MISASerializable value, MISACache cache) {
        this.location = location;
        this.value = value;
        this.cache = cache;
    }

    public MISAAttachmentLocation getLocation() {
        return location;
    }

    public MISASerializable getValue() {
        return value;
    }

    public MISACache getCache() {
        return cache;
    }
}
