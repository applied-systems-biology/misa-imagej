package org.hkijena.misa_imagej.utils;

import com.google.gson.*;
import org.hkijena.misa_imagej.api.json.JSONSchemaObjectType;
import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class GsonUtils {
    private GsonUtils() {

    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        builder.registerTypeAdapter(JSONSchemaObjectType.class, new JSONSchemaObjectType.JSONAdapter());
        builder.registerTypeAdapter(Path.class, new NIOPathJsonTypeAdapter());
        builder.registerTypeAdapter(MISAPipeline.class, new MISAPipeline.JSONAdapter());
        return builder.create();
    }

    public static <T> T fromJsonFile(Gson gson, Path filename, Class<T> klass) throws IOException {
        try(FileReader reader = new FileReader(filename.toString())) {
            return gson.fromJson(reader, klass);
        }
    }

}
