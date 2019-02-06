package org.hkijena.misa_imagej.api;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.hkijena.misa_imagej.api.attachments.MISALocatable;
import org.hkijena.misa_imagej.api.attachments.MISALocation;
import org.hkijena.misa_imagej.api.attachments.ome.MISAOMEPlanesLocation;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MISASerializableRegistry {
    private static Map<String, Class<? extends MISASerializable>> registeredCaches = new HashMap<>();
    private static boolean isInitialized = false;

    private MISASerializableRegistry() {

    }

    /**
     * Registers an editor class for a serialization Id
     * @param serializationId
     * @param cacheClass
     */
    public static void register(String serializationId, Class<? extends MISASerializable> cacheClass) {
        registeredCaches.put(serializationId, cacheClass);
    }

    /**
     * Deserializes a JSON element into a serializable
     * @param element
     * @return
     */
    public static MISASerializable deserialize(JsonElement element) {
        if (!isInitialized)
            initialize();
        if(element.isJsonObject()) {
            if(element.getAsJsonObject().has("misa:serialization-id")) {
                String id = element.getAsJsonObject().get("misa:serialization-id").getAsString();
                if(registeredCaches.containsKey(id)) {
                    Gson gson = GsonUtils.getGson();
                    MISASerializable result = gson.fromJson(element, registeredCaches.get(id));
                    result.rawData = element.getAsJsonObject();
                    return result;
                }
                else {
                    Gson gson = GsonUtils.getGson();
                    MISASerializable result = null;
                    // Deserialize to the best matching class
                    List<String> hierarchy = gson.fromJson(element.getAsJsonObject().get("misa:serialization-hierarchy"), List.class);
                    for(String hid : Lists.reverse(hierarchy)) {
                        if(registeredCaches.containsKey(hid)) {
                            result = gson.fromJson(element, registeredCaches.get(hid));
                            break;
                        }
                    }
                    if(result == null) {
                        result = gson.fromJson(element, MISASerializable.class);
                    }
                    result.rawData = element.getAsJsonObject();
                    return result;
                }
            }
            return null;
        }
        else {
            return null;
        }
    }

    public static void initialize() {
        register("misa:attachments/location", MISALocation.class);
        register("misa:attachments/locatable", MISALocatable.class);
        register("misa_ome:attachments/planes-location", MISAOMEPlanesLocation.class);
        isInitialized = true;
    }
}
