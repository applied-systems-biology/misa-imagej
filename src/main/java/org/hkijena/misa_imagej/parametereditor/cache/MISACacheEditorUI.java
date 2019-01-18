package org.hkijena.misa_imagej.parametereditor.cache;

import org.hkijena.misa_imagej.parametereditor.SampleDataEditorUI;

import javax.swing.*;

public abstract class MISACacheEditorUI extends JPanel {
    private MISACache cache;

    public MISACacheEditorUI(MISACache cache) {
        this.cache = cache;
    }

    public MISACache getCache() {
        return cache;
    }

    public void populate(SampleDataEditorUI ui) {
        if(getCache().getIOType() == MISADataIOType.Imported) {
            initializeImporterUI();
        }
        ui.insertCacheEditorUI(this);
    }

    protected abstract void initializeImporterUI();
}
