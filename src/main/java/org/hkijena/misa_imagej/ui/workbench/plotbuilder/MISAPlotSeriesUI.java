/*
 * Copyright by Ruman Gerst
 * Research Group Applied Systems Biology - Head: Prof. Dr. Marc Thilo Figge
 * https://www.leibniz-hki.de/en/applied-systems-biology.html
 * HKI-Center for Systems Biology of Infection
 * Leibniz Institute for Natural Product Research and Infection Biology - Hans Knöll Insitute (HKI)
 * Adolf-Reichwein-Straße 23, 07745 Jena, Germany
 *
 * This code is licensed under BSD 2-Clause
 * See the LICENSE file provided with this code for the full license.
 */

package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Map;

public class MISAPlotSeriesUI extends JPanel {
    private static final Color BORDER_COLOR = new Color(128, 128, 128);
    private MISAPlot plot;
    private MISAPlotSeries series;
    private JButton removeButton;
    private JButton enableToggleButton;
    private JButton moveUpButton;
    private JButton moveDownButton;

    public MISAPlotSeriesUI(MISAPlot plot, MISAPlotSeries series) {
        this.plot = plot;
        this.series = series;
        initialize();

        this.plot.getEventBus().register(this);
        updateTitleBarButtons();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // Create content panel
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setOpaque(false);
        contentContainer.setBorder(BorderFactory.createEmptyBorder(4, 16, 8, 16));
        JPanel content = new JPanel();
        contentContainer.add(content, BorderLayout.CENTER);
        initializeContent(content);

        // Create title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        titlePanel.setBackground(Color.LIGHT_GRAY);
        titlePanel.setOpaque(true);

        moveDownButton = new JButton(UIUtils.getIconFromResources("arrow-down.png"));
        UIUtils.makeFlatWithoutMargin(moveDownButton);
        moveDownButton.setToolTipText("Move down");
        moveDownButton.addActionListener(e -> plot.moveSeriesDown(series));
        titlePanel.add(moveDownButton);

        moveUpButton = new JButton(UIUtils.getIconFromResources("arrow-up.png"));
        UIUtils.makeFlatWithoutMargin(moveUpButton);
        moveUpButton.setToolTipText("Move up");
        moveUpButton.addActionListener(e -> plot.moveSeriesUp(series));
        titlePanel.add(moveUpButton);

        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(Box.createHorizontalStrut(8));

        removeButton = new JButton(UIUtils.getIconFromResources("delete.png"));
        removeButton.setToolTipText("Remove series");
        removeButton.addActionListener(e -> removeSeries());
        UIUtils.makeFlatWithoutMargin(removeButton);
        titlePanel.add(removeButton);

        enableToggleButton = new JButton();
        UIUtils.makeFlatWithoutMargin(enableToggleButton);
        enableToggleButton.addActionListener(e -> toggleEnableDisable());
        titlePanel.add(Box.createHorizontalStrut(4));
        titlePanel.add(enableToggleButton);
        updateEnableDisableToggleButton();

        add(titlePanel, BorderLayout.NORTH);
        add(contentContainer, BorderLayout.CENTER);
    }

    private void toggleEnableDisable() {
        series.setEnabled(!series.isEnabled());
        updateEnableDisableToggleButton();
    }

    @Subscribe
    public void handlePlotSeriesChangedEvent(MISAPlot.PlotSeriesListChangedEvent event) {
        updateTitleBarButtons();
    }

    private void updateTitleBarButtons() {
        removeButton.setEnabled(plot.canRemoveSeries());
        enableToggleButton.setEnabled(plot.canRemoveSeries());
        moveUpButton.setVisible(plot.canRemoveSeries());
        moveDownButton.setVisible(plot.canRemoveSeries());
    }

    private void removeSeries() {
        plot.removeSeries(series);
    }

