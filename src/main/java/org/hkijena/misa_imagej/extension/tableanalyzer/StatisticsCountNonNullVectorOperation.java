package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

import java.util.Arrays;

public class StatisticsCountNonNullVectorOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        long count = Arrays.stream(input).filter(o -> o != null && !("" + o).isEmpty()).count();
        return new Object[] { count };
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
