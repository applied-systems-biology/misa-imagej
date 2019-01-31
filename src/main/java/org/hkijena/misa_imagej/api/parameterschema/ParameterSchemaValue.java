package org.hkijena.misa_imagej.api.parameterschema;

public interface ParameterSchemaValue {

    /**
     * Generates a parameter schema validity report
     * @return
     */
    ParameterSchemaValidityReport isValidParameter();

}
