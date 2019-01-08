package org.hkijena.misa_imagej.cache;


import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MISACache {

    /**
     * JSON schema that points to the the filesystem entry within the parameter schema
     */
    private JSONSchemaObject schemaObject;

    /**
     * Relative path within the imported or exported filesystem
     * This does not include "imported" or "exported"
     */
    private Path relativePath;

    /**
     * Indicates if this data is imported or exported
     */
    private MISADataIOType ioType;

    public MISACache(JSONSchemaObject schema, MISADataIOType ioType) {
        this.schemaObject = schema;
        this.ioType = ioType;
        this.relativePath = getFilesystemValuePath(schema, ioType);
    }

    /**
     * Relative path within the imported or exported filesystem
     * This does not include "imported" or "exported"
     * @return
     */
    public Path getRelativePath() {
        return relativePath;
    }

    /**
     * Indicates if this data is imported or exported
     * @return
     */
    public MISADataIOType getIoType() {
        return ioType;
    }

    /**
     * JSON schema that points to the the filesystem entry within the parameter schema
     * @return
     */
    public JSONSchemaObject getSchemaObject() {
        return schemaObject;
    }

    /**
     * Returns the serialization ID of the pattern if available
     * Otherwise return null
     * @return
     */
    public String getPatternSerializationID() {
        if(schemaObject.hasPropertyFromPath("metadata", "pattern")) {
            return schemaObject.getPropertyFromPath("metadata", "pattern").serializationId;
        }
        return null;
    }

    /**
     * Returns the serialization ID of the description if available
     * Otherwise return null
     * @return
     */
    public String getDescriptionSerializationID() {
        if(schemaObject.hasPropertyFromPath("metadata", "description")) {
            return schemaObject.getPropertyFromPath("metadata", "description").serializationId;
        }
        return null;
    }

    /**
     * Returns a filesystem-specific value path for a JSON Schema entry
     * @param obj
     * @return
     */
    private static Path getFilesystemValuePath(JSONSchemaObject obj, MISADataIOType ioType) {
        return Paths.get(obj.getValuePath().substring("/filesystem/json-data/".length()));
    }
}
