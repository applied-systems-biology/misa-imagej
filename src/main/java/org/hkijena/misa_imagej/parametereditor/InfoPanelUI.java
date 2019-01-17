package org.hkijena.misa_imagej.parametereditor;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.hkijena.misa_imagej.utils.ResourceUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class InfoPanelUI extends JPanel {

    public InfoPanelUI() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        JLabel logo = new JLabel();
        logo.setIcon(new ImageIcon(new ImageIcon(ResourceUtils.getPluginResource("logo.png")).getImage().getScaledInstance(-1, 100, Image.SCALE_SMOOTH)));
        logo.setHorizontalAlignment(JLabel.CENTER);
        add(logo, BorderLayout.NORTH);

        JTextPane description = new JTextPane();
        description.setEditable(false);
        description.setContentType("text/html");

        try {
            description.setText(Resources.toString(ResourceUtils.getPluginResource("description.html"), Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        add(description, BorderLayout.CENTER);
    }
}
