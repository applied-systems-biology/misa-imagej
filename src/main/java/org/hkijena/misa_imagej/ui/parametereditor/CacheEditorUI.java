package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.ui.parametereditor.cache.MISADataSourceUIRegistry;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Editor that allows setting up the imported filesystem
 */
public class CacheEditorUI extends JPanel {
    private String title;
    private List<MISACache> caches;

    public CacheEditorUI(String title, List<MISACache> caches) {
        this.title = title;
        this.caches = caches;
        initialize();
    }

    private void addEditor(MISACache cache, int row) {
        UIUtils.backgroundColorJLabel(
                UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this,
                        cache.getCacheTypeName(), row, 0)),
                        cache.toColor());
        UIUtils.borderedJLabel(UIUtils.createDescriptionLabelUI(this, "/" + cache.getFilesystemEntry().getInternalPath().toString(), row, 1));
        add(MISADataSourceUIRegistry.getEditorFor(cache), new GridBagConstraints() {
            {
                gridx = 2;
                gridy = row;
                gridwidth = 2;
                anchor = GridBagConstraints.PAGE_START;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
            }
        });
    }

    private void initialize() {
        setBorder(BorderFactory.createTitledBorder(title));
        setLayout(new GridBagLayout());

        int row = 0;
        for(MISACache cache : caches) {
            addEditor(cache, row++);
        }
    }
}
