package org.hkijena.misa_imagej.ui.workbench;

import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Displays attachments from an attachment database
 */
public class MISAAttachmentViewer extends JPanel {

    private MISAAttachmentDatabase database;
    private int databaseId;
    private JsonObject jsonObject;

    public MISAAttachmentViewer(MISAAttachmentDatabase database, int databaseId) {
        this.database = database;
        this.databaseId = databaseId;
        initialize();
        updateContents();
    }

    private void initialize() {

    }

    public void loadFromDatabase() {
        if(jsonObject == null) {
            jsonObject = database.queryJsonDataAt(databaseId).getAsJsonObject();
        }
    }

    private void updateContents() {
        removeAll();
        if(jsonObject == null) {
            setLayout(new BorderLayout());
            JButton loadButton = new JButton("Load more data", UIUtils.getIconFromResources("database.png"));
            add(loadButton, BorderLayout.CENTER);
        }
        else {

        }
    }
}
