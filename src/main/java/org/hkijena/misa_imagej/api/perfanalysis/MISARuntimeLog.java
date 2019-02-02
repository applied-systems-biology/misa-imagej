package org.hkijena.misa_imagej.api.perfanalysis;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISASerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MISARuntimeLog extends MISASerializable {

    @SerializedName("entries")
    public Map<String, List<Entry>> entries = new HashMap<>();

    public static class Entry {

        @SerializedName("name")
        public String name;

        @SerializedName("end-time")
        public double endTime;

        @SerializedName("start-time")
        public double startTime;
    }
}
