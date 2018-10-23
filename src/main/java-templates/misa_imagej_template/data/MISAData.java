package misa_imagej_template.data;


import misa_imagej_template.json_schema.JSONSchemaObject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MISAData {
    private JSONSchemaObject schemaObject;
    private MISADataIOType ioType;
    private MISADataType type;
    private Path relativePath;

    public MISAData(JSONSchemaObject schema, MISADataIOType ioType) {
        this.schemaObject = schema;
        this.type = Enum.valueOf(MISADataType.class, schema.getPropertyFromPath("data-type").getValue().toString());
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

    public MISADataType getType() {
        return type;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public MISADataIOType getIoType() {
        return ioType;
    }

    public JSONSchemaObject getSchemaObject() {
        return schemaObject;
    }
}
