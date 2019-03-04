package org.hkijena.misa_imagej.extension.attachments;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISASerializable;

public class MISALocatable extends MISASerializable {

    @SerializedName("location")
    public MISALocation location = null;

}
