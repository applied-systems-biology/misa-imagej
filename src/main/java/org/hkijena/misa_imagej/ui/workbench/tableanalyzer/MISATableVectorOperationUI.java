package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

import javax.swing.*;

public class MISATableVectorOperationUI extends JPanel {
    private MISATableVectorOperation operation;

    public MISATableVectorOperationUI(MISATableVectorOperation operation) {
        this.operation = operation;
    }

    public MISATableVectorOperation getOperation() {
        return operation;
    }
}
