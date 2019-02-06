package org.hkijena.misa_imagej.api.caches;

import org.hkijena.misa_imagej.api.*;
import org.hkijena.misa_imagej.api.datasources.MISAOMETiffDataSource;
import org.hkijena.misa_imagej.utils.swappers.OMETiffSwapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MISAOMETiffCache extends MISACache {


    public MISAOMETiffCache (MISASample sample, MISAFilesystemEntry filesystemEntry) {
        super(sample, filesystemEntry);
    }

    @Override
    public String getCacheTypeName() {
        return "OME TIFF";
    }

    @Override
    public List<MISADataSource> getAdditionalDataSources() {
        List<MISADataSource> result = super.getAdditionalDataSources();
        result.add(new MISAOMETiffDataSource(this));
        return result;
    }
}
