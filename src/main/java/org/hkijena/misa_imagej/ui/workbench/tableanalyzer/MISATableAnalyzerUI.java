package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.ui.registries.MISATableAnalyzerUIOperationRegistry;
import org.hkijena.misa_imagej.ui.workbench.MISAWorkbenchUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentTabPane;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class MISATableAnalyzerUI extends JPanel {
    private MISAWorkbenchUI workbench;
    private DefaultTableModel tableModel;
    private JXTable jxTable;
    private Stack<DefaultTableModel> undoBuffer = new Stack<>();
    private static final int MAX_UNDO = 10;
    private boolean isRebuildingSelection = false;

    private JButton convertSelectedCellsButton;
    private JPopupMenu convertSelectedCellsMenu;

    public MISATableAnalyzerUI(MISAWorkbenchUI workbench, DefaultTableModel tableModel) {
        this.workbench = workbench;
        this.tableModel = tableModel;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        JButton exportButton = new JButton("Export", UIUtils.getIconFromResources("save.png"));
        {
            JPopupMenu exportPopup = UIUtils.addPopupMenuToComponent(exportButton);

            JMenuItem exportAsCSV = new JMenuItem("as CSV table (*.csv)", UIUtils.getIconFromResources("filetype-csv.png"));
            exportAsCSV.addActionListener(e -> exportTableAsCSV());
            exportPopup.add(exportAsCSV);

            JMenuItem exportAsXLSX = new JMenuItem("as Excel table (*.xlsx)", UIUtils.getIconFromResources("filetype-excel.png"));
            exportAsXLSX.addActionListener(e -> exportTableAsXLSX());
            exportPopup.add(exportAsXLSX);
        }
        toolBar.add(exportButton);

        JButton cloneDataButton = new JButton("Clone", UIUtils.getIconFromResources("table.png"));
        cloneDataButton.addActionListener(e -> cloneDataToNewTab());
        toolBar.add(cloneDataButton);

        toolBar.addSeparator();

        JButton undoButton = new JButton("Undo", UIUtils.getIconFromResources("undo.png"));
        undoButton.addActionListener(e -> undo());
        toolBar.add(undoButton);

        toolBar.addSeparator();

        JButton addRowButton = new JButton("Add row", UIUtils.getIconFromResources("add-row.png"));
        addRowButton.addActionListener(e -> addRow());
        toolBar.add(addRowButton);

        JButton addColumnButton = new JButton("Add column", (UIUtils.getIconFromResources("add-column.png")));
        JPopupMenu addColumnMenu = UIUtils.addPopupMenuToComponent(addColumnButton);

        JMenuItem addNewColumnButton = new JMenuItem("Create new column", UIUtils.getIconFromResources("add.png"));
        addNewColumnButton.addActionListener(e -> addColumn());
        addColumnMenu.add(addNewColumnButton);

        JMenuItem copyColumnButton = new JMenuItem("Copy selected column", UIUtils.getIconFromResources("copy.png"));
        copyColumnButton.addActionListener(e -> copyColumn());
        addColumnMenu.add(copyColumnButton);

        toolBar.add(addColumnButton);

        JButton removeRowButton = new JButton(UIUtils.getIconFromResources("remove-row.png"));
        removeRowButton.setToolTipText("Remove selected rows");
        removeRowButton.addActionListener(e -> removeSelectedRows());
        toolBar.add(removeRowButton);

        JButton removeColumnButton = new JButton(UIUtils.getIconFromResources("remove-column.png"));
        removeColumnButton.setToolTipText("Remove selected columns");
        removeColumnButton.addActionListener(e -> removeSelectedColumns());
        toolBar.add(removeColumnButton);

        toolBar.addSeparator();

        JButton renameColumnButton = new JButton(UIUtils.getIconFromResources("label.png"));
        renameColumnButton.setToolTipText("Rename column");
        renameColumnButton.addActionListener(e -> renameColumn());
        toolBar.add(renameColumnButton);
        
        toolBar.addSeparator();

        JButton selectRowButton = new JButton(UIUtils.getIconFromResources("select-row.png"));
        selectRowButton.setToolTipText("Expand selection to whole row");
        selectRowButton.addActionListener(e -> selectMissingRows());
        toolBar.add(selectRowButton);

        JButton selectColumnButton = new JButton(UIUtils.getIconFromResources("select-column.png"));
        selectColumnButton.setToolTipText("Expand selection to whole column");
        selectColumnButton.addActionListener(e -> selectMissingColumns());
        toolBar.add(selectColumnButton);

        JButton invertSelectionButton = new JButton(UIUtils.getIconFromResources("invert.png"));
        invertSelectionButton.setToolTipText("Invert selection");
        invertSelectionButton.addActionListener(e -> invertSelection());
        toolBar.add(invertSelectionButton);

        JButton filterSelectButton = new JButton(UIUtils.getIconFromResources("filter.png"));
        filterSelectButton.setToolTipText("Select all rows that contain the selection of values");
        filterSelectButton.addActionListener(e -> selectEquivalent());
        toolBar.add(filterSelectButton);

        toolBar.addSeparator();

        JButton autoSizeColumnButton = new JButton(UIUtils.getIconFromResources("column-autosize.png"));
        autoSizeColumnButton.setToolTipText("Autosize selected columns");
        autoSizeColumnButton.addActionListener(e -> autoSizeColumns());
        toolBar.add(autoSizeColumnButton);

        toolBar.add(Box.createHorizontalGlue());

        JButton collapseColumnsButton = new JButton("Integrate columns", UIUtils.getIconFromResources("statistics.png"));
        collapseColumnsButton.addActionListener(e -> collapseColumns());
        toolBar.add(collapseColumnsButton);

        convertSelectedCellsButton = new JButton("Convert selection", UIUtils.getIconFromResources("inplace-function.png"));
        convertSelectedCellsMenu = UIUtils.addPopupMenuToComponent(convertSelectedCellsButton);
        toolBar.add(convertSelectedCellsButton);

        add(toolBar, BorderLayout.NORTH);

        jxTable = new JXTable();
        jxTable.setModel(tableModel);
        jxTable.setColumnSelectionAllowed(true);
        jxTable.setRowSelectionAllowed(true);
        jxTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jxTable.packAll();
        add(new JScrollPane(jxTable), BorderLayout.CENTER);

        jxTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> updateConvertMenu());
    }

    private void collapseColumns() {
        MISACollapseTableColumnsDialogUI dialog = new MISACollapseTableColumnsDialogUI(tableModel);
        dialog.pack();
        dialog.setSize(800,600);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setModal(true);
        dialog.setVisible(true);
        if(dialog.getResultTableModel() != null) {
            createUndoSnapshot();
            tableModel = dialog.getResultTableModel();
            jxTable.setModel(tableModel);
            jxTable.packAll();
        }
    }

    private void undo() {
        if(!undoBuffer.isEmpty()) {
            tableModel = undoBuffer.pop();
            jxTable.setModel(tableModel);
            jxTable.packAll();
        }
    }

    private void selectEquivalent() {
        if(jxTable.getSelectedRowCount() > 0 && jxTable.getSelectedColumnCount() > 0) {
            isRebuildingSelection = true;
            List<Object[]> possibleValues = new ArrayList<>();
            int[] columns = jxTable.getSelectedColumns().clone();
            for(int i = 0; i < columns.length; ++i) {
                columns[i] = jxTable.convertColumnIndexToModel(columns[i]);
            }

            // Query all possible values
            for(int viewRow : jxTable.getSelectedRows()) {
                Object[] tuple = new Object[columns.length];
                for(int i = 0; i < columns.length; ++i) {
                    tuple[i] = tableModel.getValueAt(jxTable.convertRowIndexToModel(viewRow), columns[i]);
                }
                possibleValues.add(tuple);
            }

            jxTable.clearSelection();

            // Select all rows that match one of the possible values
            jxTable.addColumnSelectionInterval(0, tableModel.getColumnCount() - 1);
            for(int row = 0; row < tableModel.getRowCount(); ++row) {
                boolean success = false;
                for(Object[] possibleValue : possibleValues) {
                    boolean valueSuccess = true;
                    for(int i = 0; i < columns.length; ++i) {
                        if(!Objects.equals(tableModel.getValueAt(row, columns[i]), possibleValue[i])) {
                            valueSuccess = false;
                            break;
                        }
                    }
                    if(valueSuccess) {
                        success = true;
                        break;
                    }
                }
                if(success) {
                    jxTable.addRowSelectionInterval(jxTable.convertRowIndexToView(row), jxTable.convertRowIndexToView(row));
                }
            }

            isRebuildingSelection = false;
            updateConvertMenu();
        }
    }

    private void invertSelection() {
        int[] cols = jxTable.getSelectedColumns().clone();
        int[] rows = jxTable.getSelectedRows().clone();
        isRebuildingSelection = true;
        jxTable.clearSelection();
        for(int column : cols) {
            jxTable.addColumnSelectionInterval(column, column);
        }
        for(int row = 0; row < jxTable.getRowCount(); ++row) {
            if(!Ints.contains(rows, row))
                jxTable.addRowSelectionInterval(row, row);
        }
        isRebuildingSelection = false;
        updateConvertMenu();
    }

    private void selectMissingColumns() {
        jxTable.addRowSelectionInterval(0, tableModel.getRowCount() - 1);
    }

    private void selectMissingRows() {
        jxTable.addColumnSelectionInterval(0, tableModel.getColumnCount() - 1);
    }

    private void cloneDataToNewTab() {
        workbench.addTab("Table analyzer",
                UIUtils.getIconFromResources("table.png"),
                new MISATableAnalyzerUI(workbench, cloneTableModel(tableModel)),
                DocumentTabPane.CloseMode.withAskOnCloseButton, true);
        workbench.setSelectedTab(workbench.getTabCount() - 1);
    }

    private static DefaultTableModel cloneTableModel(DefaultTableModel tableModel) {
        DefaultTableModel copy = new DefaultTableModel();
        copy.setColumnCount(tableModel.getColumnCount());
        {
            Object[] identifiers = new Object[tableModel.getColumnCount()];
            for (int i = 0; i < tableModel.getColumnCount(); ++i) {
                identifiers[i] = tableModel.getColumnName(i);
            }
            copy.setColumnIdentifiers(identifiers);
        }
        for(int row = 0; row < tableModel.getRowCount(); ++row) {
            Vector<Object> rowVector = new Vector<>(tableModel.getColumnCount());
            for(int column = 0; column < tableModel.getColumnCount(); ++column) {
                rowVector.add(tableModel.getValueAt(row, column));
            }
            copy.addRow(rowVector);
        }
        return copy;
    }

    private void updateConvertMenu() {
        if(isRebuildingSelection)
            return;
        convertSelectedCellsMenu.removeAll();
        final int cellCount = jxTable.getSelectedColumnCount() * jxTable.getSelectedRowCount();

        List<MISATableAnalyzerUIOperationRegistry.Entry> entries =
                new ArrayList<>( MISAImageJRegistryService.getInstance().getTableAnalyzerUIOperationRegistry().getEntries());
        entries.sort(Comparator.comparing(MISATableAnalyzerUIOperationRegistry.Entry::getName));

        for(MISATableAnalyzerUIOperationRegistry.Entry entry : entries) {
            MISATableVectorOperation operation = entry.instantiateOperation();
            if(operation.inputMatches(cellCount) && operation.getOutputCount(cellCount) == cellCount) {
                JMenuItem item = new JMenuItem(entry.getName(), entry.getIcon());
                item.setToolTipText(entry.getDescription());
                item.addActionListener(e -> {

                    createUndoSnapshot();

                    List<CellIndex> selectedCells = getSelectedCells();
                    assert cellCount == selectedCells.size();

                    Object[] buffer = new Object[cellCount];
                    for(int i = 0; i < cellCount; ++i) {
                        buffer[i] = tableModel.getValueAt(selectedCells.get(i).getRow(), selectedCells.get(i).getColumn());
                    }

                    buffer = operation.process(buffer);

                    for(int i = 0; i < cellCount; ++i) {
                        tableModel.setValueAt(buffer[i], selectedCells.get(i).getRow(), selectedCells.get(i).getColumn());
                    }
                });
                convertSelectedCellsMenu.add(item);
            }
        }

        convertSelectedCellsButton.setEnabled(convertSelectedCellsMenu.getComponentCount() > 0);
    }

    /**
     * Gets the selected cells
     * @return
     */
    private List<CellIndex> getSelectedCells() {
        List<CellIndex> result = new ArrayList<>();
        if(jxTable.getSelectedRows() != null && jxTable.getSelectedColumns() != null) {
            for(int row : jxTable.getSelectedRows()) {
                for(int column : jxTable.getSelectedColumns()) {
                    result.add(new CellIndex(row, column));
                }
            }
        }
        return result;
    }

    private void autoSizeColumns() {
        jxTable.packAll();
    }

    private void addColumn() {
        String name = JOptionPane.showInputDialog(this,
                "Please provide a name for the new column", "Column " + (tableModel.getColumnCount() + 1));
        if (name != null && !name.isEmpty()) {
            createUndoSnapshot();
            tableModel.addColumn(name);
            jxTable.packAll();
        }
    }

    private void copyColumn() {
        int sourceColumn = jxTable.getSelectedColumn();

        if(sourceColumn == -1)
            return;

        sourceColumn = jxTable.convertColumnIndexToModel(sourceColumn);

        String name = JOptionPane.showInputDialog(this,
                "Please provide a name for the new column", tableModel.getColumnName(sourceColumn));
        if (name != null && !name.isEmpty()) {
            createUndoSnapshot();
            tableModel.addColumn(name);
            for(int i = 0; i < tableModel.getRowCount(); ++i) {
                tableModel.setValueAt(tableModel.getValueAt(i, sourceColumn), i, tableModel.getColumnCount() - 1);
            }
            jxTable.packAll();
        }
    }

    private void renameColumn() {
        if (jxTable.getSelectedColumn() != -1) {
            createUndoSnapshot();
            String oldName = tableModel.getColumnName(jxTable.convertColumnIndexToModel(jxTable.getSelectedColumn()));
            String name = JOptionPane.showInputDialog(this,
                    "Please enter a new name for the new column", oldName);
            if (name != null && !name.isEmpty()) {
                Object[] identifiers = new Object[tableModel.getColumnCount()];
                for (int i = 0; i < tableModel.getColumnCount(); ++i) {
                    identifiers[i] = tableModel.getColumnName(i);
                }
                identifiers[jxTable.convertColumnIndexToModel(jxTable.getSelectedColumn())] = name;
                tableModel.setColumnIdentifiers(identifiers);
                jxTable.packAll();
            }
        }
    }


    private void addRow() {
        tableModel.addRow(new Object[tableModel.getColumnCount()]);
    }

    private void removeSelectedColumns() {
        if (jxTable.getSelectedColumns() != null) {
            createUndoSnapshot();
            int[] newColumnIndices = new int[tableModel.getColumnCount()];
            int newColumnCount = 0;
            for (int i = 0; i < tableModel.getColumnCount(); ++i) {
                if (!Ints.contains(jxTable.getSelectedColumns(), i)) {
                    newColumnIndices[newColumnCount] = jxTable.convertColumnIndexToModel(i);
                    ++newColumnCount;
                }
            }

            DefaultTableModel newModel = new DefaultTableModel();
            for (int i = 0; i < newColumnCount; ++i) {
                newModel.addColumn(tableModel.getColumnName(newColumnIndices[i]));
            }

            Object[] rowBuffer = new Object[newColumnCount];
            for (int i = 0; i < tableModel.getRowCount(); ++i) {
                for (int j = 0; j < newColumnCount; ++j) {
                    rowBuffer[j] = tableModel.getValueAt(i, newColumnIndices[j]);
                }
                newModel.addRow(rowBuffer);
            }

            tableModel = newModel;
            jxTable.setModel(tableModel);
            jxTable.packAll();
        }
    }

    private void removeSelectedRows() {
        if (jxTable.getSelectedRows() != null) {
            createUndoSnapshot();
            int[] rows = new int[jxTable.getSelectedRows().length];
            for (int i = 0; i < jxTable.getSelectedRows().length; ++i) {
                rows[i] = jxTable.convertRowIndexToModel(jxTable.getSelectedRows()[i]);
            }
            Arrays.sort(rows);

            Vector dataVector = tableModel.getDataVector();

            for (int i = 0; i < rows.length; ++i) {
                dataVector.remove(rows[i] - i);
            }

            tableModel.setDataVector(dataVector, getColumnIdentifiers());
            jxTable.packAll();
        }
    }

    private Vector getColumnIdentifiers() {
        Vector vector = new Vector(tableModel.getColumnCount());
        for(int i = 0; i < tableModel.getColumnCount(); ++i) {
            vector.add(tableModel.getColumnName(i));
        }
        return vector;
    }

    private void createUndoSnapshot() {
        if(undoBuffer.size() >= MAX_UNDO)
            undoBuffer.remove(0);

        undoBuffer.push(cloneTableModel(tableModel));
    }

    private void exportTableAsXLSX() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export table");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Quantification output");
            Row xlsxHeaderRow = sheet.createRow(0);
            for (int i = 0; i < tableModel.getColumnCount(); ++i) {
                Cell cell = xlsxHeaderRow.createCell(i, CellType.STRING);
                cell.setCellValue(tableModel.getColumnName(i));
            }
            for (int row = 0; row < tableModel.getRowCount(); ++row) {
                Row xlsxRow = sheet.createRow(row + 1);
                for (int column = 0; column < tableModel.getColumnCount(); ++column) {
                    Object value = tableModel.getValueAt(row, column);
                    if (value instanceof Number) {
                        Cell cell = xlsxRow.createCell(column, CellType.NUMERIC);
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        Cell cell = xlsxRow.createCell(column, CellType.BOOLEAN);
                        cell.setCellValue((Boolean) value);
                    } else {
                        Cell cell = xlsxRow.createCell(column, CellType.STRING);
                        cell.setCellValue("" + value);
                    }
                }
            }

            try {
                FileOutputStream stream = new FileOutputStream(fileChooser.getSelectedFile());
                workbook.write(stream);
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void exportTableAsCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export table");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(fileChooser.getSelectedFile()))) {
                String[] rowBuffer = new String[tableModel.getColumnCount()];

                for(int column = 0; column < tableModel.getColumnCount(); ++column) {
                    rowBuffer[column] = tableModel.getColumnName(column);
                }

                writer.write(Joiner.on(',').join(rowBuffer).getBytes(Charsets.UTF_8));
                writer.write("\n".getBytes(Charsets.UTF_8));

                for (int row = 0; row < tableModel.getRowCount(); ++row) {
                    for (int column = 0; column < tableModel.getColumnCount(); ++column) {
                        if (tableModel.getValueAt(row, column) instanceof Boolean) {
                            rowBuffer[column] = (Boolean) tableModel.getValueAt(row, column) ? "TRUE" : "FALSE";
                        } else if (tableModel.getValueAt(row, column) instanceof Number) {
                            rowBuffer[column] = tableModel.getValueAt(row, column).toString();
                        } else {
                            String content = "" + tableModel.getValueAt(row, column);
                            content = content.replace("\"", "\"\"");
                            if (content.contains(",")) {
                                content = "\"" + content + "\"";
                            }
                            rowBuffer[column] = content;
                        }
                    }
                    writer.write(Joiner.on(',').join(rowBuffer).getBytes(Charsets.UTF_8));
                    writer.write("\n".getBytes(Charsets.UTF_8));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class CellIndex {
        private int row;
        private int column;

        private CellIndex(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }
    }
}
