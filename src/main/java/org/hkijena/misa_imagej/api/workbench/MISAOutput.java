package org.hkijena.misa_imagej.api.workbench;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.MISARuntimeLog;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.MISASamplePolicy;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.repository.MISAModuleInfo;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Represents a MISA++ output
 */
public class MISAOutput {

    private final Gson gson = GsonUtils.getGson();
    private Path rootPath;
    private MISAModuleInstance moduleInstance;
    private MISAModuleInfo moduleInfo;
    private MISARuntimeLog runtimeLog;
    private List<MISAAttachmentDatabase> attachments = new ArrayList<>();
    private Map<String, JSONSchemaObject> attachmentSchemas;
    private boolean loadedParameterJson = false;

    public MISAOutput(Path rootPath) throws IOException {
        this.rootPath = rootPath;
        loadParameterSchema();
        loadRuntimeLog();
        loadModuleInfo();
        loadParameters();
        loadFilesystem();
    }

    private void loadParameterSchema() throws IOException {
        if(Files.exists(getRootPath().resolve("parameter-schema.json"))) {
            JSONSchemaObject schema = GsonUtils.fromJsonFile(gson, getRootPath().resolve("parameter-schema.json"), JSONSchemaObject.class);
            schema.setId("parameters");
            schema.update();
            moduleInstance = new MISAModuleInstance(schema);
        }
    }

    private void loadRuntimeLog() throws IOException {
        if(Files.exists(getRootPath().resolve("runtime-log.json"))) {
            runtimeLog = GsonUtils.fromJsonFile(gson, getRootPath().resolve("runtime-log.json"), MISARuntimeLog.class);
        }
    }

    private void loadModuleInfo() throws IOException {
        if(Files.exists(getRootPath().resolve("misa-module-info.json"))) {
            moduleInfo = GsonUtils.fromJsonFile(gson, getRootPath().resolve("misa-module-info.json"), MISAModuleInfo.class);
        }
    }

    private void loadParameters() throws IOException {
        if(Files.exists(getRootPath().resolve("parameters.json"))) {
            moduleInstance.loadParameters(getRootPath().resolve("parameters.json"),
                    MISASamplePolicy.createMissingSamples);
            loadedParameterJson = true;
        }
    }

    private void loadFilesystem() throws IOException {
        Gson gson = GsonUtils.getGson();
        JsonObject parameters = gson.fromJson(new String(Files.readAllBytes(getRootPath().resolve("parameters.json"))), JsonObject.class);
        if(parameters.getAsJsonObject("filesystem").get("source").getAsString().equals("directories")) {
            Path inputDirectory = Paths.get(parameters.getAsJsonObject("filesystem").get("input-directory").getAsString());
            for(MISASample sample : moduleInstance.getSamples().values()) {
                sample.getImportedFilesystem().externalPath = inputDirectory.resolve(sample.getName()).toString();
                sample.getExportedFilesystem().externalPath = rootPath.resolve(sample.getName()).toString(); // Can load it from the root path
            }
        }
        else if(parameters.getAsJsonObject("filesystem").get("source").getAsString().equals("json")) {
            for(MISASample sample : moduleInstance.getSamples().values()) {
                sample.getExportedFilesystem().externalPath = rootPath.resolve(sample.getName()).toString(); // Can load it from the root path

                // Assign all other paths from JSON data
                sample.getImportedFilesystem().setExternalPathFromJson(parameters.getAsJsonObject("filesystem").getAsJsonObject("json-data").
                        getAsJsonObject("imported").getAsJsonObject(sample.getName()));
                sample.getImportedFilesystem().setExternalPathFromJson(parameters.getAsJsonObject("filesystem").getAsJsonObject("json-data").
                        getAsJsonObject("exported").getAsJsonObject(sample.getName()));
            }
        }

    }

    /**
     * Returns true if an attachment index is present
     * @return
     */
    public boolean hasAttachmentIndex() {
        return Files.exists(rootPath.resolve("attachment-index.sqlite"));
    }

    public Path getRootPath() {
        return rootPath;
    }

    public MISAModuleInstance getModuleInstance() {
        return moduleInstance;
    }

    public MISAModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public List<MISAAttachmentDatabase> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public Map<String, JSONSchemaObject> getAttachmentSchemas() {
        if(attachmentSchemas == null) {
            attachmentSchemas = new HashMap<>();
            Path path = rootPath.resolve("attachments").resolve("serialization-schemas-full.json");
            if(Files.exists(path)) {
                try {
                    Gson gson = GsonUtils.getGson();
                    JsonObject jsonObject = GsonUtils.fromJsonFile(gson, path, JsonObject.class);
                    for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        attachmentSchemas.put(entry.getKey(), gson.fromJson(entry.getValue(), JSONSchemaObject.class));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return attachmentSchemas;
    }

    public MISAAttachmentDatabase createAttachmentDatabase() {
        MISAAttachmentDatabase attachment = new MISAAttachmentDatabase(this);
        attachments.add(attachment);
        return attachment;
    }

    public boolean hasAttachmentSchemas() {
        return attachmentSchemas != null;
    }

    public boolean hasLoadedParameters() {
        return loadedParameterJson;
    }

    public MISARuntimeLog getRuntimeLog() {
        return runtimeLog;
    }
}
