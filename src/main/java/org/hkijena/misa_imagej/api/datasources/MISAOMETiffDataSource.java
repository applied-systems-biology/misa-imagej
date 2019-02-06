package org.hkijena.misa_imagej.api.datasources;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISACacheIOType;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.MISAParameterValidity;
import org.hkijena.misa_imagej.utils.swappers.OMETiffSwapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MISAOMETiffDataSource implements MISADataSource {

    private MISACache cache;
    private OMETiffSwapper tiffSwapper;

    public MISAOMETiffDataSource(MISACache cache) {
        this.cache = cache;
    }

    public OMETiffSwapper getTiffSwapper() {
        return tiffSwapper;
    }

    public void setTiffSwapper(OMETiffSwapper tiffSwapper) {
        this.tiffSwapper = tiffSwapper;
    }

    @Override
    public void install(Path installFolder, boolean forceCopy) {
        try {
            Files.createDirectories(installFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filename;
        if(tiffSwapper.isInFilesystem())
            filename = Paths.get(tiffSwapper.getPath()).getFileName().toString();
        else if(cache.getFilesystemEntry().name == null || cache.getFilesystemEntry().name.isEmpty())
            filename = "image.ome.tif";
        else
            filename = cache.getFilesystemEntry().name + ".ome.tif";

        tiffSwapper.installToFilesystem(installFolder.resolve(filename).toString(), forceCopy);
    }

    @Override
    public String getName() {
        return "OME TIFF";
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    public MISACache getCache() {
        return cache;
    }

    @Override
    public MISAParameterValidity isValidParameter() {
        if(tiffSwapper == null)
            return new MISAParameterValidity(this,
                    "Data " + cache.getCacheTypeName() + " " + cache.getRelativePathName(), false, "No data set. Please add data.");
        else if(!tiffSwapper.isValid())
            return new MISAParameterValidity(this,
                    "Data " + cache.getCacheTypeName() + " " + cache.getRelativePathName(), false, "Data is not present anymore. Did you close the image or remove the file?");
        else
            return new MISAParameterValidity(this,
                    "Data " + cache.getCacheTypeName() + " " + cache.getRelativePathName(), true, "");
    }
}
