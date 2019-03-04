package org.hkijena.misa_imagej.extension.caches;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.MISAFilesystemEntry;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.extension.datasources.MISAOMETiffDataSource;

public class MISAOMETiffCache extends MISACache {

    private MISAOMETiffDataSource nativeDataSource;


    public MISAOMETiffCache (MISASample sample, MISAFilesystemEntry filesystemEntry) {
        super(sample, filesystemEntry);
        nativeDataSource = new MISAOMETiffDataSource(this);
        addAvailableDataSource(nativeDataSource);
    }

    @Override
    public String getCacheTypeName() {
        return "OME TIFF";
    }

    @Override
    public MISADataSource getPreferredDataSource() {
        return nativeDataSource;
    }
}
