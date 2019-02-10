package org.hkijena.misa_imagej.ui.parametereditor.datasources.editors;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.datasources.MISAPipelineNodeDataSource;
import org.hkijena.misa_imagej.ui.parametereditor.datasources.MISADataSourceUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.ColorIcon;

import javax.swing.*;
import java.awt.*;

public class MISAPipelineNodeDataSourceUI extends MISADataSourceUI {

    JComboBox<MISACache> cacheList;

    public MISAPipelineNodeDataSourceUI(MISADataSource dataSource) {
        super(dataSource);
        refreshDisplay();
    }

    @Override
    protected void initialize() {
        setLayout(new GridBagLayout());

        cacheList = new JComboBox<>();
        cacheList.setRenderer(new CacheListRenderer());
        cacheList.addItemListener(itemEvent -> {
            if(cacheList.getSelectedItem() != null) {
                getNativeDataSource().setSourceCache((MISACache) cacheList.getSelectedItem());
            }
        });
        add(cacheList, new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
            }
        });

        JButton applyButton = new JButton(UIUtils.getIconFromResources("copy.png"));
        applyButton.addActionListener(actionEvent -> applyToAllSamples());
        applyButton.setToolTipText("Apply this setting to equivalent data in all other samples");
        add(applyButton, new GridBagConstraints() {
            {
                gridx = 1;
                gridy = 0;
                fill = GridBagConstraints.VERTICAL;
            }
        });
    }

    private void applyToAllSamples() {
    }

    private void refreshDisplay() {
        MutableComboBoxModel<MISACache> model = new DefaultComboBoxModel<>();
        String currentSample = getDataSource().getCache().getSample().name;
        for(MISACache cache : getNativeDataSource().getSourceNode().getModuleInstance().
                getSample(currentSample).getExportedCaches()) {
            model.addElement(cache);
        }
        model.setSelectedItem(getNativeDataSource().getSourceCache());
        cacheList.setModel(model);

        if(model.getSelectedItem() != null) {
            getNativeDataSource().setSourceCache((MISACache) model.getSelectedItem());
        }
        else {
            // Select the first one that is available
            if(model.getSize() != 0) {
                getNativeDataSource().setSourceCache(model.getElementAt(0));
            }
        }
    }

    private MISAPipelineNodeDataSource getNativeDataSource() {
        return (MISAPipelineNodeDataSource)getDataSource();
    }

    private static class CacheListRenderer extends JPanel implements ListCellRenderer<MISACache> {

        private ColorIcon icon = new ColorIcon(21,21);
        private JLabel cacheLabel = new JLabel();
        private JLabel locationLabel = new JLabel();

        public CacheListRenderer() {
            initialize();
        }

        private void initialize() {
            setLayout(new GridBagLayout());
            add(new JLabel(icon), new GridBagConstraints() {
                {
                    gridx = 0;
                    gridy = 0;
                    gridheight = 2;
                    insets = new Insets(4, 8, 4, 4);
                }
            });
            add(cacheLabel, new GridBagConstraints() {
                {
                    gridx = 1;
                    gridy = 0;
                    weightx = 1;
                    fill = GridBagConstraints.HORIZONTAL;
                    insets = new Insets(4, 4, 0, 4);
                }
            });
            add(locationLabel, new GridBagConstraints() {
                {
                    gridx = 1;
                    gridy = 1;
                    weightx = 1;
                    fill = GridBagConstraints.HORIZONTAL;
                    insets = new Insets(0, 4, 4, 4);
                }
            });
            locationLabel.setFont(locationLabel.getFont().deriveFont(11.0f));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MISACache> list, MISACache value,
                                                      int index, boolean isSelected, boolean hasFocus) {
            if(value != null) {
                icon.setColor( value.toColor());
                cacheLabel.setText(value.getCacheTypeName() + ": " +
                        value.getRelativePathName());
                locationLabel.setText( value.getSample().getModuleInstance().getName());
            }

            return this;

        }
    }
}
