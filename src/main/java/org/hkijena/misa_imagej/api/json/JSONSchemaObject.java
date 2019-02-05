package org.hkijena.misa_imagej.api.json;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISAParameter;
import org.hkijena.misa_imagej.api.MISAParameterValidity;

import javax.swing.tree.DefaultMutableTreeNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * Deserialized JSON Schema entry that allows editing and exporting to the final JSON
 */
public class JSONSchemaObject implements Cloneable, MISAParameter {

    public transient String id;

    public transient JSONSchemaObject parent = null;

    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private transient Object value = null;

    @SerializedName("type")
    public JSONSchemaObjectType type;

    @SerializedName("title")
    public String title = null;

    @SerializedName("description")
    public String description = null;

    @SerializedName("properties")
    public Map<String, JSONSchemaObject> properties = new HashMap<>();

    @SerializedName("items")
    public List<JSONSchemaObject> items = new ArrayList<>();

    @SerializedName("default")
    public Object default_value = null;

    @SerializedName("enum")
    public List<Object> enum_values = null;

    @SerializedName("required")
    public List<String> required_properties = new ArrayList<>();

    @SerializedName("additionalProperties")
    public JSONSchemaObject additionalProperties = null;

    @SerializedName("misa:serialization-id")
    public String serializationId = null;

    @SerializedName("misa:serialization-hierarchy")
    public List<String> serializationHierarchy = new ArrayList<>();

    public JSONSchemaObject() {
    }

    public JSONSchemaObject(JSONSchemaObjectType type) {
        this.type = type;
    }

    public static JSONSchemaObject createObject() {
        JSONSchemaObject result = new JSONSchemaObject(JSONSchemaObjectType.jsonObject);
        return result;
    }

    public static JSONSchemaObject createArray(List<Object> value) {
        JSONSchemaObject result = new JSONSchemaObject(JSONSchemaObjectType.jsonArray);
        result.value = value;
        return result;
    }

    public static JSONSchemaObject createNumber(double number) {
        JSONSchemaObject result = new JSONSchemaObject(JSONSchemaObjectType.jsonNumber);
        result.value = number;
        return result;
    }

    public static JSONSchemaObject createBoolean(boolean b) {
        JSONSchemaObject result = new JSONSchemaObject(JSONSchemaObjectType.jsonBoolean);
        result.value = b;
        return result;
    }

    public static JSONSchemaObject createString(String string) {
        JSONSchemaObject result = new JSONSchemaObject(JSONSchemaObjectType.jsonString);
        result.value = string;
        return result;
    }

    @Override
    public Object clone() {
        JSONSchemaObject obj = new JSONSchemaObject();
        obj.type = type;
        obj.title = title;
        obj.description = description;
        obj.default_value = default_value;
        obj.enum_values = enum_values;
        obj.value = value;
        obj.required_properties = required_properties;
        obj.additionalProperties = additionalProperties;
        obj.serializationId = serializationId;
        obj.properties = new HashMap<>();
        for(Map.Entry<String, JSONSchemaObject> kv : properties.entrySet()) {
            obj.properties.put(kv.getKey(), (JSONSchemaObject) kv.getValue().clone());
        }
        obj.serializationHierarchy = new ArrayList<>();
        obj.serializationHierarchy.addAll(serializationHierarchy);
        obj.update();
        return obj;
    }

    /**
     * Guaranteed way of returning additional properties (because this one might be null)
     * @return
     */
    public JSONSchemaObject getAdditionalPropertiesTemplate() {
        JSONSchemaObject obj;
        if(additionalProperties != null) {
            obj =  (JSONSchemaObject) additionalProperties.clone();
        }
        else {
            obj = createObject();
        }
        return obj;
    }

    /**
     * Instantiates the additional property stored in this object schema
     * @param name
     * @return
     */
    public JSONSchemaObject addAdditionalProperty(String name) {
        JSONSchemaObject obj = getAdditionalPropertiesTemplate();
        properties.put(name, obj);
        obj.id = name;
        obj.parent = this;
        obj.update();

        // Notify the change
        triggerStructureChangedEvent();

        return obj;
    }

    public void removeAdditionalProperty(String name) {
        properties.remove(name);

        // Notify the change
        triggerStructureChangedEvent();
    }

    public void update() {
        // Set the default value
        if(getValue() == null) {
            setValue(default_value);
        }

        // Update the map Ids & parent
        for(Map.Entry<String, JSONSchemaObject> kv : properties.entrySet()) {
            kv.getValue().id = kv.getKey();
            kv.getValue().parent = this;
            kv.getValue().update();
        }
    }

    /**
     * Returns the max depth of this object and its properties
     * @return
     */
    public int getMaxDepth() {
        if (properties.isEmpty())
            return 0;
        else {
            int d = 0;
            for(Map.Entry<String, JSONSchemaObject> kv : properties.entrySet()) {
                d = Math.max(d, kv.getValue().getMaxDepth() + 1);
            }
            return d;
        }
    }

