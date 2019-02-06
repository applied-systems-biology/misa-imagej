package org.hkijena.misa_imagej.api;

import java.nio.file.Path;

/**
 * Interface for any type of data that is an input of a cache
 */
public interface MISADataSource extends MISAParameter {
    /**
     *  Installs this cache into the install folder
     * @param installFolder
     * @param forceCopy forces copying all files into the install folder
     */
    void install(Path installFolder, boolean forceCopy);

    /**
     * Returns a descriptive name for this data source
     * @return
     */
    String getName();

    /**
     * Returns true if this data source should be editable
     * @return
     */
    boolean isEditable();
}
