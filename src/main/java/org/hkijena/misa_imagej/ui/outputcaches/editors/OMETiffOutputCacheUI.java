package org.hkijena.misa_imagej.ui.outputcaches.editors;

import ij.IJ;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class OMETiffOutputCacheUI extends ImageOutputCacheUI {
    public OMETiffOutputCacheUI(MISAOutput misaOutput, MISACache cache) {
        super(misaOutput, cache);
    }

    @Override
    protected void initialize(boolean silent) {
        JButton importBioformatsButton = createButton("Bioformats import", UIUtils.getIconFromResources("bioformats.png"), silent);
        importBioformatsButton.addActionListener(e -> importBioformats());
        add(importBioformatsButton);

        super.initialize(true);
    }

    private void importBioformats() {
        try {
            Optional<Path> file = Files.list(getFilesystemPath()).filter(path -> path.toString().endsWith(".ome.tiff") || path.toString().endsWith(".ome.tif")).findFirst();
            if(!file.isPresent())
                throw new IOException("Could not find a compatible file!");

            IJ.run("Bio-Formats Importer", "open='" + file.get().toString() +
                    "' autoscale color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}