package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISACacheIOType;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.ui.datasources.MISADataSourceUI;
import org.hkijena.misa_imagej.ui.datasources.MISADataSourceUIRegistry;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class MISAOutputCacheUI extends JPanel {

    private MISAOutput misaOutput;
    private MISACache cache;

    public MISAOutputCacheUI(MISAOutput misaOutput, MISACache cache) {
        this.misaOutput = misaOutput;
        this.cache = cache;
        initialize();
    }

    private void initialize() {
        updateEditorUI();
    }

    @Subscribe
    public void handleDataSourceChangeEvent(MISACache.DataSourceChangeEvent event) {
        updateEditorUI();
    }

    private void updateEditorUI() {
//        // Change data source button text
//        if(cache.getDataSource() != null)
//            selectDataSourceButton.setText(cache.getDataSource().getName());
//        else
//            selectDataSourceButton.setText(null);
//
//        if(editor != null)
//            remove(editor);
//        editor = MISADataSourceUIRegistry.getEditorFor(cache.getDataSource());
//        add(editor, BorderLayout.CENTER);
//        revalidate();
//        repaint();
    }

    public MISACache getCache() {
        return cache;
    }
}
