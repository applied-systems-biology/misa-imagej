package org.hkijena.misa_imagej.api;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrapper around attached data that allows backtracking to its cache
 * The attachment is lazily loaded on demand
 */
public class MISAAttachment {
    private MISAAttachmentDatabase database;
    private int databaseIndex;
    boolean isLoaded;
    private EventBus eventBus = new EventBus();
    private List<Property> properties = new ArrayList<>();
    private JSONSchemaObject schema = null;
    private String attachmentFullPath;

    public MISAAttachment(MISAAttachmentDatabase database, int databaseIndex, String attachmentFullPath) {
        this.database = database;
        this.databaseIndex = databaseIndex;
        this.attachmentFullPath = attachmentFullPath;
    }

    public MISAAttachmentDatabase getDatabase() {
        return database;
    }

    public boolean hasData() {
        return isLoaded;
    }

    public String getDocumentationTitle() {
        if(schema != null) {
            return schema.getDocumentationTitle();
        }
        else {
            return "Unknown";
        }
    }

    public Color toColor() {
        if(schema != null) {
            return schema.toColor();
        }
        else {
            return Color.GRAY;
        }
    }

    public void load() {
        if (!isLoaded) {
            JsonObject object = database.queryJsonDataAt(databaseIndex).getAsJsonObject();

            if (object.has("misa:serialization-id")) {
                String serializationId = object.get("misa:serialization-id").getAsString();
                schema = database.getMisaOutput().getAttachmentSchemas().getOrDefault(serializationId, null);
            }

            loadProperties(object, schema, "");
            isLoaded = true;
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private void loadProperties(JsonObject object, JSONSchemaObject schema, String subPath) {
        Stack<JsonElement> elements = new Stack<>();
        Stack<String> paths = new Stack<>();
        Stack<JSONSchemaObject> schemas = new Stack<>();
        elements.push(object);
        paths.push(subPath);
        schemas.push(schema);

        while(!elements.isEmpty()) {
            JsonElement top = elements.pop();
            String topPath = paths.pop();
            JSONSchemaObject topSchema = schemas.pop();

            if(top.isJsonPrimitive()) {
                properties.add(new MemoryProperty(topPath, top.getAsJsonPrimitive(), topSchema));
            }
            else if(top.isJsonObject()) {
                if(top.getAsJsonObject().has("misa-analyzer:database-index")) {
                    int dbId = top.getAsJsonObject().get("misa-analyzer:database-index").getAsInt();
                    properties.add(new LazyProperty(this, topPath, dbId));
                }
                else {
                    for(Map.Entry<String, JsonElement> entry : top.getAsJsonObject().entrySet()) {
                        if(entry.getKey().equals("misa:serialization-id") || entry.getKey().equals("misa:serialization-hierarchy"))
                            continue;

                        elements.push(entry.getValue());
                        paths.push(topPath + "/" + entry.getKey());

                        if(schema != null && schema.hasPropertyFromPath(entry.getKey())) {
                            schemas.push(schema.getPropertyFromPath(entry.getKey()));
                        }
                        else {
                            schemas.push(null);
                        }
                    }
                }
            }
        }

        getEventBus().post(new MISAAttachment.DataLoadedEvent(this));
    }

    public List<Property> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public List<Property> getUnloadedProperties() {
        return properties.stream().filter(property -> !property.hasValue()).collect(Collectors.toList());
    }

    public void loadAll() {
        load();
        Optional<Property> property;
        while((property = properties.stream().filter(p -> !p.hasValue()).findFirst()).isPresent()) {
            property.get().loadValue();
        }
    }

    public String getAttachmentFullPath() {
        return attachmentFullPath;
    }

    public interface Property {
        String getPath();
        JSONSchemaObject getSchema();
        void loadValue();
        boolean hasValue();
    }

    public static class MemoryProperty implements Property {
        private String path;
        private JsonPrimitive value;
        private JSONSchemaObject schema;

        public MemoryProperty(String path, JsonPrimitive value, JSONSchemaObject schema) {
            this.path = path;
            this.value = value;
            this.schema = schema;
        }

        @Override
        public String getPath() {
            return path;
        }

        public JsonPrimitive getValue() {
            return value;
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public JSONSchemaObject getSchema() {
            return schema;
        }

        @Override
        public void loadValue() {

        }
    }

    public static class LazyProperty implements Property {
        private String path;
        private boolean isLoaded;
        private JSONSchemaObject schema;
        private MISAAttachment parent;
        private int databaseIndex;

        public LazyProperty(MISAAttachment parent, String path, int databaseIndex) {
            this.path = path;
            this.parent = parent;
            this.databaseIndex = databaseIndex;
        }

        public void loadValue() {
            if(!isLoaded) {
                JsonObject object = parent.database.queryJsonDataAt(databaseIndex).getAsJsonObject();

                if (object.has("misa:serialization-id")) {
                    String serializationId = object.get("misa:serialization-id").getAsString();
                    this.schema = parent.database.getMisaOutput().getAttachmentSchemas().getOrDefault(serializationId, null);
                }

                isLoaded = true;
                parent.loadProperties(object, schema, path);
            }
        }

        @Override
        public boolean hasValue() {
            return isLoaded;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public JSONSchemaObject getSchema() {
            return schema;
        }
    }

    public static class DataLoadedEvent {
        private MISAAttachment attachment;
        public DataLoadedEvent(MISAAttachment attachment) {
            this.attachment = attachment;
        }

        public MISAAttachment getAttachment() {
            return attachment;
        }
    }
}
