package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;

import javax.swing.*;
import java.awt.*;

public class MISAPipelineNodeUI extends JPanel {

    private MISAPipelineNode node;
    private static final Color BORDER_COLOR = new Color(128,128,128);

    public MISAPipelineNodeUI(MISAPipelineNode node) {
        this.node = node;
    }

    public MISAPipelineNode getNode() {
        return node;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(BORDER_COLOR);
        graphics.drawRect(0,0,getWidth() - 1, getHeight() - 1);
    }
}
