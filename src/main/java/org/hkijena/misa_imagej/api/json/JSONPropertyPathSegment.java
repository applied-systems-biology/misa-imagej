package org.hkijena.misa_imagej.api.json;

import com.google.gson.JsonElement;

public class JSONPropertyPathSegment implements JSONPathSegment {

    private String property;

    public JSONPropertyPathSegment(String property) {
        this.property = property;
    }

    @Override
    public JsonElement getElement(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().get(property);
    }

    @Override
    public String toString() {
        return property;
    }
}
