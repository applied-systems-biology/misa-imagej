package org.hkijena.misa_imagej.ui.components;

import com.google.common.base.Charsets;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class MarkdownReader extends JPanel {

    static final MutableDataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
    static final String[] CSS_RULES = {"body { font-family: \"Sans-serif\"; }",
            "pre { background-color: #f5f2f0; border: 3px #f5f2f0 solid; }",
            "code { background-color: #f5f2f0; }",
            "h2 { padding-top: 30px; }",
            "h3 { padding-top: 30px; }",
            "th { border-bottom: 1px solid #c8c8c8; }"};

    private JScrollPane scrollPane;
    private JTextPane content;
    private String markdown;

    public MarkdownReader() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        content = new JTextPane();
        content.setEditable(false);

        HTMLEditorKit kit = new HTMLEditorKit();
        initializeStyleSheet(kit.getStyleSheet());

        content.setEditorKit(kit);
        content.setContentType("text/html");
        scrollPane = new JScrollPane(content);
        add(scrollPane, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();

        JButton saveMarkdown = new JButton("Save as *.md", UIUtils.getIconFromResources("save.png"));
        saveMarkdown.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save as Markdown");
            if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.write(fileChooser.getSelectedFile().toPath(), markdown.getBytes(Charsets.UTF_8));
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
        toolBar.add(saveMarkdown);

        JButton saveHTML = new JButton("Save as *.html", UIUtils.getIconFromResources("save.png"));
        saveHTML.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save as HTML");
            if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.write(fileChooser.getSelectedFile().toPath(),toHTML().getBytes(Charsets.UTF_8));
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
        toolBar.add(saveHTML);

        JButton savePDF = new JButton("Save as *.pdf", UIUtils.getIconFromResources("save.png"));
        savePDF.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save as PDF");
            if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                PdfConverterExtension.exportToPdf(fileChooser.getSelectedFile().toString(), toHTML(), "", OPTIONS);
            }
        });
        toolBar.add(savePDF);

//        JButton printButton = new JButton("Print", UIUtils.getIconFromResources("print.png"));
//        printButton.addActionListener(e -> {
//            try {
//                content.print();
//            } catch (PrinterException e1) {
//                throw new RuntimeException(e1);
//            }
//        });
//        toolBar.add(printButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private String toHTML() {
        String html = content.getText();
        StringBuilder stylesheet = new StringBuilder();
        for(String rule : CSS_RULES) {
            stylesheet.append(rule).append(" ");
        }
        html = "<html><head><style>" + stylesheet + "</style></head><body>" + html + "</body></html>";
        return html;
    }

    private void initializeStyleSheet(StyleSheet styleSheet) {
        for(String rule : CSS_RULES) {
            styleSheet.addRule(rule);
        }
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
        Parser parser = Parser.builder(OPTIONS).build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder(OPTIONS).build();
        String html = renderer.render(document);
        content.setText(html);
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    public String getMarkdown() {
        return markdown;
    }
}
