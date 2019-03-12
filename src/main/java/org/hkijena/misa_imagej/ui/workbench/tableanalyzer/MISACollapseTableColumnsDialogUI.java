package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MISACollapseTableColumnsDialogUI extends JDialog {
    private DefaultTableModel tableModel;

    public MISACollapseTableColumnsDialogUI(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        initialize();
    }

    private void initialize() {
        setTitle("Collapse columns");
        setLayout(new BorderLayout());

    }
}
