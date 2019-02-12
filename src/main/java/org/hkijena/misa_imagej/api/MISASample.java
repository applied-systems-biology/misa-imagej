package org.hkijena.misa_imagej.api;

import org.hkijena.misa_imagej.api.json.JSONSchemaObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a data sample
 */
public class MISASample implements MISAValidatable {

    private MISAModuleInstance moduleInstance;

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

    public MISASample(MISAModuleInstance moduleInstance, String name, JSONSchemaObject parameters, MISAFilesystemEntry importedFilesystem, MISAFilesystemEntry exportedFilesystem) {
        this.moduleInstance = moduleInstance;
        this.name = name;
        this.parameters = parameters;
        this.importedFilesystem = importedFilesystem;
        this.exportedFilesystem = exportedFilesystem;

        // Look for caches
        importedFilesystem.findCaches(this, getImportedCaches());
        exportedFilesystem.findCaches(this, getExportedCaches());
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

    @Override
    public MISAValidityReport getValidityReport() {
        MISAValidityReport report = new MISAValidityReport();

        report.merge(parameters.getValidityReport(), "Parameters");
        for(MISACache cache : importedCaches) {
            report.merge(cache.getValidityReport(), "Data", "Input");
        }
        for(MISACache cache : exportedCaches) {
            report.merge(cache.getValidityReport(), "Data", "Output");
        }

        return report;
    }

    public MISAModuleInstance getModuleInstance() {
        return moduleInstance;
    }

    public MISACache getExportedCacheByRelativePath(String path) {
        return getExportedCaches().stream().filter(misaCache -> misaCache.getRelativePath().equals(path)).findFirst().orElse(null);
    }

    public MISACache getImportedCacheByRelativePath(String path) {
        return getImportedCaches().stream().filter(misaCache -> misaCache.getRelativePath().equals(path)).findFirst().orElse(null);
    }
}
