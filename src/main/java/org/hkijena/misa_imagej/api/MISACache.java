package org.hkijena.misa_imagej.api;


import org.hkijena.misa_imagej.api.datasources.MISAFolderLinkDataSource;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class MISACache implements MISAParameter {

    private MISASample sample;

    /**
     * Relative path within the imported or exported filesystem
     * This does not include "imported" or "exported"
     */
    private MISAFilesystemEntry filesystemEntry;

    private MISADataSource dataSource;

    protected List<MISADataSource> availableDatasources = new ArrayList<>();

    private PropertyChangeSupport propertyChangeSupport;

    /**
     * List of attachments
     */
    private Map<MISAAttachmentLocation, MISAAttachment> attachments = new HashMap<>();


    public MISACache(MISASample sample, MISAFilesystemEntry filesystemEntry) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.sample = sample;
        this.filesystemEntry = filesystemEntry;

        // Default data sources that are always available
        this.availableDatasources.add(new MISAFolderLinkDataSource());
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
     * Returns a string that describes the internal path within the filesystem
     * @return
     */
    public String getRelativePath() {
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
    public MISACacheIOType getIOType() {
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
    public MISAParameterValidity isValidParameter() {
        if(getIOType() == MISACacheIOType.Exported)
            return new MISAParameterValidity(this, null, true, "");
        else if(getDataSource() == null) {
            return new MISAParameterValidity(this, "Data " + getCacheTypeName() + " " + getRelativePathName(), false, "No data source was set!");
        }
        else {
            return getDataSource().isValidParameter();
        }
    }

    /**
     * Installs this cache into the install folder
     * this is only valid for imported caches
     * @param installFolder
     * @param forceCopy forces copying all files into the install folder
     */
    public void install(Path installFolder, boolean forceCopy) {
        getDataSource().install(installFolder, forceCopy);
    }

    /**
     * Gets the map of attachments.
     * This is only used when output is analyzed
     * @return
     */
    public Map<MISAAttachmentLocation, MISAAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Returns a list of additional data sources that are recommended by this cache
     * @return
     */
    public List<MISADataSource> getAvailableDataSources() {
        return Collections.unmodifiableList(availableDatasources);
    }

    /**
     * Returns a preferred data source or null
     * @return
     */
    public MISADataSource getPreferredDataSource() {
        return null;
    }

    /**
     * Returns the sample that this cache belongs to
     * @return
     */
    public MISASample getSample() {
        return sample;
    }

    public void setDataSource(MISADataSource dataSource) {
        this.dataSource = dataSource;
        propertyChangeSupport.firePropertyChange("dataSource", null, null);
    }

    /**
     * A data source is responsible for providing the data of the cache
     */
    public MISADataSource getDataSource() {
        return dataSource;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
