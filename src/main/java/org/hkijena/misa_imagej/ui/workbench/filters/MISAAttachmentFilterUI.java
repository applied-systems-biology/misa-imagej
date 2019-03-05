package org.hkijena.misa_imagej.ui.workbench.filters;

import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class MISAAttachmentFilterUI extends JPanel {

    private static final Color BORDER_COLOR = new Color(128,128,128);
    private MISAAttachmentFilter filter;
    private JPanel content;

    public MISAAttachmentFilterUI(MISAAttachmentFilter filter) {
        this.filter = filter;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // Create content panel
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setOpaque(false);
        contentContainer.setBorder(BorderFactory.createEmptyBorder(4,16,8,16));
        content = new JPanel();
        contentContainer.add(content, BorderLayout.CENTER);

        // Create title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20,16,4,16));
        titlePanel.setOpaque(false);

        JLabel nameLabel = new JLabel(MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry()
                .getNameFilterName(filter.getClass()));
        titlePanel.add(nameLabel);
        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(Box.createHorizontalStrut(8));

        JButton removeButton = new JButton(UIUtils.getIconFromResources("delete.png"));
        removeButton.addActionListener(e -> removeFilter());
        UIUtils.makeFlatWithoutMargin(removeButton);
        titlePanel.add(removeButton);

//        JButton moveUpButton = new JButton(UIUtils.getIconFromResources("arrow-up.png"));
//        UIUtils.makeFlatWithoutMargin(moveUpButton);
//        moveUpButton.addActionListener(e -> moveFilterUp());
//        titlePanel.add(moveUpButton);
//
//        JButton moveDownButton = new JButton(UIUtils.getIconFromResources("arrow-down.png"));
//        UIUtils.makeFlatWithoutMargin(moveDownButton);
//        moveDownButton.addActionListener(e -> moveFilterDown());
//        titlePanel.add(moveDownButton);

        add(titlePanel, BorderLayout.NORTH);
        add(contentContainer, BorderLayout.CENTER);
    }

//    private void moveFilterDown() {
//    }
//
//    private void moveFilterUp() {
//    }

    private void removeFilter() {
        if(JOptionPane.showConfirmDialog(this, "Do you really want to remove this filter?", "Remove filter",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            filter.getDatabase().removeFilter(filter);
        }
    }

    public MISAAttachmentFilter getFilter() {
        return filter;
    }

    public JPanel getContentPane() {
        return content;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setColor(getFilterColor());
        graphics.fillRect(8,8, getWidth() - 16 - 1, 8);
        graphics.setColor(BORDER_COLOR);
        graphics.drawRect(8,8,getWidth() - 1 - 16, getHeight() - 1 - 8);

    }

    private Color getFilterColor() {
        float h = Math.abs(MISAImageJRegistryService.getInstance().getAttachmentFilterUIRegistry()
                .getNameFilterName(filter.getClass()).hashCode() % 256) / 255.0f;
        return Color.getHSBColor(h, 0.8f, 0.9f);
    }
}
