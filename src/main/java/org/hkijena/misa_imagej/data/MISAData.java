package org.hkijena.misa_imagej.data;


import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;
import org.hkijena.misa_imagej.data.MISADataIOType;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MISAData {

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

    public MISAData(JSONSchemaObject schema, MISADataIOType ioType) {
        this.schemaObject = schema;
        this.ioType = ioType;
        this.relativePath = getFilesystemValuePath(schema);
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
     * Returns a filesystem-specific value path for a JSON Schema entry
     * @param obj
     * @return
     */
    private static Path getFilesystemValuePath(JSONSchemaObject obj) {
        boolean nextIsChildren = true;
        String result = obj.id;
        JSONSchemaObject current = obj.parent;

        while(!(current.getValuePath().equals("/filesystem/json-data/imported") || current.getValuePath().equals("/filesystem/json-data/exported"))) {
            if(!nextIsChildren) {
                result = current.id + "/" + result;
            }
            nextIsChildren = !nextIsChildren;
            current = current.parent;
        }

        return Paths.get(result);
    }
}
