package org.hkijena.misa_imagej.api;

public interface MISAValidatable {

    /**
     * Generates a parameter schema validity report
     * @return
     */
    MISAValidityReport getValidityReport();

}