    /**
     * Returns the depth of this object
     * @return
     */
    public int getDepth() {
        if(parent == null)
            return 0;
        else
            return parent.getDepth() + 1;
    }

    public String getName() {
        return (title == null || title.isEmpty()) ? id : title;
    }

    @Override
    public String toString() {
        return getName();
    }

    public DefaultMutableTreeNode toTreeNode() {
        DefaultMutableTreeNode nd = new DefaultMutableTreeNode(this);

        ArrayList<JSONSchemaObject> objects = new ArrayList<>(properties.values());
        objects.sort(Comparator.comparingInt(JSONSchemaObject::getMaxDepth));

       for(JSONSchemaObject obj : objects) {
           nd.add(obj.toTreeNode());
       }

        return nd;
    }

    public Object toValue() {
        switch (type) {
            case jsonString:
            case jsonNumber:
            case jsonBoolean:
                return getValue();
            case jsonArray: {
                ArrayList<Object> result = new ArrayList<>();
                for (JSONSchemaObject obj : items) {
                    result.add(obj.toValue());
                }
                return result;
            }
            case jsonObject: {
                HashMap<String, Object> result = new HashMap<>();
                for (Map.Entry<String, JSONSchemaObject> kv : properties.entrySet()) {
                    result.put(kv.getKey(), kv.getValue().toValue());
                }
                return result;
            }
            default:
                throw new RuntimeException("Unknown type " + type);
        }
    }

    public String getValuePath() {
        if(parent == null)
            return "";
        else
            return parent.getValuePath() + "/" + id;
    }

    /**
     * Gets a sub-property from path
     * @param path
     * @return
     */
    public JSONSchemaObject getPropertyFromPath(String... path) {
        JSONSchemaObject result = this;
        for(String v : path) {
            result = result.properties.get(v);
        }
        return result;
    }

    public boolean hasPropertyFromPath(String... path) {
        JSONSchemaObject current = this;
        for(String v : path) {
            if(current.properties.containsKey(v))
                current = current.properties.get(v);
            else {
                return false;
            }
        }
        return true;
    }

    public JSONSchemaObject addProperty(String key, JSONSchemaObject property) {
        JSONSchemaObject copy = (JSONSchemaObject)property.clone();
        properties.put(key, copy);
        update();
        return copy;
    }

    public JSONSchemaObject ensurePropertyFromPath(String... path) {
        JSONSchemaObject current = this;
        for(String v : path) {
            if(current.properties.containsKey(v))
                current = current.properties.get(v);
            else
                current = current.addProperty(v, JSONSchemaObject.createObject());
        }
        return current;
    }

    private void flatten_(List<JSONSchemaObject> result) {
        result.add(this);
        for(Map.Entry<String, JSONSchemaObject> kv : properties.entrySet()) {
            kv.getValue().flatten_(result);
        }
    }

    public List<JSONSchemaObject> flatten() {
        ArrayList<JSONSchemaObject> result = new ArrayList<>();
        flatten_(result);
        return result;
    }

    public void addPropertyChangeListener( PropertyChangeListener l )
    {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l )
    {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        Object old = this.value;
        this.value = value;
        propertyChangeSupport.firePropertyChange("value", old, value);
    }

    public boolean hasValue() {
        return type == JSONSchemaObjectType.jsonObject || value != null;
    }

    private void triggerStructureChangedEvent() {
        propertyChangeSupport.firePropertyChange("structure", null, null);
        if(parent != null)
            parent.triggerStructureChangedEvent();
    }

    @Override
    public MISAParameterValidity isValidParameter() {
        MISAParameterValidity report = new MISAParameterValidity();
        if(hasValue())
            report.report(this, null, true, "");
        else
            report.report(this, null, false, "Value is not set");

        if(properties != null) {
            for(JSONSchemaObject object : properties.values()) {
                report.merge(object.isValidParameter(), object.id == null ? "" : object.id);
            }
        }
        if(items != null) {
            for(int i = 0; i < items.size(); ++i) {
                report.merge(items.get(i).isValidParameter(), "[" + i + "]");
            }
        }

        return report;
    }

    public void setValueFromJson(JsonElement json) {
        if(json.isJsonNull())
            return;
        switch(type) {
            case jsonBoolean:
                setValue(json.getAsBoolean());
                break;
            case jsonNumber:
                setValue(json.getAsDouble());
                break;
            case jsonString:
                setValue(json.getAsString());
                break;
            case jsonArray:
                setValue(json.getAsJsonArray());
                break;
            case jsonObject:
                for(Map.Entry<String, JsonElement> kv : json.getAsJsonObject().entrySet()) {
                    ensurePropertyFromPath(kv.getKey()).setValueFromJson(kv.getValue());
                }
                break;
        }
    }
}
