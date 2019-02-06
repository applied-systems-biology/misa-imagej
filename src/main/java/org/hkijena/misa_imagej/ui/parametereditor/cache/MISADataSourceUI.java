package org.hkijena.misa_imagej.ui.parametereditor.cache;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISACacheIOType;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.ui.parametereditor.SampleDataEditorUI;

import javax.swing.*;

public abstract class MISADataSourceUI extends JPanel {
    private MISADataSource cache;

    public MISADataSourceUI(MISADataSource cache) {
        this.cache = cache;
    }

    public MISADataSource getCache() {
        return cache;
    }

    public void populate(SampleDataEditorUI ui) {
        if(getCache().getIOType() == MISACacheIOType.Imported) {
            initializeImporterUI();
        }
        ui.insertCacheEditorUI(this);
    }

    protected abstract void initializeImporterUI();
}
