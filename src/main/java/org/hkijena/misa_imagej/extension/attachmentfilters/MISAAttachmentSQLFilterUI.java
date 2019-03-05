package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.ui.workbench.filters.MISAAttachmentFilterUI;
import org.hkijena.misa_imagej.utils.ui.DocumentChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class MISAAttachmentSQLFilterUI extends MISAAttachmentFilterUI {
    public MISAAttachmentSQLFilterUI(MISAAttachmentFilter filter) {
        super(filter);
        initialize();
    }

    private void initialize() {
        JTextArea sqlArea = new JTextArea();
        sqlArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        sqlArea.setMinimumSize(new Dimension(100, 100));
        sqlArea.setWrapStyleWord(true);
        sqlArea.setLineWrap(true);
        sqlArea.setText(getFilter().toSQLStatement());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sqlArea, BorderLayout.CENTER);
        sqlArea.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void changed(DocumentEvent documentEvent) {
                ((MISAAttachmentSQLFilter)getFilter()).setSql(sqlArea.getText().replace('\n', ' '));
            }
        });
    }
}
