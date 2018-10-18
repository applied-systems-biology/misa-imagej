package org.hkijena.misa_imagej.data;

/**
 * Action to be taken with output data after the calculation is finished.
 */
public enum MISAExportedDataAction {
    Nothing,
    Import;

    @Override
    public String toString() {
        switch(this) {
            case Nothing:
                return "Do nothing";
            case Import:
                return "Import into ImageJ";
        }
        throw new RuntimeException("Unsupported value " + this.name());
    }
}
