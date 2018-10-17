package org.hkijena.misa_imagej.data;


import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MISAData {
    private JSONSchemaObject schemaObject;
    private DataIOType ioType;
    private DataType type;
    private Path relativePath;

    public MISAData(JSONSchemaObject schema, DataIOType ioType) {
        this.schemaObject = schema;
        this.type = Enum.valueOf(DataType.class, schema.getPropertyFromPath("data-type").getValue().toString());
        this.ioType = ioType;
        this.relativePath = getFilesystemValuePath(schema);
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

    public DataType getType() {
        return type;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public DataIOType getIoType() {
        return ioType;
    }

    public JSONSchemaObject getSchemaObject() {
        return schemaObject;
    }
}
