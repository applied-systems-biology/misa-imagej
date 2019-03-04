package org.hkijena.misa_imagej.extension.outputcaches;

import ij.WindowManager;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;

public class ImageOutputCacheUI extends GenericMISAOutputCacheUI {
    public ImageOutputCacheUI(MISAOutput misaOutput, MISACache cache) {
        super(misaOutput, cache);
    }

    @Override
    protected void initialize() {

        AbstractButton renameCurrentImageButton = createButton("Set current image name", UIUtils.getIconFromResources("imagej.png"));
        renameCurrentImageButton.addActionListener(e -> renameCurrentImage());

        super.initialize();
    }

    private void renameCurrentImage() {
        if(WindowManager.getCurrentImage() != null) {
            WindowManager.getCurrentImage().setTitle(getCache().getSample().getName() + "/" + getCache().getRelativePathName());
        }
    }
}
