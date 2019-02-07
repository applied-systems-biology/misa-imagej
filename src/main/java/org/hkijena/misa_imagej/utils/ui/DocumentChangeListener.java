package org.hkijena.misa_imagej.utils.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class DocumentChangeListener implements DocumentListener {

    public abstract void changed(DocumentEvent documentEvent);

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        changed(documentEvent);
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        changed(documentEvent);
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        changed(documentEvent);
    }
}
