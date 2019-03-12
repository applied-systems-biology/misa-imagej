package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

public class MISATableAnalyzerToNumericBooleanOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        for(int i = 0; i < input.length; ++i) {
            if(input[i] instanceof Number) {
                input[i] = ((Number) input[i]).intValue() > 0 ? 1 : 0;
            }
            else if(input[i] instanceof String) {
                input[i] = input[i].toString().equalsIgnoreCase("true") ? 1 : 0;
            }
            else if(input[i] instanceof Boolean) {
                input[i] = (Boolean)input[i] ? 1 : 0;
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
