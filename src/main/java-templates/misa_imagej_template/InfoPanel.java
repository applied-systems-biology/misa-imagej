package misa_imagej_template;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

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

        JTextPane description = new JTextPane();
        description.setEditable(false);
        description.setContentType("text/html");

        try {
            description.setText(Resources.toString(Resources.getResource(InfoPanel.class, "/description.html"), Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        add(description, BorderLayout.CENTER);
    }
}
