package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.cache.MISACache;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a data sample
 */
public class MISASample {
    /**
     * Name of the sample
     */
    public String name;

    /**
     * Parametes of this sample
     */
    private JSONSchemaObject parameters;

    private MISAFilesystemEntry importedFilesystem;

    private MISAFilesystemEntry exportedFilesystem;

    private List<MISACache> importedCaches = new ArrayList<>();

    private List<MISACache> exportedCaches = new ArrayList<>();

    public MISASample(String name, JSONSchemaObject parameters, MISAFilesystemEntry importedFilesystem, MISAFilesystemEntry exportedFilesystem) {
        this.name = name;
        this.parameters = parameters;
        this.importedFilesystem = importedFilesystem;
        this.exportedFilesystem = exportedFilesystem;

        // Look for caches
        importedFilesystem.findCaches(getImportedCaches());
        exportedFilesystem.findCaches(getExportedCaches());
    }

    public JSONSchemaObject getParameters() {
        return parameters;
    }

    /**
     * The imported filesystem of this sample
     */
    public MISAFilesystemEntry getImportedFilesystem() {
        return importedFilesystem;
    }

    /**
     * The exported filesystem of this sample
     */
    public MISAFilesystemEntry getExportedFilesystem() {
        return exportedFilesystem;
    }

    public List<MISACache> getImportedCaches() {
        return importedCaches;
    }

    public List<MISACache> getExportedCaches() {
        return exportedCaches;
    }

    @Override
    public String toString() {
        return name;
    }
}
