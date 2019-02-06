package org.hkijena.misa_imagej.api.caches;

import org.hkijena.misa_imagej.api.*;
import org.hkijena.misa_imagej.api.datasources.MISAOMETiffDataSource;

import java.util.List;

public class MISAOMETiffCache extends MISACache {

    private MISAOMETiffDataSource nativeDataSource;


    public MISAOMETiffCache (MISASample sample, MISAFilesystemEntry filesystemEntry) {
        super(sample, filesystemEntry);
        nativeDataSource = new MISAOMETiffDataSource(this);
        availableDatasources.add(nativeDataSource);
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
