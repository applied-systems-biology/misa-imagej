package org.hkijena.misa_imagej.api;

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

    /**
     * Returns the total runtime
     * @return
     */
    public double getTotalRuntime() {
        double result = 0;
        for(List<Entry> list : entries.values()) {
            for(Entry entry : list) {
                result = Math.max(entry.endTime, result);
            }
        }
        return result;
    }

    /**
     * Estimates the runtime if no parallelization would be used
     * @return
     */
    public double getUnparallelizedRuntime() {
        if(entries.size() <= 1)
            return getTotalRuntime();
        double result = 0;
        for(List<Entry> list : entries.values()) {
            for(Entry entry : list) {
                result += entry.endTime - entry.startTime;
            }
        }
        return result;
    }

    /**
     * Returns the speedup by parallelization
     * @return
     */
    public double getParallelizationSpeedup() {
        return getUnparallelizedRuntime() / getTotalRuntime();
    }


}
