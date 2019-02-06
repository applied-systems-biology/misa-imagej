package org.hkijena.misa_imagej.api.json;

import com.google.gson.JsonElement;

public class JSONIndexPathSegment implements JSONPathSegment {

    private int index;

    public JSONIndexPathSegment(int index) {
        this.index = index;
    }

    @Override
    public JsonElement getElement(JsonElement jsonElement) {
        return jsonElement.getAsJsonArray().get(index);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }
}
