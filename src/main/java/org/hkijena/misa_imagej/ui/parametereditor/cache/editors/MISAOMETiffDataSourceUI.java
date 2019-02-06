package org.hkijena.misa_imagej.ui.parametereditor.cache.editors;

import ij.ImagePlus;
import ij.WindowManager;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.ui.parametereditor.cache.MISADataSourceUI;
import org.hkijena.misa_imagej.api.caches.MISAOMETiffCache;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.swappers.OMETiffSwapper;
import org.hkijena.misa_imagej.utils.ui.ImagePlusJMenuItem;

import javax.swing.*;

/**
 * Editor for OME Tiff caches
 */
public class MISAOMETiffDataSourceUI extends MISADataSourceUI {

    private MISAOMETiffCache cache;
    private JTextField display;
    private JButton optionButton;

    public MISAOMETiffDataSourceUI(MISACache cache) {
        super(cache);
        this.cache = (MISAOMETiffCache)cache;
        refreshDisplay();
    }

    @Override
    protected void initializeImporterUI() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        display = new JTextField();
        display.setEditable(false);
        add(display);

        JButton selectButton = new JButton(UIUtils.getIconFromResources("open.png"));
        createImportPopup(selectButton);
        add(selectButton);

        optionButton = new JButton(UIUtils.getIconFromResources("edit.png"));
        createEditPopup(optionButton);
        add(optionButton);

        refreshDisplay();
    }

    /**
     * Creates a popup menu that allows selecting data from filesystem or from ImageJ
     * @param selectButton
     */
    private void createImportPopup(JButton selectButton) {
        JPopupMenu selectOptions = UIUtils.addPopupMenuToComponent(selectButton);
        boolean hasImageJData = false;
        for(int i = 1; i <= WindowManager.getImageCount(); ++i) {
            final ImagePlus image = WindowManager.getImage(WindowManager.getNthImageID(i));
            ImagePlusJMenuItem item = new ImagePlusJMenuItem(image);
            item.addActionListener(actionEvent -> {
                cache.setTiffSwapper(new OMETiffSwapper(image, null));
                refreshDisplay();
            });
            selectOptions.add(item);
            hasImageJData = true;
        }
        if(hasImageJData) {
            selectOptions.addSeparator();
        }

        // Allow selection from filesystem
        JMenuItem selectExternal = new JMenuItem("From filesystem ...", UIUtils.getIconFromResources("import.png"));
        selectExternal.addActionListener(actionEvent -> {
//            java.awt.FileDialog dialog = new FileDialog((JFrame)SwingUtilities.getWindowAncestor(this), "Open image", FileDialog.LOAD);
//            dialog.setMultipleMode(false);
//            dialog.setFile("*.ome.tif;*.ome.tiff");
//            dialog.setAutoRequestFocus(true);
//            dialog.setVisible(true);
//            if(dialog.getFiles().length > 0) {
//                cache.setTiffSwapper(new OMETiffSwapper(null, dialog.getFiles()[0].getAbsolutePath()));
//                refreshDisplay();
//            }
            JFileChooser chooser = new JFileChooser();
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                cache.setTiffSwapper(new OMETiffSwapper(null, chooser.getSelectedFile().getAbsolutePath()));
                refreshDisplay();
            }
        });
        selectOptions.add(selectExternal);

        // Allow refresh of menu
        JMenuItem refresh = new JMenuItem("Refresh list", UIUtils.getIconFromResources("refresh.png"));
        refresh.addActionListener(actionEvent -> createImportPopup(selectButton));
        selectOptions.add(refresh);
    }

    private void createEditPopup(JButton selectButton) {
        JPopupMenu selectOptions = UIUtils.addPopupMenuToComponent(selectButton);

        if(cache == null || display == null)
            return;
        if(cache.getTiffSwapper() != null) {
            JMenuItem clearItem = new JMenuItem("Clear data", UIUtils.getIconFromResources("delete.png"));
            clearItem.addActionListener(actionEvent -> {
                cache.setTiffSwapper(null);
                refreshDisplay();
            });
            selectOptions.add(clearItem);

            if(!cache.getTiffSwapper().isInImageJ() && cache.getTiffSwapper().isInFilesystem()) {
                JMenuItem importItem = new JMenuItem("Import into ImageJ", UIUtils.getIconFromResources("import.png"));
                importItem.addActionListener(actionEvent -> {
                    cache.getTiffSwapper().importIntoImageJ(null); // BioFormats decides by itself
                    refreshDisplay();
                });
                selectOptions.add(importItem);
            }

            if(cache.getTiffSwapper().isInImageJ()) {
                JMenuItem selectItem = new JMenuItem("Select in ImageJ", UIUtils.getIconFromResources("target.png"));
                selectItem.addActionListener(actionEvent -> {
                    cache.getTiffSwapper().editInImageJ();
                });
                selectOptions.add(selectItem);
            }
        }
    }

    private void refreshDisplay() {
        if(cache == null || display == null)
            return;
        if(cache.getTiffSwapper() != null)
            display.setText(cache.getTiffSwapper().toString());
        else
            display.setText("<No data set>");
        createEditPopup(optionButton);
    }
}
