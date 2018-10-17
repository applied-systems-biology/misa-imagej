package org.hkijena.misa_imagej.data;

import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

public class MISAImportedData extends MISAData {

    private MISADataImportSource importSource = null;

    public MISAImportedData(JSONSchemaObject schema) {
        super(schema, DataIOType.Imported);
    }

    public MISADataImportSource getImportSource() {
        return importSource;
    }

    public void setImportSource(MISADataImportSource importSource) {
        this.importSource = importSource;
    }
}
