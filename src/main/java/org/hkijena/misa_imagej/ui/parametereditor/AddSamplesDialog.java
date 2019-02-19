package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.ui.components.MISAValidityReportStatusUI;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class AddSamplesDialog extends JDialog {

    private MISAModuleInstance moduleInstance;
    private JTextArea samplesInput;
    private MISAValidityReportStatusUI validityReportStatusUI;

    public AddSamplesDialog(Window parent, MISAModuleInstance moduleInstance) {
        super(parent);
        this.moduleInstance = moduleInstance;
        initialize();
    }

    private void initialize() {
        setSize(400, 300);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("Add samples");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());

        JTextArea infoArea = new JTextArea("Please insert the name of the sample. You can also add multiple samples at once by writing multiple lines. Each line represents one sample.");
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setBorder(null);
        infoArea.setWrapStyleWord(true);
        infoArea.setLineWrap(true);
        add(infoArea, BorderLayout.NORTH);

        samplesInput = new JTextArea();
        add(samplesInput, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        validityReportStatusUI = new MISAValidityReportStatusUI();
        buttonPanel.add(validityReportStatusUI);

        buttonPanel.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(cancelButton);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addFromInput());
        buttonPanel.add(addButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFromInput() {
        if(samplesInput.getText() != null && !samplesInput.getText().isEmpty()) {
            for(String line : samplesInput.getText().split("\n")) {
                String modified = line.trim();
                if(!modified.isEmpty()) {
                    if(!moduleInstance.getSamples().containsKey(modified)) {
                        moduleInstance.addSample(modified);
                    }
                }
            }
        }
        setVisible(false);
    }
}
