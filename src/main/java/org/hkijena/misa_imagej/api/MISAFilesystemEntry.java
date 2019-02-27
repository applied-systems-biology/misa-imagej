package org.hkijena.misa_imagej.api;

import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A MISA++ filesystem entry
 */
public class MISAFilesystemEntry implements Cloneable {
    public String name;
    public Map<String, MISAFilesystemEntry> children = new HashMap<>();
    public MISAFilesystemEntry parent;
    public String externalPath = "";
    public JSONSchemaObject metadata;
    public MISACacheIOType ioType;

    public MISAFilesystemEntry() {

    }

    public MISAFilesystemEntry(MISAFilesystemEntry parent, JSONSchemaObject sourceObject, String name , MISACacheIOType ioType) {
        this.parent = parent;
        this.name = name;
        this.ioType = ioType;
        if(sourceObject.hasPropertyFromPath("external-path")) {
            externalPath = sourceObject.getPropertyFromPath("external-path").getDefaultValue().toString();
        }
        if(sourceObject.hasPropertyFromPath("metadata")) {
            metadata = (JSONSchemaObject) sourceObject.getPropertyFromPath("metadata").clone();
        }
        if(sourceObject.hasPropertyFromPath("children")) {
            for(Map.Entry<String, JSONSchemaObject> entry : sourceObject.getPropertyFromPath("children").getProperties().entrySet()) {
                children.put(entry.getKey(), new MISAFilesystemEntry(this, entry.getValue(), entry.getKey(), ioType));
            }
        }
    }

    /**
     * Clones this filesystem entry and its children.
     * Please note that the parent will be set to null. Children will be cloned as well and have their parent field set to the
     * result.
     * @return
     */
    @Override
    public Object clone() {
        MISAFilesystemEntry entry = new MISAFilesystemEntry();
        entry.name = name;
        entry.externalPath = externalPath;
        entry.ioType = ioType;
        entry.parent = null;
        if(metadata != null)
            entry.metadata = (JSONSchemaObject)metadata.clone();
        for(Map.Entry<String, MISAFilesystemEntry> child : children.entrySet()) {
            MISAFilesystemEntry copy = (MISAFilesystemEntry)child.getValue().clone();
            copy.parent = entry;
            entry.children.put(child.getKey(), copy);
        }
        return entry;
    }

    public void findCaches(MISASample sample, List<MISACache> result) {
        MISACache cache = MISACacheRegistry.getCacheFor(sample,this);
        if(cache.isValid())
            result.add(cache);
        for(MISAFilesystemEntry entry : children.values()) {
            entry.findCaches(sample, result);
        }
    }

    public Path getInternalPath() {
        if(parent != null)
            return parent.getInternalPath().resolve(name);
        else
            return Paths.get(name);
    }

    public Path getExternalPath() {
        if(externalPath != null && !externalPath.isEmpty()) {
            return Paths.get(externalPath);
        }
        else if(parent != null) {
            return parent.getExternalPath().resolve(name);
        }
        else {
            return null;
        }
    }

    /**
     * Sets the external path from a parameter JSON object that corresponds to this entry.
     * Will recursively descend
     * @param json
     */
    public void setExternalPathFromJson(JsonObject json) {
        if(json.has("external-path")) {
            externalPath = json.get("external-path").getAsString();
        }
        for(Map.Entry<String, MISAFilesystemEntry> kv : children.entrySet()) {
            if(json.has("children") && json.getAsJsonObject("children").has(kv.getKey())) {
                kv.getValue().setExternalPathFromJson(json.getAsJsonObject("children").getAsJsonObject(kv.getKey()));
            }
        }
    }

    @Override
    public String toString() {
        return getInternalPath().toString();
    }
}
