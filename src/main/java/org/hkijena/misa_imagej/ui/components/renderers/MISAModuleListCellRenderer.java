package org.hkijena.misa_imagej.ui.components.renderers;

import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;

import javax.swing.*;
import java.awt.*;

public class MISAModuleListCellRenderer extends JPanel implements ListCellRenderer<MISAModule> {

    private MonochromeColorIcon icon = new MonochromeColorIcon(UIUtils.getIconFromResources("module-template.png"));
    private JLabel nameLabel = new JLabel();
    private JLabel versionLabel = new JLabel();

    public MISAModuleListCellRenderer() {
        super(new GridBagLayout());
        add(new JLabel(icon), new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                gridheight = 2;
                insets = new Insets(4, 8, 4, 4);
            }
        });
        add(nameLabel, new GridBagConstraints() {
            {
                gridx = 1;
                gridy = 0;
                weightx = 1;
                fill = GridBagConstraints.HORIZONTAL;
                insets = new Insets(4, 4, 0, 4);
            }
        });
        add(versionLabel, new GridBagConstraints() {
            {
                gridx = 1;
                gridy = 1;
                weightx = 1;
                fill = GridBagConstraints.HORIZONTAL;
                insets = new Insets(0, 4, 4, 4);
            }
        });
        versionLabel.setFont(versionLabel.getFont().deriveFont(11.0f));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MISAModule> list, MISAModule value, int index, boolean isSelected, boolean cellHasFocus) {
        nameLabel.setText(value.getModuleInfo().getDescription());
        icon.setColor(value.getModuleInfo().toColor());
        if(value.getModuleInfo().description == null || value.getModuleInfo().description.isEmpty())
            versionLabel.setText(value.getModuleInfo().getVersion());
        else
            versionLabel.setText(value.getModuleInfo().getName() + " - " + value.getModuleInfo().getVersion());

        if(isSelected) {
            setBackground(new Color(184, 207, 229));
        }
        else {
            setBackground(new Color(255,255,255));
        }

        return this;
    }
}
