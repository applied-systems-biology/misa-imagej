package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

public class StatisticsMaxVectorOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        double max = Double.MAX_VALUE;
        for(Object object : input) {
            if(object instanceof Number) {
                max = Math.max(max, ((Number) object).doubleValue());
            }
            else {
                max = Math.max(max, Double.parseDouble("" + object));
            }
        }
        return new Object[] { max };
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
