package org.hkijena.misa_imagej.ui.workbench;

import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;

import javax.swing.*;

public class MISAAttachmentBrowserUI extends JPanel {

    private MISAOutput misaOutput;
    private MISAAttachmentDatabase attachmentDatabase;

    public MISAAttachmentBrowserUI(MISAOutput misaOutput) {
        this.misaOutput = misaOutput;
        this.attachmentDatabase = misaOutput.createAttachmentDatabase();
    }
}
