package org.hkijena.misa_imagej.cache;

import javax.swing.*;

public class MISACacheEditorUI extends JPanel {
    private MISACache cache;

    public MISACacheEditorUI(MISACache cache) {
        this.cache = cache;
    }

    public MISACache getCache() {
        return cache;
    }
}
