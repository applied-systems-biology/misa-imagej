package org.hkijena.misa_imagej.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Wrapper around attached data that allows backtracking to its cache
 * The attachment is lazily loaded on demand
 */
public class MISAAttachment {
    private MISAAttachmentLocation location;
    private Path path;
    private MISACache cache;

    public MISAAttachment(MISAAttachmentLocation location, Path value, MISACache cache) {
        this.location = location;
        this.path = value;
        this.cache = cache;
    }

    public MISAAttachmentLocation getLocation() {
        return location;
    }

    public MISACache getCache() {
        return cache;
    }

    public MISASerializable getValue() {
        Gson gson = GsonUtils.getGson();
        try {
            JsonElement element = GsonUtils.fromJsonFile(gson, path, JsonElement.class);
            return MISASerializableRegistry.deserialize(element);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
