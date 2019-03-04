package org.hkijena.misa_imagej.ui.datasources;

import org.hkijena.misa_imagej.api.MISADataSource;

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
