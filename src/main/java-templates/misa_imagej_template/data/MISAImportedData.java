package misa_imagej_template.data;

import misa_imagej_template.data.importing.MISADataImportSource;
import misa_imagej_template.json_schema.JSONSchemaObject;

public class MISAImportedData extends MISAData {

    private MISADataImportSource importSource = null;

    public MISAImportedData(JSONSchemaObject schema) {
        super(schema, MISADataIOType.Imported);
    }

    public MISADataImportSource getImportSource() {
        return importSource;
    }

    public void setImportSource(MISADataImportSource importSource) {
        this.importSource = importSource;
    }
}
