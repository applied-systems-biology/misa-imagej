package org.hkijena.misa_imagej.api.workbench;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.parameterschema.JSONSchemaObject;
import org.hkijena.misa_imagej.api.parameterschema.MISAParameterSchema;
import org.hkijena.misa_imagej.api.parameterschema.MISASample;
import org.hkijena.misa_imagej.api.repository.MISAModuleInfo;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MISAOutput {

    private Path rootPath;
    private MISAParameterSchema parameterSchema;
    private MISAModuleInfo moduleInfo;

    public MISAOutput(Path rootPath) throws IOException {
        this.rootPath = rootPath;
        loadParameterSchema();
        loadModuleInfo();
        loadParameters();
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

    private void loadCaches() throws IOException {
        for(MISASample sample : parameterSchema.getSamples()) {
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
