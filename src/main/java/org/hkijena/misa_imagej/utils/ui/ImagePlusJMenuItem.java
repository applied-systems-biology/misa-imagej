package org.hkijena.misa_imagej.utils.ui;

import ij.ImagePlus;

import javax.swing.*;

public class ImagePlusJMenuItem extends JMenuItem {
    private ImagePlus image;

    public ImagePlusJMenuItem(ImagePlus image) {
        super(image.getTitle());
        this.image = image;
    }

    public ImagePlus getImage() {
        return image;
    }
}
