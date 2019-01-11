package org.hkijena.misa_imagej.utils.ui;

import ij.ImagePlus;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;

public class ImagePlusJMenuItem extends JMenuItem {
    private ImagePlus image;

    public ImagePlusJMenuItem(ImagePlus image) {
        super(image.getTitle(), UIUtils.getIconFromResources("imagej.png"));
        this.image = image;
    }

    public ImagePlus getImage() {
        return image;
    }
}
