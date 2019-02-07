package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;
import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;
import org.hkijena.misa_imagej.utils.GraphicsUtils;
import org.hkijena.misa_imagej.utils.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
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

    private static final Polygon arrowHead = new Polygon() {
        {
            addPoint( 0,1);
            addPoint( -5, -10);
            addPoint( 5,-10);
        }
    };

    private AffineTransform arrowHeadTransform = new AffineTransform();

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

        Graphics2D g = (Graphics2D)graphics;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHints(rh);

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
        g.setStroke(new BasicStroke(2));
        for(Map.Entry<MISAPipelineNode, Set<MISAPipelineNode>> kv : pipeline.getEdges().entrySet()) {
            for(MISAPipelineNode target : kv.getValue()) {
                MISAPipelineNodeUI sourceUI = nodeUIMap.get(kv.getKey());
                MISAPipelineNodeUI targetUI = nodeUIMap.get(target);
                Point targetPoint = MathUtils.getLineRectableIntersection(new Line2D.Double(sourceUI.getX() + sourceUI.getWidth() / 2.0, sourceUI.getY() + sourceUI.getHeight() / 2.0,
                        targetUI.getX() + targetUI.getWidth() / 2.0, targetUI.getY() + targetUI.getHeight() / 2.0),
                        new Rectangle(targetUI.getX(), targetUI.getY(), targetUI.getWidth(), targetUI.getHeight()));
                if(targetPoint != null) {
//                    Line2D finalLine = new Line2D.Double(sourceUI.getX() + sourceUI.getWidth() / 2.0,
//                            sourceUI.getY() + sourceUI.getHeight() / 2.0,
//                            targetPoint.x,
//                            targetPoint.y);
                    GraphicsUtils.drawArrowLine(g, sourceUI.getX() + sourceUI.getWidth() / 2,
                            sourceUI.getY() + sourceUI.getHeight() / 2, targetPoint.x, targetPoint.y, 8, 5);
//                    g.draw(finalLine);
//
//                    // Draw arrowhead
//                    arrowHeadTransform.setToIdentity();
//                    double angle = Math.atan2(finalLine.getY2()-finalLine.getY1(), finalLine.getX2()-finalLine.getX1());
//                    arrowHeadTransform.translate(finalLine.getX2(), finalLine.getY2());
//                    arrowHeadTransform.rotate((angle-Math.PI/2d));
//
//                    AffineTransform backup = g.getTransform();
//                    g.setTransform(arrowHeadTransform);
//                    backup.transform();
//                    g.fill(arrowHead);
//                    g.setTransform(backup);

                }
            }
        }
    }

    private MISAPipelineNodeUI addNodeUI(MISAPipelineNode node) {
        MISAPipelineNodeUI ui = new MISAPipelineNodeUI(node);
        add(ui);
        ui.setBounds(node.getX(), node.getY(), 200,150);
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
