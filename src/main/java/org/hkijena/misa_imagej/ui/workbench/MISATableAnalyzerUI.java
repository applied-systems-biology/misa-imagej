package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MISATableAnalyzerUI extends JPanel {
    private DefaultTableModel tableModel;
    private JXTable jxTable;

    public MISATableAnalyzerUI(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        JButton exportButton = new JButton("Export", UIUtils.getIconFromResources("save.png"));
        {
            JPopupMenu exportPopup = UIUtils.addPopupMenuToComponent(exportButton);

            JMenuItem exportAsCSV = new JMenuItem("as CSV table (*.csv)", UIUtils.getIconFromResources("table.png"));
            exportAsCSV.addActionListener(e -> exportTableAsCSV());
            exportPopup.add(exportAsCSV);

            JMenuItem exportAsXLSX = new JMenuItem("as Excel table (*.xlsx)", UIUtils.getIconFromResources("table.png"));
            exportAsXLSX.addActionListener(e -> exportTableAsXLSX());
            exportPopup.add(exportAsXLSX);
        }
        toolBar.add(exportButton);

        add(toolBar, BorderLayout.NORTH);

        jxTable = new JXTable();
        jxTable.setModel(tableModel);
        jxTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jxTable.packAll();
        add(new JScrollPane(jxTable), BorderLayout.CENTER);
    }

    private void exportTableAsXLSX() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export table");
        if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Quantification output");
            Row xlsxHeaderRow = sheet.createRow(0);
            for(int i = 0; i < tableModel.getColumnCount(); ++i) {
                Cell cell = xlsxHeaderRow.createCell(i, CellType.STRING);
                cell.setCellValue(tableModel.getColumnName(i));
            }
            for(int row = 0; row < tableModel.getRowCount(); ++row) {
                Row xlsxRow = sheet.createRow(row + 1);
                for(int column = 0; column < tableModel.getColumnCount(); ++column) {
                    Object value = tableModel.getValueAt(row, column);
                    if(value instanceof Number) {
                        Cell cell = xlsxRow.createCell(column, CellType.NUMERIC);
                        cell.setCellValue(((Number)value).doubleValue());
                    }
                    else if(value instanceof Boolean) {
                        Cell cell = xlsxRow.createCell(column, CellType.BOOLEAN);
                        cell.setCellValue((Boolean) value);
                    }
                    else {
                        Cell cell = xlsxRow.createCell(column, CellType.STRING);
                        cell.setCellValue("" + value);
                    }
                }
            }
        }
    }

    private void exportTableAsCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export table");
        if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try(BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(fileChooser.getSelectedFile()))) {
                String[] rowBuffer = new String[tableModel.getColumnCount()];
                for(int row = 0; row < tableModel.getRowCount(); ++row) {
                    for(int column = 0; column < tableModel.getColumnCount(); ++column) {
                        if(tableModel.getValueAt(row, column) instanceof Boolean) {
                            rowBuffer[column] = (Boolean)tableModel.getValueAt(row, column) ? "TRUE" : "FALSE";
                        }
                        else if(tableModel.getValueAt(row, column) instanceof Number) {
                            rowBuffer[column] = tableModel.getValueAt(row, column).toString();
                        }
                        else {
                            String content = "" + tableModel.getValueAt(row, column);
                            content = content.replace("\"", "\"\"");
                            if(content.contains(",")) {
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
}
