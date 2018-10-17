package org.hkijena.misa_imagej;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hkijena.misa_imagej.data.*;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;
import org.scijava.log.LogService;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class MISAParameterSchema {

    private JSONSchemaObject jsonSchemaObject;
    private List<String> objectNames = new ArrayList<>();
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public MISAParameterSchema(JSONSchemaObject object) {
        jsonSchemaObject = object;
        importFilesystem();
    }

    public List<String> getObjectNames() {
        return Collections.unmodifiableList(objectNames);
    }

    public void addObject(String name) {
        List<String> old = new ArrayList<>(objectNames);
        objectNames.add(name);
        propertyChangeSupport.firePropertyChange("objectNames", old, objectNames);
        getObjectParameters().addAdditionalProperty(name);
        getFilesystemSchema().getPropertyFromPath("json-data", "imported", "children").addAdditionalProperty(name);
        getFilesystemSchema().getPropertyFromPath("json-data", "exported", "children").addAdditionalProperty(name);
        importFilesystem();
    }

    private void importFilesystem() {
        ArrayList<JSONSchemaObject> leaves = new ArrayList<>();
        getFilesystemLeaves(getImportedFilesystemSchema(), leaves);
        for(JSONSchemaObject object : leaves) {
            object.filesystemData = new MISAImportedData(object);
        }
        leaves.clear();
        getFilesystemLeaves(getExportedFilesystemSchema(), leaves);
        for(JSONSchemaObject object : leaves) {
            object.filesystemData = new MISAExportedData(object);
        }

    }

    public JSONSchemaObject getAlgorithmParameters() {
        return jsonSchemaObject.properties.get("algorithm");
    }

    public JSONSchemaObject getObjectParameters() {
        return jsonSchemaObject.properties.get("objects");
    }

    public JSONSchemaObject getRuntimeParameters() {
        return jsonSchemaObject.properties.get("runtime");
    }

    public JSONSchemaObject getFilesystemSchema() {
        return jsonSchemaObject.properties.get("filesystem");
    }

    public JSONSchemaObject getImportedFilesystemSchema() {
        return jsonSchemaObject.getPropertyFromPath("filesystem", "json-data", "imported");
    }

    public JSONSchemaObject getExportedFilesystemSchema() {
        return jsonSchemaObject.getPropertyFromPath("filesystem", "json-data", "exported");
    }

    public JSONSchemaObject getJsonSchemaObject() {
        return jsonSchemaObject;
    }

    private void getFilesystemLeaves(JSONSchemaObject object, List<JSONSchemaObject> output) {
        if (object.properties.get("type").default_value.equals("file")) {
            output.add(object);
        } else if (object.properties.get("type").default_value.equals("folder")) {
            if(object.properties.containsKey("children")) {
                for (Map.Entry<String, JSONSchemaObject> kv : object.properties.get("children").properties.entrySet()) {
                    getFilesystemLeaves(kv.getValue(), output);
                }
            }
            else {
                output.add(object);
            }
        } else {
            throw new RuntimeException("Unknown type " + object.properties.get("type").default_value);
        }
    }

    public void addPropertyChangeListener( PropertyChangeListener l )
    {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l )
    {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    /**
     * Runs the MISADataImportSource instances
     * @param importedDirectory
     */
    private void prepareImportedFiles(MISADialog app, Path importedDirectory, boolean forceCopy) {
        List<JSONSchemaObject> flat = getImportedFilesystemSchema().flatten();
        for(int i = 0; i < flat.size(); ++i) {
            JSONSchemaObject object = flat.get(i);
            if(object.filesystemData != null) {
                MISAImportedData data = (MISAImportedData)object.filesystemData;
                app.getLog().info("[" + (i + 1) + " / " + flat.size() + "] Importing data " + data.getRelativePath().toString() + " into " + importedDirectory.toString());
                data.getImportSource().runImport(importedDirectory, forceCopy);
            }
        }
    }

    /**
     * Checks if a parameter JSON can be written and shows an error message if something is wrong
     * @param parent
     * @return
     */
    public boolean canWriteParameterJSON(Component parent) {

        // Check if all importers are set
        StringBuilder message = new StringBuilder();
        boolean success = true;

        {
            boolean wroteInitialMessage = false;
            for(JSONSchemaObject object : getImportedFilesystemSchema().flatten()) {
                if (object.filesystemData != null) {
                    MISAImportedData data = (MISAImportedData)object.filesystemData;
                    if(data.getImportSource() == null) {
                        success = false;
                        if(!wroteInitialMessage) {
                            message.append("You still need to setup following input data:\n");
                            wroteInitialMessage = true;
                        }
                        message.append(data.getRelativePath().toString()).append("\n");
                    }
                }
            }
            if(wroteInitialMessage)
                message.append("\n");
        }

        // Check if all values are set
        {
            boolean wroteInitialMessage = false;
            for(JSONSchemaObject object : getObjectParameters().flatten()) {
                if (!object.hasValue()) {
                    success = false;
                    if(!wroteInitialMessage) {
                        message.append("Following object parameters are not set, yet:\n");
                        wroteInitialMessage = true;
                    }
                    message.append(object.getValuePath()).append("\n");
                }
            }
            if(wroteInitialMessage)
                message.append("\n");
        }
        {
            boolean wroteInitialMessage = false;
            for(JSONSchemaObject object : getAlgorithmParameters().flatten()) {
                if (!object.hasValue()) {
                    success = false;
                    if(!wroteInitialMessage) {
                        message.append("Following algorithm parameters are not set, yet:\n");
                        wroteInitialMessage = true;
                    }
                    message.append(object.getValuePath()).append("\n");
                }
            }
            if(wroteInitialMessage)
                message.append("\n");
        }

        if(!success) {
            JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
        }

        return success;
    }

    /**
     * Writes the final JSON parameter
     * @param parameterSchema where the parameter will be written
     * @param importedDirectory The physical path of the import directory where ImageJ data is exported if needed.
     * @param exportedDirectory The physical path of the export directory where everything will be cached
     * @param forceCopy If true, the importer will copy the files into the imported directory even if not necessary
     */
    public void writeParameterJSON(MISADialog app, Path parameterSchema, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {

        // Set necessary variables inside the JSON parameters
        if(!relativeDirectories) {
            getImportedFilesystemSchema().getPropertyFromPath("external-path").setValue(importedDirectory.toString());
            getExportedFilesystemSchema().getPropertyFromPath("external-path").setValue(exportedDirectory.toString());
        }
        else {
            getImportedFilesystemSchema().getPropertyFromPath("external-path").setValue(importedDirectory.getFileName().toString());
            getExportedFilesystemSchema().getPropertyFromPath("external-path").setValue(exportedDirectory.getFileName().toString());
        }

        prepareImportedFiles(app, importedDirectory, forceCopy);

        app.getLog().info("Writing parameter schema into " + parameterSchema.toString());
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting().serializeNulls();
        Gson gson = builder.create();
        try(OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(parameterSchema.toString()))) {
            w.write(gson.toJson(jsonSchemaObject.toValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
