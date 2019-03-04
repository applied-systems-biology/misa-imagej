package org.hkijena.misa_imagej.extension.attachments;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MISAOMEPlanesLocation extends MISALocation {

    @SerializedName("ome-planes")
    List<Plane> planes = new ArrayList<>();

    public static class Plane {
        @SerializedName("series")
        int series;
        @SerializedName("c")
        int channel;
        @SerializedName("z")
        int depth;
        @SerializedName("t")
        int time;
    }
}
