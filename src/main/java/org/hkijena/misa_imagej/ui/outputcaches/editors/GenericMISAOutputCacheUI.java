package org.hkijena.misa_imagej.ui.outputcaches.editors;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.ui.outputcaches.MISAOutputCacheUI;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

public class GenericMISAOutputCacheUI extends MISAOutputCacheUI {

    public GenericMISAOutputCacheUI(MISAOutput misaOutput, MISACache cache) {
        super(misaOutput, cache);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createHorizontalGlue());

        initialize(false);
    }

    protected JButton createButton(String text, Icon icon, boolean silent) {
        if(silent)
            return new JButton(icon) {
                {
                    setToolTipText(text);
                }
            };
        else
            return new JButton(text, icon);
    }

    protected void initialize(boolean silent) {
        JButton copyPathButton = createButton("Copy path", UIUtils.getIconFromResources("copy.png"), silent);
        copyPathButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(getFilesystemPath().toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        add(copyPathButton);

        JButton openFolderButton = createButton("Open folder", UIUtils.getIconFromResources("open.png"), silent);
        openFolderButton.addActionListener(e -> {
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(getFilesystemPath().toFile());
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
        add(openFolderButton);
    }
}
