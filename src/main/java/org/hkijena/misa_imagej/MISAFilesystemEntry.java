package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.cache.MISACache;
import org.hkijena.misa_imagej.cache.MISACacheRegistry;
import org.hkijena.misa_imagej.cache.MISADataIOType;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

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
    public MISADataIOType ioType;

    public MISAFilesystemEntry() {

    }

    public MISAFilesystemEntry(MISAFilesystemEntry parent, JSONSchemaObject sourceObject, String name , MISADataIOType ioType) {
        this.parent = parent;
        this.name = name;
        this.ioType = ioType;
        if(sourceObject.hasPropertyFromPath("external-path")) {
            externalPath = sourceObject.getPropertyFromPath("external-path").default_value.toString();
        }
        if(sourceObject.hasPropertyFromPath("metadata")) {
            metadata = (JSONSchemaObject) sourceObject.getPropertyFromPath("metadata").clone();
        }
        if(sourceObject.hasPropertyFromPath("children")) {
            for(Map.Entry<String, JSONSchemaObject> entry : sourceObject.getPropertyFromPath("children").properties.entrySet()) {
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
    protected Object clone() {
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

    public void findCaches(List<MISACache> result) {
        Path relativePath = null;
        switch(ioType) {
            case Imported:
                break;
            case Exported:
                break;
            default:
                throw new UnsupportedOperationException();
        }

        MISACache cache = MISACacheRegistry.getCacheFor(this);
        if(cache.isValid())
            result.add(cache);
        for(MISAFilesystemEntry entry : children.values()) {
            entry.findCaches(result);
        }
    }

    public Path getInternalPath() {
        if(parent != null)
            return Paths.get(parent.getInternalPath().toString(), name);
        else
            return Paths.get(name);
    }

    @Override
    public String toString() {
        return getInternalPath().toString();
    }
}
