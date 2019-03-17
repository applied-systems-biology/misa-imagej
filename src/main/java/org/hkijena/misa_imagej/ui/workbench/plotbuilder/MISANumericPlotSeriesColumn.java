package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import java.util.ArrayList;
import java.util.List;

public class MISANumericPlotSeriesColumn extends MISAPlotSeriesColumn<Double> {
    @SafeVarargs
    public MISANumericPlotSeriesColumn(List<MISAPlotSeriesData> seriesDataList, MISAPlotSeriesGenerator<Double> defaultGenerator, MISAPlotSeriesGenerator<Double>... additionalGenerators) {
        super(seriesDataList, defaultGenerator, additionalGenerators);
    }

    @Override
    protected List<Double> getValuesFromTable() {
        List<Double> result = new ArrayList<>(getSeriesData().getSize());
        for(Object value : getSeriesData().getData()) {
            if(value instanceof Number) {
                result.add(((Number) value).doubleValue());
            }
            else {
                try {
                    result.add(Double.parseDouble("" + value));
                }
                catch (NumberFormatException e) {
                    result.add(0.0);
                }
            }
        }
        return result;
    }
}
