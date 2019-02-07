package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;
import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;

public class MISAPipelineUI extends JPanel {

    private MISAPipeline pipeline;

    public MISAPipelineUI() {
        super(null);
        initialize();

        MISAPipelineNodeUI ui1 = addNodeUI(null);
        MISAPipelineNodeUI ui2 = addNodeUI(null);
        ui1.setLocation(100,100);
        ui2.setLocation(300,200);
    }

    public MISAPipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(MISAPipeline pipeline) {
        this.pipeline = pipeline;
    }

    private void initialize() {
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

//        // Draw a nice grid
//        graphics.setColor(new Color(184, 207, 229));
//        for(int y = 0; y < getHeight(); y += 20) {
//            graphics.drawLine(0, y, getWidth() - 1, y);
//        }
//        for(int x = 0; x < getWidth(); x += 20) {
//            graphics.drawLine(x, 0, x, getHeight() - 1);
//        }
    }

    private MISAPipelineNodeUI addNodeUI(MISAPipelineNode node) {
        MISAPipelineNodeUI ui = new MISAPipelineNodeUI(node);
        add(ui);
        ui.setBounds(0, 0, 200,150);
        return ui;
    }
}
