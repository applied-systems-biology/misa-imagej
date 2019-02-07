package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;
import org.hkijena.misa_imagej.ui.parametereditor.MISAModuleInstanceUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentChangeListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MISAPipelineNodeUI extends JPanel implements ComponentListener {

    private MISAPipelineNode node;
    private static final Color BORDER_COLOR = new Color(128,128,128);

    public MISAPipelineNodeUI(MISAPipelineNode node) {
        super(new BorderLayout());
        this.node = node;
        initialize();
    }

    private void initialize() {
        addComponentListener(this);

        JPanel padding = new JPanel(new BorderLayout(8,8));
        padding.setOpaque(false);
        padding.setBorder(BorderFactory.createEmptyBorder(16,8,8,8));

        // Create name editor
        JTextField nameEditor = new JTextField(node.name) {
            @Override
            public void setBorder(Border border) {
                // No border
            }
        };
        nameEditor.setBackground(this.getBackground());
        nameEditor.setFont(nameEditor.getFont().deriveFont(14.0f));
        nameEditor.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void changed(DocumentEvent documentEvent) {
                node.name = nameEditor.getText();
            }
        });
        padding.add(nameEditor, BorderLayout.NORTH);

        // Create description editor
        JTextArea descriptionEditor = new JTextArea(node.description);
        descriptionEditor.setBackground(getBackground());
        descriptionEditor.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void changed(DocumentEvent documentEvent) {
                node.description = descriptionEditor.getText();
            }
        });
        padding.add(descriptionEditor, BorderLayout.CENTER);

        // Create the UI for functions
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        buttonPanel.add(Box.createHorizontalGlue());

        JButton connectButton = new JButton(UIUtils.getIconFromResources("connect.png"));
        connectButton.setToolTipText("Connect to another node");
        initializeConnectMenu(UIUtils.addPopupMenuToComponent(connectButton));
        UIUtils.makeFlat(connectButton);
        buttonPanel.add(connectButton);

        JButton editParametersButton = new JButton(UIUtils.getIconFromResources("edit.png"));
        editParametersButton.setToolTipText("Edit parameters");
        editParametersButton.addActionListener(actionEvent -> editParameters());
        UIUtils.makeFlat(editParametersButton);
        buttonPanel.add(editParametersButton);

        padding.add(buttonPanel, BorderLayout.SOUTH);
        add(padding, BorderLayout.CENTER);
    }

    private void initializeConnectMenu(JPopupMenu menu) {

    }

    private void editParameters() {
        MISAModuleInstanceUI editor = new MISAModuleInstanceUI(node.moduleInstance, true);
        editor.setTitle("MISA++ pipeline tool - Parameters for " + node.name);
        editor.setVisible(true);

        // Java separates between JFrame and JDialog
        // Both solutions are not good for out use case
        // Workaround: Disable parent frame
        JFrame pipeliner = (JFrame)SwingUtilities.getWindowAncestor(this);
        editor.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {

            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {

            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {
                pipeliner.setEnabled(true);
            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {

            }
        });
        pipeliner.setEnabled(false);
    }

    public MISAPipelineNode getNode() {
        return node;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setColor(node.moduleInstance.getModuleInfo().toColor());
        graphics.fillRect(0,0, getWidth() - 1, 8);
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
