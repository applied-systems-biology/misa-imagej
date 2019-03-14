package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultMISAPlotSettingsUI extends MISAPlotSettingsUI {

    private int gridBagRow = 0;

    public DefaultMISAPlotSettingsUI(MISAPlot plot) {
        super(plot);
        setLayout(new GridBagLayout());
        initialize();
    }

    private void initialize() {
        addStringEditorComponent("Title", () -> getPlot().getTitle(), s -> getPlot().setTitle(s));
    }

    protected void addComponent(String label, Icon icon, Component component) {
        final int finalRow = gridBagRow++;
        add(new JLabel(label, icon, JLabel.LEFT), new GridBagConstraints() {
            {
                gridx = 0;
                gridy = finalRow;
                anchor = GridBagConstraints.WEST;
                insets = UIUtils.UI_PADDING;
            }
        });
        add(component, new GridBagConstraints() {
            {
                gridx = 1;
                gridy = finalRow;
                anchor = GridBagConstraints.WEST;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                insets = UIUtils.UI_PADDING;
            }
        });
    }

    protected void addStringEditorComponent(String label, Supplier<String> getter, Consumer<String> setter) {
        JTextField textField = new JTextField(getter.get());
        textField.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void changed(DocumentEvent documentEvent) {
                setter.accept("" + textField.getText());
            }
        });
        addComponent(label, UIUtils.getIconFromResources("text.png"), textField);
    }
}
