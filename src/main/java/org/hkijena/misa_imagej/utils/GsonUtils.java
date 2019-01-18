package org.hkijena.misa_imagej.utils;

import com.google.gson.*;
import org.hkijena.misa_imagej.parametereditor.json_schema.JSONSchemaObjectType;
import org.hkijena.misa_imagej.parametereditor.json_schema.JSONSchemaObjectTypeAdapter;

import java.lang.reflect.Type;

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
