package org.hkijena.misa_imagej.utils;

import com.google.gson.*;
import org.hkijena.misa_imagej.api.parameterschema.JSONSchemaObjectType;
import org.hkijena.misa_imagej.api.parameterschema.JSONSchemaObjectTypeAdapter;

public class GsonUtils {
    private GsonUtils() {

    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        builder.registerTypeAdapter(JSONSchemaObjectType.class, new JSONSchemaObjectTypeAdapter());
        return builder.create();
    }
}
