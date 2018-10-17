package org.hkijena.misa_imagej;

import ij.ImagePlus;
import ij.WindowManager;

import java.util.ArrayList;
import java.util.Collections;

public class ImageJHelper {
    /**
     * Gets all image names
     * @return
     */
    public static String[] getImageNames() {
        String[] result = WindowManager.getImageTitles();
        if(result == null || result.length == 0)
            return new String[0];
        else
            return result;
    }

    /**
     * Returns true if the image is a Image Stack (3D image)
     * @param img
     * @return
     */
    public static boolean isImage2D(ImagePlus img) {
        return img.getImageStackSize() == 1;
    }

    /**
     * Returns true if the image is a Image Stack (3D image)
     * @param img
     * @return
     */
    public static boolean isImage3D(ImagePlus img) {
        return img.getImageStackSize() > 1 && !img.isHyperStack();
    }

    /**
     * Gets the names of all images that are not image stacks
     * @return
     */
    public static String[] getImage2DNames() {
        ArrayList<String> result = new ArrayList<>();
        result.ensureCapacity(WindowManager.getImageCount());
        for(String name : getImageNames()) {
            ImagePlus img = WindowManager.getImage(name);
            if(isImage2D(img)) {
                result.add(name);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Gets the names of all images that are not image stacks
     * @return
     */
    public static String[] getImage3DNames() {
        ArrayList<String> result = new ArrayList<>();
        result.ensureCapacity(WindowManager.getImageCount());
        for(String name : getImageNames()) {
            ImagePlus img = WindowManager.getImage(name);
            if(isImage3D(img)) {
                result.add(name);
            }
        }
        return result.toArray(new String[0]);
    }
}
