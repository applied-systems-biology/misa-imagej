package org.hkijena.misa_imagej.api.workbench;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.*;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.repository.MISAModuleInfo;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MISAOutput {

    private Path rootPath;
    private MISAModuleInstance moduleInstance;
    private MISAModuleInfo moduleInfo;
    private Path runtimeLogPath;

    public MISAOutput(Path rootPath) throws IOException {
        this.rootPath = rootPath;
        this.runtimeLogPath = rootPath.resolve("runtime-log.json");
        loadParameterSchema();
        loadModuleInfo();
        loadParameters();
        loadFilesystem();
        loadCaches();
    }

    private void loadParameterSchema() throws IOException {
        Gson gson = GsonUtils.getGson();
        JSONSchemaObject schema = gson.fromJson(new String(Files.readAllBytes(getRootPath().resolve("parameter-schema.json"))), JSONSchemaObject.class);
        schema.setId("parameters");
        schema.update();
        moduleInstance = new MISAModuleInstance(schema);
    }

    private void loadModuleInfo() throws IOException {
        Gson gson = GsonUtils.getGson();
        moduleInfo = gson.fromJson(new String(Files.readAllBytes(getRootPath().resolve("misa-module-info.json"))), MISAModuleInfo.class);
    }

    private void loadParameters() throws IOException {
        moduleInstance.loadParameters(getRootPath().resolve("parameters.json"),
                MISASamplePolicy.createMissingSamples);
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

    private void loadCaches() throws IOException {
//        for(MISASample sample : moduleInstance.getSamples()) {
//            Path importedCacheRootAttachmentsPath = rootPath.resolve("attachments").resolve("imported").resolve(sample.name);
//            Path exportedCacheRootAttachmentsPath = rootPath.resolve("attachments").resolve("exported").resolve(sample.name);
//
//            for(MISACache cache : sample.getImportedCaches()) {
//                if(cache.getRelativePath() != null) {
//                    Path cacheAttachmentsPath = importedCacheRootAttachmentsPath.resolve(cache.getRelativePath());
//
//                    if(cacheAttachmentsPath.toFile().isDirectory()) {
//                        loadCacheAttachments(cacheAttachmentsPath, cache);
//                    }
//                }
//            }
//
//            for(MISACache cache : sample.getExportedCaches()) {
//                if(cache.getRelativePath() != null) {
//                    Path cacheAttachmentsPath = exportedCacheRootAttachmentsPath.resolve(cache.getRelativePath());
//
//                    if(cacheAttachmentsPath.toFile().isDirectory()) {
//                        loadCacheAttachments(cacheAttachmentsPath, cache);
//                    }
//                }
//            }
//        }
    }

//    private void loadCacheAttachments(Path cacheAttachmentsPath, MISACache cache) throws IOException {
//        final Gson gson = GsonUtils.getGson();
//        for(Path path : Files.find(cacheAttachmentsPath, Integer.MAX_VALUE, (path, basicFileAttributes) ->
//                basicFileAttributes.isRegularFile() && path.getFileName().toString().endsWith(".json")).collect(Collectors.toList())) {
//
//            Path subCachePath = cacheAttachmentsPath.relativize(path);
//
//            for(Map.Entry<String, JsonElement> kv : gson.fromJson(new String(Files.readAllBytes(path)),
//                    JsonObject.class).getAsJsonObject().entrySet()) {
//                MISAAttachmentLocation attachmentLocation = new MISAAttachmentLocation();
//                attachmentLocation.subCachePath = subCachePath;
//                attachmentLocation.attachmentIndex = kv.getKey();
//                cache.getAttachments().put(attachmentLocation, new MISAAttachment(attachmentLocation, path, cache));
//            }
//        }
//    }

    public static void main(String[] args) throws IOException {
        MISAOutput output = new MISAOutput(Paths.get("/home/rgerst/tmp/glomeruli_full_quantified/"));
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

    public Path getRuntimeLogPath() {
        return runtimeLogPath;
    }
}
