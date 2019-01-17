package org.hkijena.misa_imagej.parametereditor;

public interface ParameterSchemaValue {

    /**
     * Generates a parameter schema validity report
     * @return
     */
    ParameterSchemaValidityReport isValidParameter();

}
