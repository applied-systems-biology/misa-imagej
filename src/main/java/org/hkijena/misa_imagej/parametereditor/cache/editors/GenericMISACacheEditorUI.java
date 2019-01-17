package org.hkijena.misa_imagej.parametereditor.cache.editors;

import org.hkijena.misa_imagej.parametereditor.cache.MISACache;
import org.hkijena.misa_imagej.parametereditor.cache.MISACacheEditorUI;
import org.hkijena.misa_imagej.utils.UIUtils;

import java.awt.*;

public class GenericMISACacheEditorUI extends MISACacheEditorUI {

    public GenericMISACacheEditorUI(MISACache cache) {
        super(cache);
    }

    @Override
    protected void initializeImporterUI() {
        setLayout(new GridBagLayout());
        UIUtils.createDescriptionLabelUI(this, "No settings available for data of type '" + getCache().getCacheTypeName() + "'", 0, 0);
    }
}
