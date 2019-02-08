package org.hkijena.misa_imagej.api.repository;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.OSUtils;
import org.hkijena.misa_imagej.utils.OperatingSystem;
import org.hkijena.misa_imagej.utils.OperatingSystemArchitecture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MISA++ module located within a repository
 */
public class MISAModule {

    @SerializedName("executable-path")
    public String executablePath;

    @SerializedName("operating-system")
    public OperatingSystem operatingSystem;

    @SerializedName("architecture")
    public OperatingSystemArchitecture operatingSystemArchitecture;

    /**
     * Contains the info about this module
     */
    private transient MISAModuleInfo moduleInfo;

    /**
     * Contains the path where this module was defined
     */
    transient String linkPath;

    /**
     * Parameter schema queried from the executable
     */
    private transient String parameterSchema;

    public MISAModule() {

    }

    /**
     * Returns or queries the module info
     * @return
     */
    public MISAModuleInfo getModuleInfo() {
        if(moduleInfo == null && isCompatible()) {
            String infoString = queryModuleInfo();
            if(infoString != null) {
                Gson gson = GsonUtils.getGson();
                moduleInfo = gson.fromJson(infoString, MISAModuleInfo.class);
            }
        }
        return moduleInfo;
    }

    /**
     * Finds the executable that matches best to the current operating system
     * If no matching executable is found, NULL is returned
     * @return
     */
    public boolean isCompatible() {
        OperatingSystem os = OSUtils.detectOperatingSystem();
        OperatingSystemArchitecture arch = OSUtils.detectArchitecture();
        return OSUtils.isCompatible(os, arch, operatingSystem, operatingSystemArchitecture);
    }

    /**
     * Returns the parameter schema JSON if applicable
     * @return The parameter schema. If no matching executable is found or the executable crashes, returns null
     */
    public String getParameterSchemaJSON() {
        if(parameterSchema == null && isCompatible()) {
            parameterSchema = queryParameterSchema();
        }
        return parameterSchema;
    }

    public String getLinkPath() {
        return linkPath;
    }

    /**
     * Returns the parameter schema if possible
     * @return The parameter Schema JSON if successful. Otherwise null.
     */
    private String queryParameterSchema() {
        try {
            Path tmppath = Files.createTempFile("MISAParameterSchema", ".json");
//            System.out.println(executablePath + " " + tmppath.toString());
            ProcessBuilder pb = new ProcessBuilder(executablePath, "--write-parameter-schema", tmppath.toString());
            Process p = pb.start();
            if(p.waitFor() == 0) {
                return new String(Files.readAllBytes(tmppath));
            }
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        }
        return null;
    }

    private String queryModuleInfo() {
        try {
            ProcessBuilder pb = new ProcessBuilder(executablePath, "--module-info");
            Process p = pb.start();
            if(p.waitFor() == 0) {
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    while ( (line = reader.readLine()) != null) {
                        builder.append(line);
                        builder.append(System.getProperty("line.separator"));
                    }
                    return builder.toString();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        if(getModuleInfo() == null)
            return "<Unable to load>";
        else
            return getModuleInfo().toString();
    }

    /**
     * Generates a filename for this module.
     * Does not include an extension
     * @return
     */
    public String getGeneratedFileName() {
        return getModuleInfo().getName() + "-" + getModuleInfo().getVersion() + "-" + operatingSystem.toString() + "-" + operatingSystemArchitecture.toString();
    }

    /**
     * Creates a new module instance
     * @return
     */
    public MISAModuleInstance instantiate() {
        Gson gson = GsonUtils.getGson();
        JSONSchemaObject schema = gson.fromJson(getParameterSchemaJSON(), JSONSchemaObject.class);
        schema.id = "parameters";
        schema.update();
        MISAModuleInstance instance = new MISAModuleInstance(schema);
        instance.setModuleInfo(getModuleInfo());
        instance.setModule(this);
        return instance;
    }

}
