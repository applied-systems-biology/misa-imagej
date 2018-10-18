package org.hkijena.misa_imagej.data;

import ij.ImagePlus;

import java.awt.*;

/**
 * Possible data types that can be stored in the filesystem
 */
public enum MISADataType {
    None,
    file,
    image_file,
    generic_file_stack,
    image_stack,
    json_file,
    exportable_meta_data;

    @Override
    public String toString() {
        switch(this) {
            case file:
                return "File";
            case image_file:
                return "Image";
            case image_stack:
                return "Image Stack";
            case json_file:
                return "JSON file";
            case generic_file_stack:
                return "File stack";
            case exportable_meta_data:
                return "JSON data";
            case None:
                return "Unknown";
        }
        throw new RuntimeException("Unknown data type " + this.name());
    }

    public Color toColor() {
        switch(this) {
            case file:
                return new Color(213,62,79);
            case image_file:
                return new Color(252,141,89);
            case image_stack:
                return new Color(254,224,139);
            case json_file:
                return new Color(230,245,152);
            case generic_file_stack:
                return new Color(153,213,148);
            case exportable_meta_data:
                return new Color(50,136,189);
            case None:
                return Color.BLACK;
        }
        throw new RuntimeException("Unknown data type " + this.name());
    }
}
