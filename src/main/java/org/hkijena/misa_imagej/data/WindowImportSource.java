package org.hkijena.misa_imagej.data;

import ij.WindowManager;

import java.nio.file.Path;

/**
 * Imports data from a window
 */
public class WindowImportSource implements MISADataImportSource {
    private MISAImportedData importedData;
    private String windowName;

    public WindowImportSource(MISAImportedData importedData, String window) {
        this.importedData = importedData;
        windowName = window;
        if(WindowManager.getWindow(window) == null)
            throw new RuntimeException("Window " + window + " does not exist!");
    }

    @Override
    public String toString() {
        return "[ImageJ] " + windowName;
    }

    @Override
    public void runImport(Path importedDirectory, boolean forcedCopy) {
        // TODO:
    }

    @Override
    public MISAImportedData getData() {
        return importedData;
    }
}
