package org.hkijena.misa_imagej.api.parameterschema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.cache.MISACache;
import org.hkijena.misa_imagej.api.cache.MISACacheIOType;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MISAParameterSchema implements ParameterSchemaValue {

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

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private MISASample currentSample = null;

    public MISAParameterSchema(JSONSchemaObject object) {
        algorithmParameters = object.properties.get("algorithm");
        runtimeParameters = object.properties.get("runtime");
        sampleParametersTemplate = object.properties.get("samples").getAdditionalPropertiesTemplate();
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
        MISASample sample = new MISASample(name,
                (JSONSchemaObject) sampleParametersTemplate.clone(),
                (MISAFilesystemEntry) sampleImportedFilesystemTemplate.clone(),
                (MISAFilesystemEntry) sampleExportedFilesystemTemplate.clone());
        samples.put(name, sample);
        propertyChangeSupport.firePropertyChange("samples", null, null);

        if(samples.size() == 1) {
            setCurrentSample(name);
        }
    }

    public void removeSample(String name) {
        if(currentSample != null && currentSample.name.equals(name))
            currentSample = null;
        samples.remove(name);
        propertyChangeSupport.firePropertyChange("samples", null, null);
        propertyChangeSupport.firePropertyChange("currentSample", null, null);
    }

    public MISASample getSample(String name) {
        return samples.get(name);
    }

    public MISASample getCurrentSample() {
        if(currentSample == null && samples.size() > 0)
            return samples.values().stream().findFirst().get();
        return currentSample;
    }

    public void setCurrentSample(String name) {
        MISASample sample = getSample(name);
        if(sample != currentSample) {
            currentSample = sample;
            propertyChangeSupport.firePropertyChange("currentSample", null, null);
        }
    }

    public JSONSchemaObject getAlgorithmParameters() {
        return algorithmParameters;
    }

    public JSONSchemaObject getRuntimeParameters() {
        return runtimeParameters;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Writes the final JSON parameter
     *
     * @param parameterSchema   where the parameter will be written
     * @param importedDirectory The physical path of the import directory where ImageJ data is exported if needed.
     * @param exportedDirectory The physical path of the export directory where everything will be cached
     * @param forceCopy         If true, the importer will copy the files into the imported directory even if not necessary
     */
    public void install(Path parameterSchema, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {

        JSONSchemaObject parameters = new JSONSchemaObject(JSONSchemaObjectType.jsonObject);

        // Save properties
        parameters.addProperty("algorithm", algorithmParameters);
        parameters.addProperty("runtime", runtimeParameters);
        parameters.addProperty("samples", new JSONSchemaObject(JSONSchemaObjectType.jsonObject));

        for(MISASample sample : samples.values()) {
            parameters.getPropertyFromPath("samples").addProperty(sample.name, sample.getParameters());
        }

        parameters.ensurePropertyFromPath("filesystem").addProperty("source", JSONSchemaObject.createString("directories"));
        if(!relativeDirectories) {
            parameters.ensurePropertyFromPath("filesystem").addProperty("input-directory",
                    JSONSchemaObject.createString(importedDirectory.toString()));
            parameters.ensurePropertyFromPath("filesystem").addProperty("output-directory",
                    JSONSchemaObject.createString(exportedDirectory.toString()));
        }
        else {
            parameters.ensurePropertyFromPath("filesystem").addProperty("input-directory",
                    JSONSchemaObject.createString(parameterSchema.getParent().relativize(importedDirectory).toString()));
            parameters.ensurePropertyFromPath("filesystem").addProperty("output-directory",
                    JSONSchemaObject.createString(parameterSchema.getParent().relativize(exportedDirectory).toString()));
        }

        // Write the parameter schema
        Gson gson = GsonUtils.getGson();
        try(OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(parameterSchema.toString()))) {
            w.write(gson.toJson(parameters.toValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Install imported data into their proper filesystem locations
        for(MISASample sample : samples.values()) {
            for(MISACache cache : sample.getImportedCaches()) {
                Path cachePath = importedDirectory.resolve(sample.name).resolve(cache.getFilesystemEntry().getInternalPath());
                cache.install(cachePath, forceCopy);
            }
        }
    }

    /**
     * Generates the parameter schema report
     * @return
     */
    @Override
    public ParameterSchemaValidityReport isValidParameter() {
        ParameterSchemaValidityReport report = new ParameterSchemaValidityReport();

        report.merge(algorithmParameters.isValidParameter(), "Algorithm parameters");
        report.merge(runtimeParameters.isValidParameter(), "Runtime parameters");

        for(MISASample sample : samples.values()) {
            report.merge(sample.isValidParameter(), "Samples", sample.name);
        }

        return report;
    }

    /**
     * Load parameters from the provided JSON string
     * @param jsonString
     */
    public void loadParameters(String jsonString) {
        Gson gson = GsonUtils.getGson();
        JsonObject root = gson.fromJson(jsonString, JsonObject.class);

        // Add missing samples
        if(root.has("samples")) {
            for(Map.Entry<String, JsonElement> kv : root.getAsJsonObject("samples").entrySet()) {

            }
        }

    }
}
