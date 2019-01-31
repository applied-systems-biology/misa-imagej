package org.hkijena.misa_imagej.ui.workbench;

import javax.swing.*;
import java.awt.*;

public class MISAWorkbench extends JFrame {

    public MISAWorkbench() {
        initialize();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("MISA++ Workbench for ImageJ");
    }

}
