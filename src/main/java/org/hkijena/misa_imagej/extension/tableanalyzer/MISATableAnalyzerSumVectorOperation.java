package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

public class MISATableAnalyzerSumVectorOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        double sum = 0;
        for(Object object : input) {
            if(object instanceof Number) {
                sum += ((Number) object).doubleValue();
            }
            else {
                sum += Double.parseDouble("" + object);
            }
        }
        return new Object[] { sum };
    }

    @Override
    public boolean inputMatches(int inputItemCount) {
        return true;
    }

    @Override
    public int getOutputCount(int inputItemCount) {
        return 1;
    }
}
