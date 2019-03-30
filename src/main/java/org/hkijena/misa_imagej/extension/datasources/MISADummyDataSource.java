package org.hkijena.misa_imagej.extension.datasources;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.MISAValidityReport;

import java.nio.file.Path;

/**
 * Data source that does nothing.
 * Used for internal purposes
 */
public class MISADummyDataSource implements MISADataSource {

    private final MISACache cache;

    public MISADummyDataSource(MISACache cache) {
        this.cache = cache;
    }

    @Override
    public void install(Path installFolder, boolean forceCopy) {

    }

    @Override
    public String getName() {
        return "Dummy";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public MISACache getCache() {
        return cache;
    }

    @Override
    public MISAValidityReport getValidityReport() {
        return new MISAValidityReport();
    }
}
