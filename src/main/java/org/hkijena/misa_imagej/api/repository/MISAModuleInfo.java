package org.hkijena.misa_imagej.api.repository;

import com.google.gson.annotations.SerializedName;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MISAModuleInfo {
    @SerializedName("name")
    public String name;

    @SerializedName("version")
    public String version;

    @SerializedName("description")
    public String description;

    @SerializedName("dependencies")
    public List<MISAModuleInfo> dependencies = new ArrayList<>();

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

    /**
     * Automatically generates a color from the name
     * @return
     */
    public Color toColor() {
        float h = Math.abs(getName().hashCode() % 256) / 255.0f;
        return Color.getHSBColor(h, 0.8f, 0.8f);
    }
}
