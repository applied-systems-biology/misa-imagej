package misa_imagej_template;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;

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

        Parser parser = Parser.builder().build();
        try {
            Node document = parser.parseReader(new InputStreamReader(InfoPanel.class.getResourceAsStream("/description.md")));
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            description.setText(renderer.render(document));
        } catch (IOException e) {
            e.printStackTrace();
        }

        add(description, BorderLayout.CENTER);
    }
}
