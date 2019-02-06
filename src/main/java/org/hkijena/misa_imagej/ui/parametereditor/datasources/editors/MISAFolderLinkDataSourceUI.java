package org.hkijena.misa_imagej.ui.parametereditor.datasources.editors;

import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.datasources.MISAFolderLinkDataSource;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.MISADataSourceUI;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;

public class MISAFolderLinkDataSourceUI extends MISADataSourceUI {

    private MISAFolderLinkDataSource dataSource;
    private JTextField display;

    public MISAFolderLinkDataSourceUI(MISADataSource dataSource) {
        super(dataSource);
        this.dataSource = (MISAFolderLinkDataSource)dataSource;
        refreshDisplay();
    }

    @Override
    protected void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        display = new JTextField();
        display.setEditable(false);
        add(display);

        JButton selectButton = new JButton(UIUtils.getIconFromResources("open.png"));
        selectButton.addActionListener(actionEvent -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select folder");
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                dataSource.setSourceFolder(chooser.getSelectedFile().toPath());
            }
            refreshDisplay();
        });
        add(selectButton);
        refreshDisplay();
    }

    private void refreshDisplay() {
        if (dataSource == null || display == null)
            return;
        if(dataSource.getSourceFolder() == null)
            display.setText("<No data set>");
        else
            display.setText(dataSource.getSourceFolder().toString());
    }
}