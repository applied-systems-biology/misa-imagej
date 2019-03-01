package org.hkijena.misa_imagej.ui.outputcaches.editors;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;

import javax.swing.*;

public class ImageOutputCacheUI extends GenericMISAOutputCacheUI {
    public ImageOutputCacheUI(MISAOutput misaOutput, MISACache cache) {
        super(misaOutput, cache);
    }

    @Override
    protected void initialize(boolean silent) {
        super.initialize(true);

    }
}
