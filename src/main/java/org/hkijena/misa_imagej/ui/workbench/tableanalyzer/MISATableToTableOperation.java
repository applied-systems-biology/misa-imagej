package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

import javax.swing.table.DefaultTableModel;

public interface MISATableToTableOperation {
    DefaultTableModel process(DefaultTableModel input);
}
