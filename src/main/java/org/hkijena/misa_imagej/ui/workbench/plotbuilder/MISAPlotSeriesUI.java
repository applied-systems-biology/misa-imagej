package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.stream.Collectors;

public class MISAPlotSeriesUI extends JPanel {
    private static final Color BORDER_COLOR = new Color(128, 128, 128);
    private MISAPlot plot;
    private MISAPlotSeries series;
    private JButton removeButton;

    public MISAPlotSeriesUI(MISAPlot plot, MISAPlotSeries series) {
        this.plot = plot;
        this.series = series;
        initialize();

        this.plot.getEventBus().register(this);
        updateRemoveButton();
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

        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(Box.createHorizontalStrut(8));

        removeButton = new JButton(UIUtils.getIconFromResources("delete.png"));
        removeButton.setToolTipText("Remove series");
        removeButton.addActionListener(e -> removeSeries());
        UIUtils.makeFlatWithoutMargin(removeButton);
        titlePanel.add(removeButton);

        add(titlePanel, BorderLayout.NORTH);
        add(contentContainer, BorderLayout.CENTER);
    }

    @Subscribe
    public void handlePlotSeriesChangedEvent(MISAPlot.PlotSeriesListChangedEvent event) {
        updateRemoveButton();
    }

    private void updateRemoveButton() {
        removeButton.setEnabled(plot.canAddSeries());
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
            column.setRenderer(new Renderer(plot));

            column.addItem(-1);
            for (int i = 0; i < plot.getTableModel().getColumnCount(); ++i) {
                column.addItem(i);
            }
            column.setSelectedItem(entry.getValue().getColumnIndex());
            column.addItemListener(e -> {
                if (column.getSelectedItem() instanceof Integer)
                    entry.getValue().setColumnIndex((Integer) column.getSelectedItem());
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

        revalidate();
        repaint();
    }

    public static class Renderer extends JLabel implements ListCellRenderer<Integer> {

        private MISAPlot plot;

        public Renderer(MISAPlot plot) {
            this.plot = plot;
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value >= 0) {
                setText(plot.getTableModel().getColumnName(value));
                setIcon(UIUtils.getIconFromResources("select-column.png"));
            } else {
                setText("Generate");
                setIcon(UIUtils.getIconFromResources("cog.png"));
            }
            return this;
        }
    }
}
