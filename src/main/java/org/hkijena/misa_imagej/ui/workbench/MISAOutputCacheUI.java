package org.hkijena.misa_imagej.ui.workbench;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;

import javax.swing.*;
import java.nio.file.Path;

public class MISAOutputCacheUI extends JPanel {

    private MISAOutput misaOutput;
    private MISACache cache;

    public MISAOutputCacheUI(MISAOutput misaOutput, MISACache cache) {
        this.misaOutput = misaOutput;
        this.cache = cache;
    }

    public MISAOutput getMisaOutput() {
        return misaOutput;
    }

    public MISACache getCache() {
        return cache;
    }

    public Path getFilesystemPath() {
        return misaOutput.getRootPath().resolve(cache.getSample().getName()).resolve(cache.getRelativePath());
    }
}
