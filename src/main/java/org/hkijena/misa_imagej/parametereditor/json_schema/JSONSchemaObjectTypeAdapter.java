package org.hkijena.misa_imagej.parametereditor.json_schema;

import com.google.gson.*;

import java.lang.reflect.Type;

public class JSONSchemaObjectTypeAdapter implements JsonDeserializer<JSONSchemaObjectType>, JsonSerializer<JSONSchemaObjectType> {
    @Override
    public JSONSchemaObjectType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        switch (jsonElement.getAsString()) {
            case "object":
                return JSONSchemaObjectType.jsonObject;
            case "number":
                return JSONSchemaObjectType.jsonNumber;
            case "boolean":
                return JSONSchemaObjectType.jsonBoolean;
            case "string":
                return JSONSchemaObjectType.jsonString;
            case "array":
                return JSONSchemaObjectType.jsonArray;
            default:
                throw new JsonParseException("Unknown type " + jsonElement.getAsString());
        }
    }

    @Override
    public JsonElement serialize(JSONSchemaObjectType jsonSchemaObjectType, Type type, JsonSerializationContext jsonSerializationContext) {
        switch(jsonSchemaObjectType) {
            case jsonObject:
                return new JsonPrimitive("object");
            case jsonBoolean:
                return new JsonPrimitive("boolean");
            case jsonString:
                return new JsonPrimitive("string");
            case jsonNumber:
                return new JsonPrimitive("number");
            case jsonArray:
                return new JsonPrimitive("array");
            default:
                throw new UnsupportedOperationException("Unknown schema object type " + jsonSchemaObjectType);
        }
    }
}
