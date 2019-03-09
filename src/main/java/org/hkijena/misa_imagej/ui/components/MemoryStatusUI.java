package org.hkijena.misa_imagej.ui.components;

import javax.swing.*;

public class MemoryStatusUI extends JProgressBar {

    private Timer timer;
    private static final long MEGABYTES = 1024 * 1024;

    public MemoryStatusUI() {
        initialize();
    }

    private void initialize() {
        setStringPainted(true);
        setString("- / -");
        timer = new Timer(1000, e -> {
            setMaximum((int)(Runtime.getRuntime().maxMemory() / MEGABYTES));
            setValue((int)(Runtime.getRuntime().totalMemory() / MEGABYTES));
            setString((Runtime.getRuntime().totalMemory() / MEGABYTES) + "MB / " + (Runtime.getRuntime().maxMemory() / MEGABYTES) + "MB");
        });
        timer.setRepeats(true);
        timer.start();
    }
}
