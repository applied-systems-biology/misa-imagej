package org.hkijena.misa_imagej.ui.outputcaches.editors;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.ui.outputcaches.MISAOutputCacheUI;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class GenericMISAOutputCacheUI extends MISAOutputCacheUI {

    private JButton firstButton;
    private JPopupMenu additionalActionsMenu;

    public GenericMISAOutputCacheUI(MISAOutput misaOutput, MISACache cache) {
        super(misaOutput, cache);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createHorizontalGlue());

        initialize();
    }

    protected void initialize() {

        AbstractButton openFolderButton = createButton("Open folder", UIUtils.getIconFromResources("open.png"));
        openFolderButton.addActionListener(e -> {
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(getFilesystemPath().toFile());
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
        });

        AbstractButton copyPathButton = createButton("Copy path", UIUtils.getIconFromResources("copy.png"));
        copyPathButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(getFilesystemPath().toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });

    }

    protected AbstractButton createButton(String text, Icon icon) {
        if(firstButton == null) {
            firstButton = new JButton(text, icon);
            add(firstButton);
            return firstButton;
        }
        else {
            if(additionalActionsMenu == null) {
                JButton additionalActions = new JButton("...");
                additionalActions.setMaximumSize(new Dimension(1,(int)firstButton.getPreferredSize().getHeight()));
                additionalActionsMenu = UIUtils.addPopupMenuToComponent(additionalActions);
                add(additionalActions);
            }

            JMenuItem item = new JMenuItem(text, icon);
            additionalActionsMenu.add(item);
            return item;
        }
    }

}
