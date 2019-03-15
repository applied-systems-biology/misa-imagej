package org.hkijena.misa_imagej.ui.workbench.tableanalyzer;

import org.hkijena.misa_imagej.MISAImageJRegistryService;

import javax.swing.*;
import java.awt.*;

public class MISATableVectorOperationDialogUI extends JDialog {

    private MISATableVectorOperation operation;
    private boolean needsOpenDialog;
    private boolean userAccepts;

    private MISATableVectorOperationDialogUI(MISATableVectorOperation operation) {
        this.operation = operation;

        MISATableVectorOperationUI ui = MISAImageJRegistryService.getInstance().getTableAnalyzerUIOperationRegistry().createUIForVectorOperation(operation);
        if(ui == null) {
            needsOpenDialog = false;
        }
        else {
            setLayout(new BorderLayout());
            add(ui, BorderLayout.CENTER);
        }
    }

    public MISATableVectorOperation getOperation() {
        return operation;
    }

    public boolean isNeedsOpenDialog() {
        return needsOpenDialog;
    }

    public boolean isUserAccepts() {
        return userAccepts;
    }
}
