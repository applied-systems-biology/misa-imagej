package org.hkijena.misa_imagej.utils;

import com.google.gson.*;
import org.hkijena.misa_imagej.api.json.JSONSchemaObjectType;
import org.hkijena.misa_imagej.api.json.JSONSchemaObjectTypeAdapter;

import java.nio.file.Path;

public class GsonUtils {
    private GsonUtils() {

    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        builder.registerTypeAdapter(JSONSchemaObjectType.class, new JSONSchemaObjectTypeAdapter());
        builder.registerTypeAdapter(Path.class, new NIOPathJsonTypeAdapter());
        return builder.create();
    }
}
