package org.hkijena.misa_imagej.ui.parametereditor.datasources.editors;

import ij.ImagePlus;
import ij.WindowManager;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.datasources.MISAOMETiffDataSource;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.MISADataSourceUI;
import org.hkijena.misa_imagej.api.caches.MISAOMETiffCache;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.swappers.OMETiffSwapper;
import org.hkijena.misa_imagej.utils.ui.ImagePlusJMenuItem;

import javax.swing.*;

/**
 * Editor for OME Tiff caches
 */
public class MISAOMETiffDataSourceUI extends MISADataSourceUI {

    private MISAOMETiffDataSource dataSource;
    private JTextField display;
    private JButton optionButton;

    public MISAOMETiffDataSourceUI(MISADataSource dataSource) {
        super(dataSource);
        this.dataSource = (MISAOMETiffDataSource)dataSource;
        refreshDisplay();
    }

    @Override
    protected void initialize() {
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
                dataSource.setTiffSwapper(new OMETiffSwapper(image, null));
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
                dataSource.setTiffSwapper(new OMETiffSwapper(null, chooser.getSelectedFile().getAbsolutePath()));
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

        if(dataSource == null || display == null)
            return;
        if(dataSource.getTiffSwapper() != null) {
            JMenuItem clearItem = new JMenuItem("Clear data", UIUtils.getIconFromResources("delete.png"));
            clearItem.addActionListener(actionEvent -> {
                dataSource.setTiffSwapper(null);
                refreshDisplay();
            });
            selectOptions.add(clearItem);

            if(!dataSource.getTiffSwapper().isInImageJ() && dataSource.getTiffSwapper().isInFilesystem()) {
                JMenuItem importItem = new JMenuItem("Import into ImageJ", UIUtils.getIconFromResources("import.png"));
                importItem.addActionListener(actionEvent -> {
                    dataSource.getTiffSwapper().importIntoImageJ(null); // BioFormats decides by itself
                    refreshDisplay();
                });
                selectOptions.add(importItem);
            }

            if(dataSource.getTiffSwapper().isInImageJ()) {
                JMenuItem selectItem = new JMenuItem("Select in ImageJ", UIUtils.getIconFromResources("target.png"));
                selectItem.addActionListener(actionEvent -> {
                    dataSource.getTiffSwapper().editInImageJ();
                });
                selectOptions.add(selectItem);
            }
        }
    }

    private void refreshDisplay() {
        if(dataSource == null || display == null)
            return;
        if(dataSource.getTiffSwapper() != null)
            display.setText(dataSource.getTiffSwapper().toString());
        else
            display.setText("<No data set>");
        createEditPopup(optionButton);
    }
}