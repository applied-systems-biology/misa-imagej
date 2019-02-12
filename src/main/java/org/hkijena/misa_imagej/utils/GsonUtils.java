package org.hkijena.misa_imagej.utils;

import com.google.gson.*;
import org.hkijena.misa_imagej.api.json.JSONSchemaObjectType;
import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class GsonUtils {
    private GsonUtils() {

    }

    public static GsonBuilder getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        builder.registerTypeAdapter(JSONSchemaObjectType.class, new JSONSchemaObjectType.JSONAdapter());
        builder.registerTypeAdapter(Path.class, new NIOPathJsonTypeAdapter());
        builder.registerTypeAdapter(MISAPipeline.class, new MISAPipeline.JSONAdapter());
        return builder;
    }

    public static Gson getGson() {
        return getGsonBuilder().create();
    }

    public static <T> T fromJsonFile(Gson gson, Path filename, Class<T> klass) throws IOException {
        try(FileReader reader = new FileReader(filename.toString())) {
            return gson.fromJson(reader, klass);
        }
    }

    public static void toJsonFile(Gson gson, Object object, Path filename) throws IOException {
        try(FileWriter writer = new FileWriter(filename.toString())) {
            writer.write(gson.toJson(object));
        }
    }

}
