/*
 * Copyright by Ruman Gerst
 * Research Group Applied Systems Biology - Head: Prof. Dr. Marc Thilo Figge
 * https://www.leibniz-hki.de/en/applied-systems-biology.html
 * HKI-Center for Systems Biology of Infection
 * Leibniz Institute for Natural Product Research and Infection Biology - Hans Knöll Insitute (HKI)
 * Adolf-Reichwein-Straße 23, 07745 Jena, Germany
 *
 * This code is licensed under BSD 2-Clause
 * See the LICENSE file provided with this code for the full license.
 */

package org.hkijena.misa_imagej.extension.tableanalyzer;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;

import java.util.HashMap;
import java.util.Map;

public class ConvertToOccurrencesVectorOperation implements MISATableVectorOperation {
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
