package org.hkijena.misa_imagej.api.workbench;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.MISASerializableRegistry;
import org.hkijena.misa_imagej.api.MISAAttachmentLocation;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.MISAParameterSchema;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.MISARuntimeLog;
import org.hkijena.misa_imagej.api.repository.MISAModuleInfo;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class MISAOutput {

    private Path rootPath;
    private MISAParameterSchema parameterSchema;
    private MISAModuleInfo moduleInfo;
    private MISARuntimeLog runtimeLog;

    public MISAOutput(Path rootPath) throws IOException {
        this.rootPath = rootPath;
        loadParameterSchema();
        loadModuleInfo();
        loadParameters();
        loadRuntimeLog();
        loadCaches();
    }

    private void loadParameterSchema() throws IOException {
        Gson gson = GsonUtils.getGson();
        JSONSchemaObject schema = gson.fromJson(new String(Files.readAllBytes(getRootPath().resolve("parameter-schema.json"))), JSONSchemaObject.class);
        schema.id = "parameters";
        schema.update();
        parameterSchema = new MISAParameterSchema(schema);
    }

    private void loadModuleInfo() throws IOException {
        Gson gson = GsonUtils.getGson();
        moduleInfo = gson.fromJson(new String(Files.readAllBytes(getRootPath().resolve("misa-module-info.json"))), MISAModuleInfo.class);
    }

    private void loadParameters() throws  IOException {
        Gson gson = GsonUtils.getGson();
        parameterSchema.loadParameters(gson.fromJson(new String(Files.readAllBytes(getRootPath().resolve("parameters.json"))), JsonObject.class));
    }

    private void loadRuntimeLog() throws  IOException {
        if(getRootPath().resolve("runtime-log.json").toFile().isFile()) {
            Gson gson = GsonUtils.getGson();
            runtimeLog = gson.fromJson(new String(Files.readAllBytes(getRootPath().resolve("runtime-log.json"))), MISARuntimeLog.class);
        }
    }

    private void loadCaches() throws IOException {
        for(MISASample sample : parameterSchema.getSamples()) {
            Path cacheRootPath = rootPath.resolve(sample.name);
            Path importedCacheRootAttachmentsPath = rootPath.resolve("attachments").resolve("imported").resolve(sample.name);
            Path exportedCacheRootAttachmentsPath = rootPath.resolve("attachments").resolve("exported").resolve(sample.name);

            for(MISACache cache : sample.getImportedCaches()) {
                if(cache.getRelativePath() != null) {
                    Path cachePath = cacheRootPath.resolve(cache.getRelativePath());
                    Path cacheAttachmentsPath = importedCacheRootAttachmentsPath.resolve(cache.getRelativePath());

                    if(cacheAttachmentsPath.toFile().isDirectory()) {
                        loadCacheAttachments(cacheAttachmentsPath, cache);
                    }
                }
            }

            for(MISACache cache : sample.getExportedCaches()) {
                if(cache.getRelativePath() != null) {
                    Path cachePath = cacheRootPath.resolve(cache.getRelativePath());
                    Path cacheAttachmentsPath = exportedCacheRootAttachmentsPath.resolve(cache.getRelativePath());

                    if(cacheAttachmentsPath.toFile().isDirectory()) {
                        loadCacheAttachments(cacheAttachmentsPath, cache);
                    }
                }
            }
        }
    }

    private void loadCacheAttachments(Path cacheAttachmentsPath, MISACache cache) throws IOException {
        final Gson gson = GsonUtils.getGson();
        for(Path path : Files.find(cacheAttachmentsPath, Integer.MAX_VALUE, (path, basicFileAttributes) ->
                basicFileAttributes.isRegularFile() && path.getFileName().toString().endsWith(".json")).collect(Collectors.toList())) {

            Path subCachePath = cacheAttachmentsPath.relativize(path);

            for(Map.Entry<String, JsonElement> kv : gson.fromJson(new String(Files.readAllBytes(path)),
                    JsonObject.class).getAsJsonObject().entrySet()) {
                MISAAttachmentLocation attachmentLocation = new MISAAttachmentLocation();
                attachmentLocation.subCachePath = subCachePath;
                attachmentLocation.attachmentIndex = kv.getKey();
                cache.getAttachments().put(attachmentLocation, MISASerializableRegistry.deserialize(kv.getValue()));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MISAOutput output = new MISAOutput(Paths.get("/home/rgerst/tmp/ome_glomeruli/output/"));
        System.out.println("test");
    }

    public Path getRootPath() {
        return rootPath;
    }

    public MISAParameterSchema getParameterSchema() {
        return parameterSchema;
    }

    public MISAModuleInfo getModuleInfo() {
        return moduleInfo;
    }
}
