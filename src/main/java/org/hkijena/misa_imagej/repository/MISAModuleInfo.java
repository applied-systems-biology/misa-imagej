package org.hkijena.misa_imagej.repository;

import com.google.gson.annotations.SerializedName;

public class MISAModuleInfo {
    @SerializedName("name")
    public String name;

    @SerializedName("version")
    public String version;

    @SerializedName("description")
    public String description;

    @Override
    public String toString() {
        if(description == null || description.isEmpty()) {
            return name + "-" + version;
        }
        else {
            return description + " (" + name + "-" + version + ")";
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        if(description == null || description.isEmpty())
            return name;
        else
            return description;
    }
}
