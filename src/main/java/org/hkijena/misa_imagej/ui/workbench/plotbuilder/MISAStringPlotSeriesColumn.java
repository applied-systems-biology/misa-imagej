package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import java.util.ArrayList;
import java.util.List;

public class MISAStringPlotSeriesColumn extends MISAPlotSeriesColumn<String> {

    @SafeVarargs
    public MISAStringPlotSeriesColumn(List<MISAPlotSeriesData> seriesDataList, MISAPlotSeriesGenerator<String> defaultGenerator, MISAPlotSeriesGenerator<String>... additionalGenerators) {
        super(seriesDataList, defaultGenerator, additionalGenerators);
    }

    @Override
    protected List<String> getValuesFromTable() {
        List<String> result = new ArrayList<>(getSeriesData().getSize());
        for(Object value : getSeriesData().getData()) {
            result.add("" + value);
        }
        return result;
    }
}
