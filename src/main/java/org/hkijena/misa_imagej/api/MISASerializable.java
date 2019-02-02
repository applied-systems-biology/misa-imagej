package org.hkijena.misa_imagej.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MISASerializable {
    @SerializedName("misa:serialization-id")
    public String serializationId = "misa:serializable";

    @SerializedName("misa:serialization-hierarchy")
    public List<String> serializationHierarchy = new ArrayList<>();
}
