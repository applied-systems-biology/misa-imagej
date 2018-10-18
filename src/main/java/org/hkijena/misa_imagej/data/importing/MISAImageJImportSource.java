package org.hkijena.misa_imagej.data.importing;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import net.imagej.Dataset;
import org.hkijena.misa_imagej.data.MISAImportedData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Imports data from a window
 */
public class MISAImageJImportSource implements MISADataImportSource {
    private MISAImportedData importedData;
    private ImagePlus image;

    public MISAImageJImportSource(MISAImportedData importedData, ImagePlus image) {
        this.importedData = importedData;
        this.image = image;
    }

    @Override
    public String toString() {
        return "[ImageJ] " + image.getTitle();
    }

    @Override
    public void runImport(Path importedDirectory, boolean forcedCopy) {
        Path fullpath = importedDirectory.resolve(importedData.getRelativePath());
        try {
            Files.createDirectories(fullpath.getParent());
            IJ.saveAsTiff(image, fullpath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MISAImportedData getData() {
        return importedData;
    }

    /**
     * Returns true if the ImagePlus is compatible with the imported data
     * @param importedData
     * @param image
     * @return
     */
    public static boolean canHold(MISAImportedData importedData, ImagePlus image) {
        if(image == null)
            return false;
        switch(importedData.getType()) {
            case image_file: {
                return !image.isHyperStack() && image.getNSlices() == 1;
            }
            case image_stack: {
                return !image.isHyperStack() && image.getNSlices() >= 1;
            }
            default:
                return false;
        }
    }
}
