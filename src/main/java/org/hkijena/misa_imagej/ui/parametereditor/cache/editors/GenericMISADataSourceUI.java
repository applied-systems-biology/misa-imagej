package org.hkijena.misa_imagej.ui.parametereditor.cache.editors;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.ui.parametereditor.cache.MISADataSourceUI;
import org.hkijena.misa_imagej.utils.UIUtils;

import java.awt.*;

public class GenericMISADataSourceUI extends MISADataSourceUI {

    public GenericMISADataSourceUI(MISACache cache) {
        super(cache);
    }

    @Override
    protected void initializeImporterUI() {
        setLayout(new GridBagLayout());
        UIUtils.createDescriptionLabelUI(this, "No settings available for data of type '" + getCache().getCacheTypeName() + "'", 0, 0);
    }
}
