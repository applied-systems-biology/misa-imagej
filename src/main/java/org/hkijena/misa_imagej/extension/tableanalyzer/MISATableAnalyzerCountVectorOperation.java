package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

import java.util.HashMap;
import java.util.Map;

public class MISATableAnalyzerCountVectorOperation implements MISATableVectorOperation {
    @Override
    public Object[] process(Object[] input) {
        Map<Object, Integer> counts = new HashMap<>();
        for(Object object : input) {
            int count = counts.getOrDefault(object, 0);
            ++count;
            counts.put(object, count);
        }
        for(int i = 0; i < input.length; ++i) {
            input[i] = counts.get(input[i]);
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
