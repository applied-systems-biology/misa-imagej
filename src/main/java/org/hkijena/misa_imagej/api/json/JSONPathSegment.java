package org.hkijena.misa_imagej.api.json;

import com.google.gson.JsonElement;

/**
 * Interface for adressing values in JSON
 */
public interface JSONPathSegment {
    JsonElement getElement(JsonElement jsonElement);
}
