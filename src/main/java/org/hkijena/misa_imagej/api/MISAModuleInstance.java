package org.hkijena.misa_imagej.api;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.json.JSONSchemaObjectType;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.api.repository.MISAModuleInfo;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MISAModuleInstance implements MISAValidatable {

    /**
     * JSON schema object for runtime parameters
     */
    private JSONSchemaObject algorithmParameters;

    /**
     * JSON schema object for runtime parameters
     */
    private JSONSchemaObject runtimeParameters;

    /**
     * Template used for sample parameters
     */
    private JSONSchemaObject sampleParametersTemplate;

    private MISAFilesystemEntry sampleImportedFilesystemTemplate;

    private MISAFilesystemEntry sampleExportedFilesystemTemplate;

    /**
     * The samples
     */
    private Map<String, MISASample> samples = new HashMap<>();

    private MISASample currentSample = null;

    private MISAModuleInfo moduleInfo;

    private MISAModule module;

    private String name = "module";

    private EventBus eventBus = new EventBus();

    public MISAModuleInstance(JSONSchemaObject object) {
        algorithmParameters = object.getProperties().get("algorithm");
        runtimeParameters = object.getProperties().get("runtime");
        sampleParametersTemplate = object.getProperties().get("samples").getAdditionalPropertiesTemplate();
        sampleImportedFilesystemTemplate = new MISAFilesystemEntry(null,
                object.getPropertyFromPath("filesystem", "json-data", "imported", "children").getAdditionalPropertiesTemplate(),
                "",
                MISACacheIOType.Imported);
        sampleExportedFilesystemTemplate = new MISAFilesystemEntry(null,
                object.getPropertyFromPath("filesystem", "json-data", "exported", "children").getAdditionalPropertiesTemplate(),
                "",
                MISACacheIOType.Exported);
    }

    public Collection<String> getSampleNames() {
        return Collections.unmodifiableCollection(samples.keySet());
    }

    public Collection<MISASample> getSamples() {
        return Collections.unmodifiableCollection(samples.values());
    }

    public void addSample(String name) {
        if (samples.containsKey(name)) {
            int counter = 1;
            while (samples.containsKey(name + " (" + counter + ")")) {
                ++counter;
            }
            name = name + " (" + counter + ")";
        }
        MISASample sample = new MISASample(this, name,
                (JSONSchemaObject) sampleParametersTemplate.clone(),
                (MISAFilesystemEntry) sampleImportedFilesystemTemplate.clone(),
                (MISAFilesystemEntry) sampleExportedFilesystemTemplate.clone());
        samples.put(name, sample);
        getEventBus().post(new AddedSampleEvent(sample));

        if (samples.size() == 1) {
            setCurrentSample(name);
        }
    }

    public void removeSample(String name) {
        if (currentSample != null && currentSample.name.equals(name))
            currentSample = null;
        MISASample removed = samples.get(name);
        samples.remove(name);
        getEventBus().post(new RemovedSampleEvent(removed));
        getEventBus().post(new ChangedCurrentSampleEvent(currentSample));
    }

    public MISASample getSample(String name) {
        return samples.get(name);
    }

    public MISASample getCurrentSample() {
        if (currentSample == null && samples.size() > 0)
            return samples.values().stream().findFirst().get();
        return currentSample;
    }

    public void setCurrentSample(String name) {
        MISASample sample = getSample(name);
        if (sample != currentSample) {
            currentSample = sample;
            getEventBus().post(new ChangedCurrentSampleEvent(currentSample));
        }
    }

    public JSONSchemaObject getAlgorithmParameters() {
        return algorithmParameters;
    }

    public JSONSchemaObject getRuntimeParameters() {
        return runtimeParameters;
    }

    /**
     * Returns the parameters as JSON object
     *
     * @return
     */
    public JsonElement getParametersAsJson(Path importedDirectory, Path exportedDirectory) {
        JSONSchemaObject parameters = new JSONSchemaObject(JSONSchemaObjectType.jsonObject);

        // Save properties
        parameters.addProperty("algorithm", algorithmParameters);
        parameters.addProperty("runtime", runtimeParameters);
        parameters.addProperty("samples", new JSONSchemaObject(JSONSchemaObjectType.jsonObject));

        for (MISASample sample : samples.values()) {
            parameters.getPropertyFromPath("samples").addProperty(sample.name, sample.getParameters());
        }

        parameters.ensurePropertyFromPath("filesystem").addProperty("source", JSONSchemaObject.createString("directories"));
        parameters.ensurePropertyFromPath("filesystem").addProperty("input-directory",
                JSONSchemaObject.createString(importedDirectory.toString()));
        parameters.ensurePropertyFromPath("filesystem").addProperty("output-directory",
                JSONSchemaObject.createString(exportedDirectory.toString()));

        return parameters.toJson();
    }

    /**
     * Writes the final JSON parameter
     *
     * @param parameterJsonPath where the parameter will be written
     * @param importedDirectory The physical path of the import directory where ImageJ data is exported if needed.
     * @param exportedDirectory The physical path of the export directory where everything will be cached
     * @param forceCopy         If true, the importer will copy the files into the imported directory even if not necessary
     */
    public void install(Path parameterJsonPath, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {

        JsonElement parameterJson;
        if (relativeDirectories) {
            parameterJson = getParametersAsJson(parameterJsonPath.getParent().relativize(importedDirectory),
                    parameterJsonPath.getParent().relativize(exportedDirectory));
        } else {
            parameterJson = getParametersAsJson(importedDirectory, exportedDirectory);
        }

        // Write the parameter schema
        try {
            GsonUtils.toJsonFile(GsonUtils.getGson(), parameterJson, parameterJsonPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Install imported data into their proper filesystem locations
        for (MISASample sample : samples.values()) {
            for (MISACache cache : sample.getImportedCaches()) {
                Path cachePath = importedDirectory.resolve(sample.name).resolve(cache.getFilesystemEntry().getInternalPath());
                cache.install(cachePath, forceCopy);
            }
        }
    }

    /**
     * Generates the parameter schema report
     *
     * @return
     */
    @Override
    public MISAValidityReport getValidityReport() {
        MISAValidityReport report = new MISAValidityReport();

        report.merge(algorithmParameters.getValidityReport(), "Algorithm parameters");
        report.merge(runtimeParameters.getValidityReport(), "Runtime parameters");

        for (MISASample sample : samples.values()) {
            report.merge(sample.getValidityReport(), "Samples", sample.name);
        }

        return report;
    }

    /**
     * Load parameters from the provided JSON
     *
     * @param root
     */
    public void loadParameters(JsonObject root) {
        // Add missing samples & merge their parameters
        if (root.has("samples")) {
            for (Map.Entry<String, JsonElement> kv : root.getAsJsonObject("samples").entrySet()) {
                if (!samples.containsKey(kv.getKey())) {
                    addSample(kv.getKey());
                }
                samples.get(kv.getKey()).getParameters().setValueFromJson(kv.getValue());
            }
        }

        // Merge parameters
        if (root.has("algorithm")) {
            algorithmParameters.setValueFromJson(root.get("algorithm"));
        }
        if (root.has("runtime")) {
            runtimeParameters.setValueFromJson(root.get("runtime"));
        }

    }

    public MISAModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public void setModuleInfo(MISAModuleInfo moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public MISAModule getModule() {
        return module;
    }

    public void setModule(MISAModule module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static class AddedSampleEvent {
        private MISASample sample;

        public AddedSampleEvent(MISASample sample) {
            this.sample = sample;
        }

        public MISASample getSample() {
            return sample;
        }
    }

    public static class RemovedSampleEvent {
        private MISASample sample;

        public RemovedSampleEvent(MISASample sample) {
            this.sample = sample;
        }

        public MISASample getSample() {
            return sample;
        }
    }

    public static class ChangedCurrentSampleEvent {
        private MISASample sample;

        public ChangedCurrentSampleEvent(MISASample sample) {
            this.sample = sample;
        }

        public MISASample getSample() {
            return sample;
        }
    }
}
