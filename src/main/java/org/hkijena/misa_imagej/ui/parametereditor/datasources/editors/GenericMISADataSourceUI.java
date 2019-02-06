package org.hkijena.misa_imagej.ui.parametereditor.datasources.editors;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.MISADataSourceUI;
import org.hkijena.misa_imagej.utils.UIUtils;

import java.awt.*;

public class GenericMISADataSourceUI extends MISADataSourceUI {

    public GenericMISADataSourceUI(MISADataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void initialize() {
        setLayout(new GridBagLayout());
//        UIUtils.createDescriptionLabelUI(this, "No settings available for data of type '" + getDataSource().getCacheTypeName() + "'", 0, 0);
    }
}
