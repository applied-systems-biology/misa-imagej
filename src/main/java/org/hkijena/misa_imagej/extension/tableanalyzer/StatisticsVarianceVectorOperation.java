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