    private void initializeContent(JPanel content) {
        content.setLayout(new GridBagLayout());
        int row = 0;
        for(String key : series.getParameterNames()) {
            JLabel label = new JLabel(key);
            final int finalRow = row;

            if(series.getParameterType(key).equals(String.class)) {
                JTextField editor = new JTextField((String)series.getParameterValue(key));
                editor.getDocument().addDocumentListener(new DocumentChangeListener() {
                    @Override
                    public void changed(DocumentEvent documentEvent) {
                        series.setParameterValue(key, "" + editor.getText());
                    }
                });
                content.add(editor, new GridBagConstraints() {
                    {
                        gridx = 1;
                        gridy = finalRow;
                        anchor = GridBagConstraints.WEST;
                        insets = UIUtils.UI_PADDING;
                        fill = GridBagConstraints.HORIZONTAL;
                        weightx = 1;
                    }
                });
            }
            else if(Number.class.isAssignableFrom(series.getParameterType(key))) {
                JSpinner spinner = new JSpinner(new SpinnerNumberModel());
                spinner.setValue(series.getParameterValue(key));
                spinner.addChangeListener(e -> series.setParameterValue(key, spinner.getValue()));
                content.add(spinner, new GridBagConstraints() {
                    {
                        gridx = 1;
                        gridy = finalRow;
                        anchor = GridBagConstraints.WEST;
                        insets = UIUtils.UI_PADDING;
                        fill = GridBagConstraints.HORIZONTAL;
                        weightx = 1;
                    }
                });
            }
            else {
                continue;
            }
            content.add(label, new GridBagConstraints() {
                {
                    gridx = 0;
                    gridy = finalRow;
                    anchor = GridBagConstraints.WEST;
                    insets = UIUtils.UI_PADDING;
                }
            });
            ++row;
        }
        for (Map.Entry<String, MISAPlotSeriesColumn> entry : series.getColumns().entrySet()) {
            JLabel label = new JLabel(entry.getKey());
            JComboBox<Integer> column = new JComboBox<>();
            column.setRenderer(new Renderer(entry.getValue(), plot));

            for(int i = 0; i < entry.getValue().getGenerators().size(); ++i) {
                column.addItem(-(i + 1));
            }
            for (int i = 0; i < plot.getSeriesDataList().size(); ++i) {
                column.addItem(i);
            }
            column.setSelectedItem(entry.getValue().getSeriesDataIndex());
            column.addItemListener(e -> {
                if (column.getSelectedItem() instanceof Integer)
                    entry.getValue().setSeriesDataIndex((Integer) column.getSelectedItem());
            });

            if (entry.getValue() instanceof MISAStringPlotSeriesColumn) {
                label.setIcon(UIUtils.getIconFromResources("text.png"));
            } else if (entry.getValue() instanceof MISANumericPlotSeriesColumn) {
                label.setIcon(UIUtils.getIconFromResources("number.png"));
            }

            final int finalRow = row;
            content.add(label, new GridBagConstraints() {
                {
                    gridx = 0;
                    gridy = finalRow;
                    anchor = GridBagConstraints.WEST;
                    insets = UIUtils.UI_PADDING;
                }
            });
            content.add(column, new GridBagConstraints() {
                {
                    gridx = 1;
                    gridy = finalRow;
                    anchor = GridBagConstraints.WEST;
                    insets = UIUtils.UI_PADDING;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                }
            });

            ++row;
        }

        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    private void updateEnableDisableToggleButton() {
        if (series.isEnabled()) {
            enableToggleButton.setIcon(UIUtils.getIconFromResources("eye.png"));
            enableToggleButton.setToolTipText("Disable series");
        } else {
            enableToggleButton.setIcon(UIUtils.getIconFromResources("eye-slash.png"));
            enableToggleButton.setToolTipText("Enable series");
        }

    }

    public static class Renderer extends JLabel implements ListCellRenderer<Integer> {

        private MISAPlotSeriesColumn column;
        private MISAPlot plot;

        public Renderer(MISAPlotSeriesColumn column, MISAPlot plot) {
            this.column = column;
            this.plot = plot;
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value >= 0) {
                setText(plot.getSeriesDataList().get(value).getName());
                setIcon(UIUtils.getIconFromResources("select-column.png"));
            } else {
                MISAPlotSeriesGenerator generator = (MISAPlotSeriesGenerator) column.getGenerators().get(-value - 1);
                setText(generator.getName());
                setIcon(UIUtils.getIconFromResources("cog.png"));
            }
            return this;
        }
    }
}
