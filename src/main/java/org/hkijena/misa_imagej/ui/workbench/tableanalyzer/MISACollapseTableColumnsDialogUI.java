package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

import com.google.common.base.Joiner;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.ui.registries.MISATableAnalyzerUIOperationRegistry;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class MISACollapseTableColumnsDialogUI extends JDialog {
    private DefaultTableModel tableModel;
    private DefaultTableModel resultTableModel;
    private List<JComboBox<Object>> columnOperations = new ArrayList<>();

    public MISACollapseTableColumnsDialogUI(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        initialize();
    }

    private void initialize() {
        setTitle("Integrate columns");
        setLayout(new BorderLayout(8,8));

        JPanel columnPanel = new JPanel(new GridBagLayout());

        for(int columnIndex = 0; columnIndex < tableModel.getColumnCount(); ++columnIndex) {

            final int row = columnIndex;
            JComboBox<Object> operationJComboBox = new JComboBox<>();
            operationJComboBox.setRenderer(new Renderer());
            columnOperations.add(operationJComboBox);

            operationJComboBox.addItem(null);
            operationJComboBox.addItem(new CategorizeColumnRole());
            List<MISATableAnalyzerUIOperationRegistry.Entry> entries =
                    new ArrayList<>( MISAImageJRegistryService.getInstance().getTableAnalyzerUIOperationRegistry().getEntries());
            entries.sort(Comparator.comparing(MISATableAnalyzerUIOperationRegistry.Entry::getName));

            for(MISATableAnalyzerUIOperationRegistry.Entry entry : entries) {
                MISATableVectorOperation operation = entry.instantiateOperation();
                if(operation.inputMatches(tableModel.getRowCount()) && operation.getOutputCount(tableModel.getRowCount()) == 1) {
                    operationJComboBox.addItem(operation);
                }
            }

            columnPanel.add(operationJComboBox, new GridBagConstraints() {
                {
                    gridx = 0;
                    gridy = row;
                    insets = UIUtils.UI_PADDING;
                    anchor = GridBagConstraints.NORTHWEST;
                }
            });

            JLabel label = new JLabel(tableModel.getColumnName(columnIndex),
                    UIUtils.getIconFromResources("select-column.png"), JLabel.LEFT);
            columnPanel.add(label, new GridBagConstraints() {
                {
                    gridx = 1;
                    gridy = row;
                    fill = GridBagConstraints.HORIZONTAL;
                    weightx = 1;
                    insets = UIUtils.UI_PADDING;
                    anchor = GridBagConstraints.WEST;
                }
            });
        }
        UIUtils.addFillerGridBagComponent(columnPanel, tableModel.getColumnCount(), 1);

        add(new JScrollPane(columnPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        buttonPanel.add(Box.createHorizontalGlue());

        JButton calculateButton = new JButton("Calculate", UIUtils.getIconFromResources("statistics.png"));
        calculateButton.addActionListener(e -> calculate());
        buttonPanel.add(calculateButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void calculate() {
        if(columnOperations.stream().anyMatch(entry -> entry.getSelectedItem() instanceof CategorizeColumnRole)) {
            calculateWithCategorization();
        }
        else {
            calculateWithoutCategorization();
        }
    }

    private void calculateWithCategorization() {
        List<Integer> categorySourceColumns = new ArrayList<>();
        for(int columnIndex = 0; columnIndex < tableModel.getColumnCount(); ++columnIndex) {
            Object columnRole = columnOperations.get(columnIndex).getSelectedItem();
            if(columnRole instanceof CategorizeColumnRole) {
                categorySourceColumns.add(columnIndex);
            }
        }

        int[] rowCategoryAssignments = new int[tableModel.getRowCount()];
        List<String> categoryNames = new ArrayList<>();
        StringBuilder currentCategoryName = new StringBuilder();

        for(int row = 0; row < tableModel.getRowCount(); ++row) {
            currentCategoryName.setLength(0);
            for(int column : categorySourceColumns) {
                if(currentCategoryName.length() > 0)
                    currentCategoryName.append(", ");
                currentCategoryName.append(tableModel.getColumnName(column));
                currentCategoryName.append("=");
                currentCategoryName.append(tableModel.getValueAt(row, column));
            }

            int categoryIndex = categoryNames.indexOf(currentCategoryName.toString());
            if(categoryIndex == -1) {
                categoryNames.add(currentCategoryName.toString());
                categoryIndex = categoryNames.size() - 1;
            }

            rowCategoryAssignments[row] = categoryIndex;
        }

        DefaultTableModel result = new DefaultTableModel();

        // Create columns
        result.addColumn("Category");
        for(int columnIndex = 0; columnIndex < tableModel.getColumnCount(); ++columnIndex) {
            Object columnRole = columnOperations.get(columnIndex).getSelectedItem();
            if(columnRole instanceof MISATableVectorOperation) {
                MISATableVectorOperation operation = (MISATableVectorOperation)columnRole;
                String shortcut = MISAImageJRegistryService.getInstance().getTableAnalyzerUIOperationRegistry().getShortcutOf(operation);
                result.addColumn(shortcut + "(" + tableModel.getColumnName(columnIndex) + ")");
            }
        }

        // Create rows
        ArrayList<Object> rowBuffer = new ArrayList<>();
        Vector<Object> resultRowBuffer = new Vector<>();
        for(int category = 0; category < categoryNames.size(); ++category) {
            resultRowBuffer.clear();
            resultRowBuffer.add(categoryNames.get(category));
            for(int columnIndex = 0; columnIndex < tableModel.getColumnCount(); ++columnIndex) {
                Object columnRole = columnOperations.get(columnIndex).getSelectedItem();
                if(columnRole instanceof MISATableVectorOperation) {
                    MISATableVectorOperation operation = (MISATableVectorOperation)columnRole;
                    rowBuffer.clear();
                    for (int row = 0; row < tableModel.getRowCount(); ++row) {
                        if (rowCategoryAssignments[row] == category) {
                            rowBuffer.add(tableModel.getValueAt(row, columnIndex));
                        }
                    }

                    Object[] integratedResult = operation.process(rowBuffer.toArray());
                    resultRowBuffer.add(integratedResult[0]);
                }
            }
            result.addRow(resultRowBuffer.toArray());
        }

        resultTableModel = result;
        setVisible(false);
    }

    private void calculateWithoutCategorization() {
        DefaultTableModel result = new DefaultTableModel();

        Object[] buffer = new Object[tableModel.getRowCount()];
        Vector<Object> row = new Vector<>();

        for(int columnIndex = 0; columnIndex < tableModel.getColumnCount(); ++columnIndex) {
            Object columnRole = columnOperations.get(columnIndex).getSelectedItem();
            if(columnRole instanceof MISATableVectorOperation) {
                MISATableVectorOperation operation = (MISATableVectorOperation)columnRole;
                String shortcut = MISAImageJRegistryService.getInstance().getTableAnalyzerUIOperationRegistry().getShortcutOf(operation);
                result.addColumn(shortcut + "(" + tableModel.getColumnName(columnIndex) + ")");

                for(int i = 0; i < tableModel.getRowCount(); ++i) {
                    buffer[i] = tableModel.getValueAt(i, columnIndex);
                }

                Object[] integratedResult = operation.process(buffer);
                row.add(integratedResult[0]);
            }
        }

        result.addRow(row);
        resultTableModel = result;
        setVisible(false);
    }

    public DefaultTableModel getResultTableModel() {
        return resultTableModel;
    }

    /**
     * Marks the column as categorization source
     */
    private static class CategorizeColumnRole {

    }

    private static class Renderer extends JLabel implements ListCellRenderer<Object> {

        public Renderer() {
            setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            if(value instanceof MISATableVectorOperation) {
                setText(MISAImageJRegistryService.getInstance().getTableAnalyzerUIOperationRegistry().getNameOf((MISATableVectorOperation) value));
                setIcon(MISAImageJRegistryService.getInstance().getTableAnalyzerUIOperationRegistry().getIconOf((MISATableVectorOperation) value));
            }
            else if(value instanceof CategorizeColumnRole) {
                setText("Use as category");
                setIcon(UIUtils.getIconFromResources("filter.png"));
            }
            else {
                setText("Ignore column");
                setIcon(UIUtils.getIconFromResources("remove.png"));
            }

            if(isSelected) {
                setBackground(new Color(184, 207, 229));
            }
            else {
                setBackground(new Color(255,255,255));
            }

            return this;
        }
    }
}
