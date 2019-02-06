package org.hkijena.misa_imagej.api.caches;

import org.hkijena.misa_imagej.api.*;
import org.hkijena.misa_imagej.utils.swappers.OMETiffSwapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MISAOMETiffCache extends MISACache {

    private OMETiffSwapper tiffSwapper;

    public MISAOMETiffCache (MISASample sample, MISAFilesystemEntry filesystemEntry) {
        super(sample, filesystemEntry);
    }

    @Override
    public String getCacheTypeName() {
        return "OME TIFF";
    }

    public OMETiffSwapper getTiffSwapper() {
        return tiffSwapper;
    }

    public void setTiffSwapper(OMETiffSwapper tiffSwapper) {
        this.tiffSwapper = tiffSwapper;
    }

    @Override
    public MISAParameterValidity isValidParameter() {
        if(getIOType() == MISACacheIOType.Imported) {
            if(tiffSwapper == null)
                return new MISAParameterValidity(this,
                        "Data " + getCacheTypeName() + " " + getRelativePathName(), false, "No data set. Please add data.");
            else if(!tiffSwapper.isValid())
                return new MISAParameterValidity(this,
                        "Data " + getCacheTypeName() + " " + getRelativePathName(), false, "Data is not present anymore. Did you close the image or remove the file?");
            else
                return new MISAParameterValidity(this,
                        "Data " + getCacheTypeName() + " " + getRelativePathName(), true, "");
        }
        return super.isValidParameter();
    }

    @Override
    public void install(Path installFolder, boolean forceCopy) {
        super.install(installFolder, forceCopy);

        try {
            Files.createDirectories(installFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filename;
        if(tiffSwapper.isInFilesystem())
            filename = Paths.get(tiffSwapper.getPath()).getFileName().toString();
        else if(getFilesystemEntry().name == null || getFilesystemEntry().name.isEmpty())
            filename = "image.ome.tif";
        else
            filename = getFilesystemEntry().name + ".ome.tif";

        tiffSwapper.installToFilesystem(installFolder.resolve(filename).toString(), forceCopy);
    }
}
