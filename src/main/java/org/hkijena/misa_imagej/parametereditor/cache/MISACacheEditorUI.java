package org.hkijena.misa_imagej.parametereditor.cache;

import javax.swing.*;

public abstract class MISACacheEditorUI extends JPanel {
    private MISACache cache;

    public MISACacheEditorUI(MISACache cache) {
        this.cache = cache;
        if(cache.getIOType() == MISADataIOType.Imported) {
            initializeImporterUI();
        }
    }

    public MISACache getCache() {
        return cache;
    }

    protected abstract void initializeImporterUI();
}
