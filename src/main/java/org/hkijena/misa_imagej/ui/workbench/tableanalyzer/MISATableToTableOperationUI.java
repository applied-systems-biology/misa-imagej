package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

import javax.swing.*;

public class MISATableToTableOperationUI extends JPanel {
    private MISATableToTableOperation operation;

    public MISATableToTableOperationUI(MISATableToTableOperation operation) {
        this.operation = operation;
    }

    public MISATableToTableOperation getOperation() {
        return operation;
    }
}
