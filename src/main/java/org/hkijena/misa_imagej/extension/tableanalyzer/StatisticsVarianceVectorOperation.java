package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

public class StatisticsVarianceVectorOperation implements MISATableVectorOperation {

    @Override
    public Object[] process(Object[] input) {
        double sumSquared = 0;
        double sum = 0;
        for (Object object : input) {
            if (object instanceof Number) {
                sumSquared += Math.pow(((Number) object).doubleValue(), 2);
                sum += ((Number) object).doubleValue();
            } else {
                sumSquared += Math.pow(Double.parseDouble("" + object), 2);
                sum += Double.parseDouble("" + object);
            }
        }
        return new Object[]{(sumSquared / input.length) - Math.pow(sum / input.length, 2)};
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
