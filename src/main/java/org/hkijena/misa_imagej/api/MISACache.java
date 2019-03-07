package org.hkijena.misa_imagej.api;


import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.extension.datasources.MISAFolderLinkDataSource;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MISACache implements MISAValidatable {

    private MISASample sample;

    /**
     * Relative path within the imported or exported filesystem
     * This does not include "imported" or "exported"
     */
    private MISAFilesystemEntry filesystemEntry;

    private MISADataSource dataSource;

    private List<MISADataSource> availableDatasources = new ArrayList<>();

    private EventBus eventBus = new EventBus();

    /**
     * List of attachments TODO
     */
//    private Map<MISAAttachmentLocation, MISAAttachment> attachments = new HashMap<>();


    public MISACache(MISASample sample, MISAFilesystemEntry filesystemEntry) {
        this.sample = sample;
        this.filesystemEntry = filesystemEntry;

        // Default data sources that are always available
        this.availableDatasources.add(new MISAFolderLinkDataSource(this));
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
     * Relative path including the IO mode
     * @return
     */
    public String getFullRelativePath() {
        if(getIOType() == MISACacheIOType.Imported)
            return "imported/" + getRelativePath();
        else if(getIOType() == MISACacheIOType.Exported)
            return "exported/" + getRelativePath();
        else
            throw new UnsupportedOperationException();
    }

    /**
     * Returns the serialization ID of the pattern if available
     * Otherwise return null
     * @return
     */
    public String getPatternSerializationID() {
        if(getFilesystemEntry().metadata.hasPropertyFromPath("pattern")) {
            return getFilesystemEntry().metadata.getPropertyFromPath("pattern").getSerializationId();
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
            return getFilesystemEntry().metadata.getPropertyFromPath("description").getSerializationId();
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
        if(getFilesystemEntry().metadata.hasPropertyFromPath("description")) {
            String name = getFilesystemEntry().metadata.getPropertyFromPath("description").getDocumentationTitle();
            if(name != null && !name.isEmpty())
                return name;
        }
        return getPatternSerializationID() + " -> " + getDescriptionSerializationID();
    }

    /**
     * Returns the documentation for this cache
     * @return
     */
    public String getCacheDocumentation() {
        if(getFilesystemEntry().metadata.hasPropertyFromPath("description")) {
           return getFilesystemEntry().metadata.getPropertyFromPath("description").getDocumentationDescription();
        }
        return null;
    }

    /**
     * Returns the documentation for the pattern of this cache
     * @return
     */
    public String getCachePatternTypeName() {
        if(getFilesystemEntry().metadata.hasPropertyFromPath("pattern")) {
            return getFilesystemEntry().metadata.getPropertyFromPath("pattern").getDocumentationTitle();
        }
        return null;
    }

    /**
     * Returns the documentation for the pattern of this cache
     * @return
     */
    public String getCachePatternDocumentation() {
        if(getFilesystemEntry().metadata.hasPropertyFromPath("pattern")) {
            return getFilesystemEntry().metadata.getPropertyFromPath("pattern").getDocumentationDescription();
        }
        return null;
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
    public MISAValidityReport getValidityReport() {
        if(getIOType() == MISACacheIOType.Exported)
            return new MISAValidityReport(this, null, true, "");
        else if(getDataSource() == null) {
            return new MISAValidityReport(this, "Data " + getCacheTypeName() + " " + getRelativePathName(), false, "No data source was set!");
        }
        else {
            return getDataSource().getValidityReport();
        }
    }

    /**
     * Installs this cache into the install folder
     * this is only valid for imported caches
     * @param installFolder
     * @param forceCopy forces copying all files into the install folder
     */
    public void install(Path installFolder, boolean forceCopy) {
        if(getIOType() == MISACacheIOType.Imported) {
            getDataSource().install(installFolder, forceCopy);
        }
    }

    /**
     * Gets a data source by type
     * @param klass
     * @param <T>
     * @return
     */
    public <T extends MISADataSource> T getDataSourceByType(Class<T> klass) {
        return (T)getAvailableDataSources().stream().filter(klass::isInstance).findFirst().get();
    }

    /**
     * Gets all data sources that have the speciefied type
     * @param klass
     * @param <T>
     * @return
     */
    public <T extends MISADataSource> List<T> getDataSourcesByType(Class<T> klass) {
        List<T> result = new ArrayList<>();
        getAvailableDataSources().stream().filter(klass::isInstance).forEach(misaDataSource -> {
            result.add((T)misaDataSource);
        });
        return result;
    }

    /**
     * Returns a list of additional data sources that are available for this cache
     * @return
     */
    public List<MISADataSource> getAvailableDataSources() {
        return Collections.unmodifiableList(availableDatasources);
    }

    /**
     * Adds a new data source to the list of available ones
     * @param source
     */
    public void addAvailableDataSource(MISADataSource source) {
        availableDatasources.add(source);
    }

    /**
     * Removes a data source from the list of available sources
     * @param source
     */
    public void removeAvailableDataSource(MISADataSource source) {
        availableDatasources.remove(source);
        if(source == dataSource) {
            if(getPreferredDataSource() != null)
                setDataSource(getPreferredDataSource());
            else if(availableDatasources.size() > 0)
                setDataSource(availableDatasources.get(0));
            else
                setDataSource(null);
        }
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
        getEventBus().post(new DataSourceChangeEvent(this, this.dataSource));
    }

    /**
     * A data source is responsible for providing the data of the cache
     */
    public MISADataSource getDataSource() {
        return dataSource;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static class DataSourceChangeEvent {
        private MISACache cache;
        private MISADataSource dataSource;

        public DataSourceChangeEvent(MISACache cache, MISADataSource dataSource) {
            this.cache = cache;
            this.dataSource = dataSource;
        }

        public MISACache getCache() {
            return cache;
        }

        public MISADataSource getDataSource() {
            return dataSource;
        }
    }
}
