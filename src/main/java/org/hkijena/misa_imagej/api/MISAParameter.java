package org.hkijena.misa_imagej.api;

public interface MISAParameter {

    /**
     * Generates a parameter schema validity report
     * @return
     */
    MISAParameterValidity isValidParameter();

}
