package org.hkijena.misa_imagej.data.importing;

import ij.ImagePlus;
import ij.WindowManager;
import net.imagej.Dataset;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.hkijena.misa_imagej.MISADialog;
import org.hkijena.misa_imagej.UIHelper;
import org.hkijena.misa_imagej.data.ImagePlusJMenuItem;
import org.hkijena.misa_imagej.data.MISADataType;
import org.hkijena.misa_imagej.data.MISAImportedData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Editor widget that allows selection of how data is imported
 */
public class MISAImporterSourceEditor extends JPanel {

    private MISADialog app;
    private MISAImportedData dataObject;
    private JTextField display;
    private JPopupMenu selectOptions;

    public MISAImporterSourceEditor(MISADialog app, MISAImportedData dataObject) {
        this.app = app;
        this.dataObject = dataObject;
        initialize();
    }

    private void updateDisplay() {
        if(dataObject.getImportSource() == null) {
            display.setText("<Not set>");
            display.setForeground(Color.red);
        }
        else {
            display.setText(dataObject.getImportSource().toString());
            display.setForeground(Color.BLACK);
        }
    }

    private void createPopup() {
        selectOptions = new JPopupMenu("Select import source");
        boolean hasImageJData = false;
        for(int i = 1; i <= WindowManager.getImageCount(); ++i) {
            final ImagePlus image = WindowManager.getImage(WindowManager.getNthImageID(i));
            if(MISAImageJImportSource.canHold(dataObject, image)) {
                ImagePlusJMenuItem item = new ImagePlusJMenuItem(image);
                item.addActionListener(actionEvent -> {
                    dataObject.setImportSource(new MISAImageJImportSource(dataObject, image));
                    updateDisplay();
                });
                selectOptions.add(item);
                hasImageJData = true;
            }
        }
        if(hasImageJData) {
            selectOptions.addSeparator();
        }

        // Allow selection from filesystem
        JMenuItem selectExternal = new JMenuItem("From filesystem ...");
        selectExternal.addActionListener(actionEvent -> {
            JFileChooser chooser = MISAFileImportSource.createFileChooserFor(dataObject);
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                dataObject.setImportSource(new MISAFileImportSource(dataObject, chooser.getSelectedFile().toPath()));
                updateDisplay();
            }
        });
        selectOptions.add(selectExternal);

        // Allow refresh of menu
        JMenuItem refresh = new JMenuItem("Refresh list");
        refresh.addActionListener(actionEvent -> {
            createPopup();
        });
        selectOptions.add(refresh);
    }

    private void initialize() {
        setLayout(new GridBagLayout());

        display = new JTextField();
        display.setEditable(false);
        add(display, new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                gridwidth = 1;
                anchor = GridBagConstraints.WEST;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                insets = UIHelper.UI_PADDING;
            }
        });

        JButton selectButton = new JButton("Select ...");
        add(selectButton, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 1;
                gridy = 0;
            }
        });

        createPopup();

        selectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                selectOptions.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            }
        });

        updateDisplay();
    }
}
