package org.hkijena.misa_imagej.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.util.HashMap;
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
                    return gson.fromJson(element, registeredCaches.get(id));
                }
                else {
                    Gson gson = GsonUtils.getGson();
                    return gson.fromJson(element, MISASerializable.class);
                }
            }
            return null;
        }
        else {
            return null;
        }
    }

    public static void initialize() {
        isInitialized = true;
    }
}
