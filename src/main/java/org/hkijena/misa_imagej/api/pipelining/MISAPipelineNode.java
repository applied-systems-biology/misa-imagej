package org.hkijena.misa_imagej.api.pipelining;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISAModuleInstance;

public class MISAPipelineNode {

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("module-name")
    public String moduleName;

    @SerializedName("x")
    public int x;

    @SerializedName("y")
    public int y;

    public transient MISAModuleInstance moduleInstance;

    public MISAPipelineNode() {

    }

}
