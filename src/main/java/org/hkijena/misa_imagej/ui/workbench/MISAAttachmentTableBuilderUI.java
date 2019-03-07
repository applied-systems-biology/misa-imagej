package org.hkijena.misa_imagej.ui.workbench;

import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;

import javax.swing.*;
import java.util.List;

public class MISAAttachmentTableBuilderUI extends JPanel {
    private MISAAttachmentDatabase database;
    private List<Integer> databaseIds;

    public MISAAttachmentTableBuilderUI(MISAAttachmentDatabase database) {
        this.database = database;
    }

    public void setDatabaseIds(List<Integer> databaseIds) {
        this.databaseIds = databaseIds;
    }
}
