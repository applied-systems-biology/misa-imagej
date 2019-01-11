package org.hkijena.misa_imagej.parametereditor.cache;


import org.hkijena.misa_imagej.parametereditor.MISAFilesystemEntry;
import org.hkijena.misa_imagej.parametereditor.ParameterSchemaValidityReport;
import org.hkijena.misa_imagej.parametereditor.ParameterSchemaValue;

import java.awt.*;
import java.nio.file.Path;

public class MISACache implements ParameterSchemaValue {
    /**
     * Relative path within the imported or exported filesystem
     * This does not include "imported" or "exported"
     */
    private MISAFilesystemEntry filesystemEntry;

    public MISACache(MISAFilesystemEntry filesystemEntry) {
        this.filesystemEntry = filesystemEntry;
    }

    /**
     * Returns the filesystem entry this cache is attached to
     * @return
     */
    public MISAFilesystemEntry getFilesystemEntry() {
        return filesystemEntry;
    }

    /**
     * Returns a non-empty string that describes the internal path within the filesystem
     * @return
     */
    public String getRelativePathName() {
        if(getFilesystemEntry().getInternalPath().toString().isEmpty())
            return "<Root>";
        else
            return getFilesystemEntry().getInternalPath().toString();
    }

    /**
     * Returns the serialization ID of the pattern if available
     * Otherwise return null
     * @return
     */
    public String getPatternSerializationID() {
        if(getFilesystemEntry().metadata.hasPropertyFromPath("pattern")) {
            return getFilesystemEntry().metadata.getPropertyFromPath("pattern").serializationId;
        }
        return null;
    }

    /**
     * Returns the serialization ID of the description if available
     * Otherwise return null
     * @return
     */
    public String getDescriptionSerializationID() {
        if(getFilesystemEntry().metadata.hasPropertyFromPath("description")) {
            return getFilesystemEntry().metadata.getPropertyFromPath("description").serializationId;
        }
        return null;
    }

    /**
     * Returns the IO type of this cache
     * @return
     */
    public MISADataIOType getIOType() {
        return getFilesystemEntry().ioType;
    }

    /**
     * Returns true if this cache has a pattern or description
     * @return
     */
    public boolean isValid() {
        return getPatternSerializationID() != null || getDescriptionSerializationID() != null;
    }

    /**
     * Returns the name of this cache
     * @return
     */
    public String getCacheTypeName() {
        return getPatternSerializationID() + " -> " + getDescriptionSerializationID();
    }

    @Override
    public String toString() {
        return getPatternSerializationID() + "|" + getDescriptionSerializationID() + " @ " + getFilesystemEntry().toString();
    }

    /**
     * Automatically generates a color from the name
     * @return
     */
    public Color toColor() {
        float h = Math.abs(getCacheTypeName().hashCode() % 256) / 255.0f;
        return Color.getHSBColor(h, 0.5f, 1);
    }

    @Override
    public ParameterSchemaValidityReport isValidParameter() {
        if(getIOType() == MISADataIOType.Exported)
            return new ParameterSchemaValidityReport(this, null, true, "");
        else
            return new ParameterSchemaValidityReport(this, null, false, "The data type '" + getCacheTypeName() + "' is not supported by MISA++ for ImageJ.");
    }

    /**
     * Installs this cache into the install folder
     * this is only valid for imported caches
     * @param installFolder
     * @param forceCopy forces copying all files into the install folder
     */
    public void install(Path installFolder, boolean forceCopy) {

    }
}