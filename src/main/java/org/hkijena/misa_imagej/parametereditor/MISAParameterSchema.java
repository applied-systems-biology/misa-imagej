package org.hkijena.misa_imagej.parametereditor;

import org.hkijena.misa_imagej.parametereditor.cache.MISADataIOType;
import org.hkijena.misa_imagej.parametereditor.json_schema.JSONSchemaObject;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.*;

public class MISAParameterSchema {

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
        sampleParametersTemplate = object.properties.get("objects").getAdditionalPropertiesTemplate();
        sampleImportedFilesystemTemplate = new MISAFilesystemEntry(null,
                object.getPropertyFromPath("filesystem", "json-data", "imported", "children").getAdditionalPropertiesTemplate(),
                "",
                MISADataIOType.Imported);
        sampleExportedFilesystemTemplate = new MISAFilesystemEntry(null,
                object.getPropertyFromPath("filesystem", "json-data", "exported", "children").getAdditionalPropertiesTemplate(),
                "",
                MISADataIOType.Exported);
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
        samples.remove(name);
        propertyChangeSupport.firePropertyChange("samples", null, null);
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
     * Runs the MISADataImportSource instances
     *
     * @param importedDirectory
     */
    private void prepareImportedFiles(MISAModuleUI app, Path importedDirectory, boolean forceCopy) {
//        List<JSONSchemaObject> flat = getImportedFilesystemSchema().flatten();
//        for(int i = 0; i < flat.size(); ++i) {
//            JSONSchemaObject object = flat.get(i);
//            if(object.filesystemData != null) {
//                MISAImportedData data = (MISAImportedData)object.filesystemData;
//                Path subpath = importedDirectory.resolve(data.getRelativePath());
//                app.getLogService().info("[" + (i + 1) + " / " + flat.size() + "] Importing data " + data.getRelativePath().toString() + " into " + subpath.toString());
//                data.getImportSource().runImport(subpath, forceCopy);
//            }
//        }
    }

    /**
     * Checks if a parameter JSON can be written and shows an error message if something is wrong
     *
     * @param parent
     * @return
     */
    public boolean canWriteParameterJSON(Component parent) {
        return true;

//        // Check if all importers are set
//        StringBuilder message = new StringBuilder();
//        boolean success = true;
//
//        {
//            boolean wroteInitialMessage = false;
////            for(JSONSchemaObject object : getImportedFilesystemSchema().flatten()) {
////                if (object.filesystemData != null) {
////                    MISAImportedData data = (MISAImportedData)object.filesystemData;
////                    if(data.getImportSource() == null) {
////                        success = false;
////                        if(!wroteInitialMessage) {
////                            message.append("You still need to setup following input data:\n");
////                            wroteInitialMessage = true;
////                        }
////                        message.append(data.getRelativePath().toString()).append("\n");
////                    }
////                }
////            }
//            if(wroteInitialMessage)
//                message.append("\n");
//        }
//
//        // Check if all values are set
//        {
//            boolean wroteInitialMessage = false;
//            for(JSONSchemaObject object : getObjectParameters().flatten()) {
//                if (!object.hasValue()) {
//                    success = false;
//                    if(!wroteInitialMessage) {
//                        message.append("Following object parameters are not set, yet:\n");
//                        wroteInitialMessage = true;
//                    }
//                    message.append(object.getValuePath()).append("\n");
//                }
//            }
//            if(wroteInitialMessage)
//                message.append("\n");
//        }
//        {
//            boolean wroteInitialMessage = false;
//            for(JSONSchemaObject object : getAlgorithmParameters().flatten()) {
//                if (!object.hasValue()) {
//                    success = false;
//                    if(!wroteInitialMessage) {
//                        message.append("Following algorithm parameters are not set, yet:\n");
//                        wroteInitialMessage = true;
//                    }
//                    message.append(object.getValuePath()).append("\n");
//                }
//            }
//            if(wroteInitialMessage)
//                message.append("\n");
//        }
//
//        if(!success) {
//            JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
//        }
//
//        return success;
    }

    /**
     * Writes the final JSON parameter
     *
     * @param parameterSchema   where the parameter will be written
     * @param importedDirectory The physical path of the import directory where ImageJ data is exported if needed.
     * @param exportedDirectory The physical path of the export directory where everything will be cached
     * @param forceCopy         If true, the importer will copy the files into the imported directory even if not necessary
     */
    public void writeParameterJSON(MISAModuleUI app, Path parameterSchema, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {

//        // Set necessary variables inside the JSON parameters
//        if(!relativeDirectories) {
//            getImportedFilesystemSchema().getPropertyFromPath("external-path").setValue(importedDirectory.toString());
//            getExportedFilesystemSchema().getPropertyFromPath("external-path").setValue(exportedDirectory.toString());
//        }
//        else {
//            getImportedFilesystemSchema().getPropertyFromPath("external-path").setValue(importedDirectory.getFileName().toString());
//            getExportedFilesystemSchema().getPropertyFromPath("external-path").setValue(exportedDirectory.getFileName().toString());
//        }
//
//        prepareImportedFiles(app, importedDirectory, forceCopy);
//
//        app.getLogService().info("Writing parameter schema into " + parameterSchema.toString());
//        GsonBuilder builder = new GsonBuilder().setPrettyPrinting().serializeNulls();
//        Gson gson = builder.create();
//        try(OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(parameterSchema.toString()))) {
//            w.write(gson.toJson(jsonSchemaObject.toValue()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
