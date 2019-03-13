package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

public class StatisticsMinVectorOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        double min = Double.MAX_VALUE;
        for(Object object : input) {
            if(object instanceof Number) {
                min = Math.min(min, ((Number) object).doubleValue());
            }
            else {
                min = Math.min(min, Double.parseDouble("" + object));
            }
        }
        return new Object[] { min };
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
