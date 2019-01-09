package org.hkijena.misa_imagej.cache.editors;

import org.hkijena.misa_imagej.cache.MISACache;
import org.hkijena.misa_imagej.cache.MISACacheEditorUI;
import org.hkijena.misa_imagej.cache.MISADataIOType;
import org.hkijena.misa_imagej.utils.UIUtils;

import java.awt.*;

public class GenericMISACacheEditorUI extends MISACacheEditorUI {

    public GenericMISACacheEditorUI(MISACache cache) {
        super(cache);
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        if(getCache().getIOType() == MISADataIOType.Imported) {
            UIUtils.createDescriptionLabelUI(this, "No settings available for data of type '" + getCache().getCacheTypeName() + "'", 0, 0);
        }
    }
}
