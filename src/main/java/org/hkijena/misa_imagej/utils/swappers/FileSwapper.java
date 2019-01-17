package org.hkijena.misa_imagej.utils.swappers;

/**
 * Interface for all data that can be swapped between ImageJ and filesystem
 */
public interface FileSwapper {
    /**
     * Gets the ID of this data within ImageJ (e.g. window name)
     * @return
     */
    Object getImageJObject();

    /**
     * Gets the file path of the data
     * @return
     */
    String getPath();

    /**
     * Returns true if the data is present in ImageJ
     * @return
     */
    boolean isInImageJ();

    /**
     * Returns true if the data is present in the physical filesystem
     * @return
     */
    boolean isInFilesystem();

    /**
     * Returns true if the data is still available in any form
     * @return
     */
    boolean isValid();

    /**
     * Imports the data into imageJ.
     * requires that the data is present in the path
     * If the data is already present within ImageJ, the data is duplicated
     * @param id Preferred ID. This might not always work
     */
    void importIntoImageJ(String id);

    /**
     * Exports data to the filesystem
     * If the data is already present as file, the current path will be updated (files will not be deleted)
     * @param path The export path
     */
    void exportToFilesystem(String path);

    /**
     * Exports to the filesystem without changing the current path
     * By default, less intrusive installation is preferred (e.g. creating symlinks instead of copying)
     * @param path
     * @param forceCopy Disables symlinking and other ways to optimize installation
     */
    void installToFilesystem(String path, boolean forceCopy);

}
