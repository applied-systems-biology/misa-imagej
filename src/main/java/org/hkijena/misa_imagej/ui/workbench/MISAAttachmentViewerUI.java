package org.hkijena.misa_imagej.ui.workbench;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jfree.data.json.impl.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Displays attachments from an attachment database
 */
public class MISAAttachmentViewerUI extends JPanel {

    private MISAAttachmentDatabase database;
    private int databaseId;
    private JsonObject jsonObject;
    private JSONSchemaObject schemaObject;

    private static final int COLUMN_LABEL = 0;
    private static final int COLUMN_CONTENT = 1;

    public MISAAttachmentViewerUI(MISAAttachmentDatabase database, int databaseId) {
        this.database = database;
        this.databaseId = databaseId;

        setLayout(new BorderLayout());
        JButton loadButton = new JButton("Load more data", UIUtils.getIconFromResources("database.png"));
        loadButton.addActionListener(e -> loadFromDatabase());
        add(loadButton, BorderLayout.CENTER);
    }

    public MISAAttachmentViewerUI(JsonObject jsonObject, JSONSchemaObject schemaObject) {
        this.jsonObject = jsonObject;
        this.schemaObject = schemaObject;
        displayJsonContents();
    }

    public void loadFromDatabase() {
        if (jsonObject == null) {
            jsonObject = database.queryJsonDataAt(databaseId).getAsJsonObject();

            if (jsonObject.has("misa:serialization-id")) {
                String serializationId = jsonObject.get("misa:serialization-id").getAsString();
                this.schemaObject = database.getMisaOutput().getAttachmentSchemas().getOrDefault(serializationId, null);
            }

            removeAll();
            displayJsonContents();
        }
    }

    private void insertLabelFor(String propertyName, Icon icon, int row) {
        JLabel label = new JLabel(propertyName, icon, JLabel.LEFT);
        add(label, new GridBagConstraints() {
            {
                gridx = COLUMN_LABEL;
                gridy = row;
                anchor = GridBagConstraints.NORTHWEST;
            }
        });
    }

    private void insertSubUI(MISAAttachmentViewerUI ui, int row) {
        add(ui, new GridBagConstraints() {
            {
                gridx = COLUMN_LABEL;
                gridy = row;
                anchor = GridBagConstraints.NORTHWEST;
                weightx = 1;
                fill = GridBagConstraints.HORIZONTAL;
            }
        });
    }

    private void insertDisplayFor(String name, JsonElement element, final int row) {

        // Find title + description via JSON schema
        String propertyName = null;
        String propertyDescription = null;

        if (schemaObject != null && schemaObject.hasPropertyFromPath(name)) {
            propertyName = schemaObject.getPropertyFromPath(name).getDocumentationTitle();
            propertyDescription = schemaObject.getPropertyFromPath(name).getDocumentationDescription();
        }

        if (propertyName == null)
            propertyName = name;

        if (element == null) {
            // Element was inherited from JSON schema
            switch (schemaObject.getType()) {
                case jsonObject: {
                    JSONSchemaObject subSchema = null;
                    if (schemaObject != null && schemaObject.hasPropertyFromPath(name)) {
                        subSchema = schemaObject.getPropertyFromPath(name);
                    }
                    MISAAttachmentViewerUI ui = new MISAAttachmentViewerUI(null, subSchema);
                    insertSubUI(ui, row);
                }
                break;
                case jsonNumber: {
                    insertLabelFor(propertyName, UIUtils.getIconFromResources("number.png"), row);
                }
                break;
                case jsonString: {
                    insertLabelFor(propertyName, UIUtils.getIconFromResources("text.png"), row);
                }
                break;
                case jsonBoolean: {

                }
                break;
                case jsonArray:
                    break;
            }
        } else {
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive) element;
                if (primitive.isString()) {
                    insertLabelFor(propertyName, UIUtils.getIconFromResources("text.png"), row);
                } else if (primitive.isBoolean()) {

                } else if (primitive.isNumber()) {
                    insertLabelFor(propertyName, UIUtils.getIconFromResources("number.png"), row);
                }
            } else if (element.isJsonObject()) {
                insertLabelFor(propertyName, UIUtils.getIconFromResources("object.png"), row);
                JsonObject object = (JsonObject) element;
                if (object.has("misa-analyzer:database-index")) {
                    int dbIndex = object.get("misa-analyzer:database-index").getAsInt();
                    MISAAttachmentViewerUI ui = new MISAAttachmentViewerUI(database, dbIndex);
                    insertSubUI(ui, row);
                } else {
                    JSONSchemaObject subSchema = null;
                    if (schemaObject != null && schemaObject.hasPropertyFromPath(name)) {
                        subSchema = schemaObject.getPropertyFromPath(name);
                    }
                    MISAAttachmentViewerUI ui = new MISAAttachmentViewerUI(object, subSchema);
                    insertSubUI(ui, row);
                }
            } else if (element.isJsonArray()) {

            }
        }
    }

    private void displayJsonContents() {
        setLayout(new GridBagLayout());

        Set<String> propertySet = new HashSet<>();
        if (jsonObject != null) {
            propertySet.addAll(jsonObject.keySet());
        }
        if (schemaObject != null) {
            propertySet.addAll(schemaObject.getProperties().keySet());
        }
        propertySet.remove("misa:serialization-id");
        propertySet.remove("misa:serialization-hierarchy");

        List<String> properties = new ArrayList<>(propertySet);
        properties.sort(String::compareTo);

        for (int i = 0; i < properties.size(); ++i) {
            if (jsonObject.has(properties.get(i)))
                insertDisplayFor(properties.get(i), jsonObject.get(properties.get(i)), i);
            else
                insertDisplayFor(properties.get(i), null, i);
        }

        revalidate();
        repaint();
    }
}
