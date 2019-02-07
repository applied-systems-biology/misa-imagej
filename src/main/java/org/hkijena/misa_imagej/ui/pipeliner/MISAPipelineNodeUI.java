package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class MISAPipelineNodeUI extends JPanel implements ComponentListener {

    private MISAPipelineNode node;
    private static final Color BORDER_COLOR = new Color(128,128,128);

    public MISAPipelineNodeUI(MISAPipelineNode node) {
        this.node = node;
        initialize();
    }

    private void initialize() {
        addComponentListener(this);
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


    @Override
    public void componentResized(ComponentEvent componentEvent) {

    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {
        node.x = getX();
        node.y = getY();
    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {

    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {

    }
}
