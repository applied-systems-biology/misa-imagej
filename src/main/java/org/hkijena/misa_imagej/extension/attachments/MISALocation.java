package org.hkijena.misa_imagej.extension.attachments;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISASerializable;

import java.nio.file.Path;

public class MISALocation extends MISASerializable {
    @SerializedName("filesystem-location")
    public Path filesystemLocation;
    @SerializedName("filesystem-unique-location")
    public Path filesystemUniqueLocation;
}
