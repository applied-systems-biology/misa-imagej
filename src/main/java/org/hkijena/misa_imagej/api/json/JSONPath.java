package org.hkijena.misa_imagej.api.json;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;

import java.util.ArrayList;

public class JSONPath implements JSONPathSegment {

    private ArrayList<JSONPathSegment> segments = new ArrayList<>();

    public JSONPath() {

    }

    @Override
    public JsonElement getElement(JsonElement jsonElement) {
        JsonElement result = jsonElement;
        for(JSONPathSegment segment : segments) {
            result = segment.getElement(result);
        }
        return result;
    }

    public JSONPath resolve(String property) {
        return resolve(new JSONPropertyPathSegment(property));
    }

    public JSONPath resolve(int index) {
        return resolve(new JSONIndexPathSegment(index));
    }

    public JSONPath resolve(JSONPathSegment segment) {
        JSONPath result = new JSONPath();
        result.segments.addAll(segments);
        result.segments.add(segment);
        return result;
    }

    @Override
    public String toString() {
        Joiner joiner = Joiner.on('/');
        return joiner.join(segments);
    }
}
