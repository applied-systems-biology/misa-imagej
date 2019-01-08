package org.hkijena.misa_imagej.repository;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.utils.OSUtils;
import org.hkijena.misa_imagej.utils.OperatingSystem;
import org.hkijena.misa_imagej.utils.OperatingSystemArchitecture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MISA++ module located within a repository
 */
public class MISAModule {
    @SerializedName("id")
    public String id;

    @SerializedName("version")
    public String version;

    @SerializedName("name")
    public String name;

    @SerializedName("executables")
    public List<MISAExecutable> executables = new ArrayList<>();

    /**
     * Contains the path where this module was defined
     */
    public transient String definitionPath;

    /**
     * Parameter schema queried from the executable
     */
    private transient String parameterSchema;

    public MISAModule() {

    }

    /**
     * Finds the executable that matches best to the current operating system
     * If no matching executable is found, NULL is returned
     * @return
     */
    public MISAExecutable getBestMatchingExecutable() {
        OperatingSystem os = OSUtils.detectOperatingSystem();
        OperatingSystemArchitecture arch = OSUtils.detectArchitecture();
        for(MISAExecutable e : executables) {
            if(e.operatingSystem == os && e.operatingSystemArchitecture == arch)
                return e;
        }
        for(MISAExecutable e : executables) {
            if(OSUtils.isCompatible(os, arch, e.operatingSystem, e.operatingSystemArchitecture))
                return e;
        }
        return null;
    }

    /**
     * Returns the parameter schema JSON if applicable
     * @return The parameter schema. If no matching executable is found or the executable crashes, returns null
     */
    public String getParameterSchema() {
        if(parameterSchema == null) {
            MISAExecutable executable = getBestMatchingExecutable();
            if(executable != null) {
                parameterSchema = executable.queryParameterSchema();
            }
        }
        return parameterSchema;
    }

    @Override
    public String toString() {
        return name + " (" + id + "-" + version + ")";
    }
}
