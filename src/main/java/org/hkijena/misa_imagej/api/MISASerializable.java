package org.hkijena.misa_imagej.api;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jfree.data.json.impl.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MISASerializable {
    @SerializedName("misa:serialization-id")
    public String serializationId = "misa:serializable";

    @SerializedName("misa:serialization-hierarchy")
    public List<String> serializationHierarchy = new ArrayList<>();

    /**
     * Full JSON data that is deserialized
     * This might contain additional properties that are not deserialized
     */
    public transient JsonObject rawData;

    @Override
    public String toString() {
        return serializationId;
    }
}
