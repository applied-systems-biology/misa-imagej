package org.hkijena.misa_imagej.ui.workbench;

import org.hkijena.misa_imagej.api.workbench.table.MISAAttachmentTable;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class MISAAttachmentTableColumnEditor extends JDialog {

    private MISAAttachmentTable table;

    public MISAAttachmentTableColumnEditor(MISAAttachmentTable table) {
        this.table = table;
        initialize();
    }

    private void initialize() {
        setTitle("Edit columns");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());
        setLayout(new BorderLayout());
    }

}
