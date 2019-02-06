package org.hkijena.misa_imagej.ui.parametereditor.datasources;

import org.hkijena.misa_imagej.api.MISADataSource;

import javax.swing.*;

public abstract class MISADataSourceUI extends JPanel {
    private MISADataSource dataSource;

    public MISADataSourceUI(MISADataSource dataSource) {
        this.dataSource = dataSource;
        initialize();
    }

    public MISADataSource getDataSource() {
        return dataSource;
    }

    protected abstract void initialize();
}
