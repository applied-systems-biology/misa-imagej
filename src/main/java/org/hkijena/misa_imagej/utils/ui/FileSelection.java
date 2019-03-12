package org.hkijena.misa_imagej.utils.ui;

import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Text field with a file selection
 */
public class FileSelection extends JPanel {

    private JFileChooser jFileChooser = new JFileChooser();
    private JTextField pathEdit;
    private Mode mode;

    public FileSelection() {
        this.mode = Mode.OPEN;
        initialize();
    }

    public FileSelection(Mode mode) {
        this.mode = mode;
        initialize();
    }

    private void initialize() {
        setLayout(new GridBagLayout());

        pathEdit = new JTextField();
        add(pathEdit, new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                gridwidth = 1;
                anchor = GridBagConstraints.WEST;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                insets = UIUtils.UI_PADDING;
            }
        });

        JButton selectButton = new JButton(UIUtils.getIconFromResources("open.png"));
        add(selectButton, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.PAGE_START;
                gridx = 1;
                gridy = 0;
            }
        });

        selectButton.addActionListener(actionEvent -> {
            if(mode == Mode.OPEN) {
                if(getFileChooser().showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    pathEdit.setText(getFileChooser().getSelectedFile().toString());
                }
            }
            else {
                if(getFileChooser().showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    pathEdit.setText(getFileChooser().getSelectedFile().toString());
                }
            }
        });
    }

    public void setPath(Path path) {
        pathEdit.setText(path.toString());
    }

    public Path getPath() {
        return Paths.get(pathEdit.getText());
    }

    public JFileChooser getFileChooser() {
        return jFileChooser;
    }

    public enum Mode {
        OPEN,
        SAVE
    }
}
