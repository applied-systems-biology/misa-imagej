package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

import java.util.Arrays;

public class StatisticsMedianVectorOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        double[] numbers = new double[input.length];
        for(int i = 0; i < input.length; ++i) {
            if(input[i] instanceof Number) {
                numbers[i] = ((Number) input[i]).doubleValue();
            }
            else {
                numbers[i] = Double.parseDouble("" + input[i]);
            }
        }
        Arrays.sort(numbers);
        if(numbers.length % 2 == 0) {
            double floor = numbers[numbers.length / 2 - 1];
            double ceil = numbers[numbers.length / 2];
            return new Object[] { (floor + ceil) / 2.0 };
        }
        else {
            return new Object[] { numbers[numbers.length / 2] };
        }
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
