package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

public interface MISATableVectorOperation {
    Object[] process(Object[] input);
    boolean inputMatches(int inputItemCount);
    int getOutputCount(int inputItemCount);
}
