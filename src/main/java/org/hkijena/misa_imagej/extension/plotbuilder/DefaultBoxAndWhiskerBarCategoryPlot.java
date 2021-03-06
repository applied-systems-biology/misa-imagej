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

package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

import java.util.List;

public class DefaultBoxAndWhiskerBarCategoryPlot extends DefaultBoxAndWhiskerCategoryPlot {

    public DefaultBoxAndWhiskerBarCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
        setTitle("Box Plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset() {
        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(getTitle(), getCategoryAxisLabel(), getValueAxisLabel(), (BoxAndWhiskerCategoryDataset)getDataset(), true);
        CustomBoxAndWhiskerRenderer renderer = new CustomBoxAndWhiskerRenderer();
        chart.getCategoryPlot().setRenderer(renderer);
        return chart;
    }
}
