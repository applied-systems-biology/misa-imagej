package misa_imagej_template;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSelection extends JPanel {

    private JFileChooser jFileChooser = new JFileChooser();
    private JTextField pathEdit;

    public FileSelection() {
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

        selectButton.addActionListener(actionEvent -> {
            if(getFileChooser().showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathEdit.setText(getFileChooser().getSelectedFile().toString());
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
}
