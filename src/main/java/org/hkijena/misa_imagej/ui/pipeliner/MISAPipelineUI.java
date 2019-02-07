package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;
import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MISAPipelineUI extends JPanel implements MouseMotionListener, MouseListener {

    private MISAPipeline pipeline;

    private MISAPipelineNodeUI currentlyDragged;
    private Point currentlyDraggedOffset = new Point();

    private PropertyChangeListener pipelineListener;

    private Map<MISAPipelineNode, MISAPipelineNodeUI> nodeUIMap = new HashMap<>();

    public MISAPipelineUI() {
        super(null);
        initialize();
        refresh();
    }

    public MISAPipeline getPipeline() {
        return pipeline;
    }

    private void initialize() {
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);

        // Update the UI when something changes
        pipelineListener = (propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("addNode") ||
                    propertyChangeEvent.getPropertyName().equals("addEdge")) {
                refresh();
            }
        });
    }

    public void refresh() {
        nodeUIMap.clear();
        removeAll();
        if(pipeline != null) {
            for(MISAPipelineNode node : pipeline.getNodes()) {
                addNodeUI(node);
            }
        }
        if(getParent() != null)
            getParent().revalidate();
        repaint();
    }

    public void setPipeline(MISAPipeline pipeline) {
        if(this.pipeline != null)
            this.pipeline.removePropertyChangeListener(pipelineListener);
        this.pipeline = pipeline;
        this.pipeline.addPropertyChangeListener(pipelineListener);
        refresh();
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

        // Draw the edges
        graphics.setColor(Color.BLACK);
        for(Map.Entry<MISAPipelineNode, Set<MISAPipelineNode>> kv : pipeline.getEdges().entrySet()) {
            for(MISAPipelineNode target : kv.getValue()) {
                MISAPipelineNodeUI sourceUI = nodeUIMap.get(kv.getKey());
                MISAPipelineNodeUI targetUI = nodeUIMap.get(target);
                graphics.drawLine(sourceUI.getX(), sourceUI.getY(), targetUI.getX(), targetUI.getY());
            }
        }
    }

    private MISAPipelineNodeUI addNodeUI(MISAPipelineNode node) {
        MISAPipelineNodeUI ui = new MISAPipelineNodeUI(node);
        add(ui);
        ui.setBounds(node.x, node.y, 200,150);
        nodeUIMap.put(node, ui);
        return ui;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if(currentlyDragged != null) {
            currentlyDragged.setLocation(currentlyDraggedOffset.x + mouseEvent.getX(),
                    currentlyDraggedOffset.y + mouseEvent.getY());
            repaint();
            if(getParent() != null)
                getParent().revalidate();
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseEvent.BUTTON1) {
            for(int i = 0; i < getComponentCount(); ++i) {
                Component component = getComponent(i);
                if(component.getBounds().contains(mouseEvent.getX(), mouseEvent.getY())) {
                    if(component instanceof MISAPipelineNodeUI) {
                        currentlyDragged = (MISAPipelineNodeUI)component;
                        currentlyDraggedOffset.x = component.getX() - mouseEvent.getX();
                        currentlyDraggedOffset.y = component.getY() - mouseEvent.getY();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        currentlyDragged = null;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public Dimension getPreferredSize() {
        int width = 0;
        int height = 0;
        for(int i = 0; i < getComponentCount(); ++i) {
            Component component = getComponent(i);
            width = Math.max(width, component.getX() + component.getWidth());
            height = Math.max(height, component.getY() + component.getHeight());
        }
        return new Dimension(width, height);
    }

}
