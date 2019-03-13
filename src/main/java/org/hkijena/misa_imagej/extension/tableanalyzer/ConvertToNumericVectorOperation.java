package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

public class ConvertToNumericVectorOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        for(int i = 0; i < input.length; ++i) {
            if(input[i] instanceof Number) {
            }
            else {
                input[i] = 0;
            }
        }
        return input;
    }

    @Override
    public boolean inputMatches(int inputItemCount) {
        return true;
    }

    @Override
    public int getOutputCount(int inputItemCount) {
        return inputItemCount;
    }
}
