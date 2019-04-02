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

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.DefaultMISAPlotSettingsUI;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;

public class CategoryPlotSettingsUI extends DefaultMISAPlotSettingsUI {

    public CategoryPlotSettingsUI(MISAPlot plot) {
        super(plot);
        initialize();
    }

    private void initialize() {
        addStringEditorComponent("X axis label", () -> getNativePlot().getCategoryAxisLabel(), s -> getNativePlot().setCategoryAxisLabel(s));
        addStringEditorComponent("Value axis label", () -> getNativePlot().getValueAxisLabel(), s -> getNativePlot().setValueAxisLabel(s));
    }

    private CategoryPlot getNativePlot() {
        return (CategoryPlot)getPlot();
    }
}
