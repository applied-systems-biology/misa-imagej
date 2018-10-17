package org.hkijena.misa_imagej;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class InfoPanel extends JPanel {

    public InfoPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        JLabel logo = new JLabel();
        logo.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/logo.png")).getImage().getScaledInstance(-1, 100, Image.SCALE_SMOOTH)));
        logo.setHorizontalAlignment(JLabel.CENTER);
        add(logo, BorderLayout.NORTH);

        JTextArea description = new JTextArea();
        description.setText( new BufferedReader(new InputStreamReader(InfoPanel.class.getResourceAsStream("/description.txt"))).lines().collect(Collectors.joining("\n")));
        description.setEditable(false);
        add(description, BorderLayout.CENTER);
    }
}
