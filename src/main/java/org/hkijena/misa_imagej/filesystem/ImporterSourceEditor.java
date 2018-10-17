package org.hkijena.misa_imagej.filesystem;

import org.hkijena.misa_imagej.ImageJHelper;
import org.hkijena.misa_imagej.UIHelper;
import org.hkijena.misa_imagej.data.FileImportSource;
import org.hkijena.misa_imagej.data.MISAImportedData;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Editor widget that allows selection of how data is imported
 */
public class ImporterSourceEditor extends JPanel {

    private JSONSchemaObject jsonSchemaObject;
    private JTextField display;
    private JPopupMenu selectOptions;

    public ImporterSourceEditor(JSONSchemaObject object) {
        jsonSchemaObject = object;
        initialize();
    }

    private void updateDisplay() {
        if(((MISAImportedData)jsonSchemaObject.filesystemData).getImportSource() == null) {
            display.setText("<Not set>");
            display.setForeground(Color.red);
        }
        else {
            display.setText(((MISAImportedData)jsonSchemaObject.filesystemData).getImportSource().toString());
            display.setForeground(Color.BLACK);
        }

    }

    private String[] getAvailableWindows() {
        switch(jsonSchemaObject.filesystemData.getType()) {
            case image_stack:
                return ImageJHelper.getImage3DNames();
            case image_file:
                return ImageJHelper.getImage2DNames();
            default:
                return new String[0];
        }
    }

    private void createPopup() {
        selectOptions = new JPopupMenu("Select import source");
        for(String window : getAvailableWindows()) {
            JMenuItem item = new JMenuItem("[ImageJ] " + window);
            selectOptions.add(item);
        }
        if(getAvailableWindows().length > 0) {
            selectOptions.addSeparator();
        }

        // Allow selection from filesystem
        JMenuItem selectExternal = new JMenuItem("From filesystem ...");
        selectExternal.addActionListener(actionEvent -> {
            JFileChooser chooser = FileImportSource.createFileChooserFor((MISAImportedData)jsonSchemaObject.filesystemData);
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                ((MISAImportedData)jsonSchemaObject.filesystemData).setImportSource(
                        new FileImportSource(((MISAImportedData)jsonSchemaObject.filesystemData), chooser.getSelectedFile().toPath()));
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
