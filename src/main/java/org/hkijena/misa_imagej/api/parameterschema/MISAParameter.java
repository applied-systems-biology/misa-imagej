package org.hkijena.misa_imagej.api.parameterschema;

public interface MISAParameter {

    /**
     * Generates a parameter schema validity report
     * @return
     */
    MISAParameterValidity isValidParameter();

}
