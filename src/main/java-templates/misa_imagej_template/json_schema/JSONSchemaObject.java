package misa_imagej_template.json_schema;

import com.google.gson.annotations.SerializedName;
import misa_imagej_template.data.MISAData;
import misa_imagej_template.data.MISADataType;

import javax.swing.tree.DefaultMutableTreeNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * Deserialized JSON Schema entry that allows editing and exporting to the final JSON
 */
public class JSONSchemaObject implements Cloneable {

    public transient String id;

    public transient JSONSchemaObject parent = null;

    public transient MISAData filesystemData = null;

    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private transient Object value = null;

    @SerializedName("type")
    public String type;

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

    @Override
    public Object clone() {
        JSONSchemaObject obj = new JSONSchemaObject();
        obj.type = type;
        obj.title = title;
        obj.description = description;
        obj.default_value = default_value;
        obj.enum_values = enum_values;
        obj.required_properties = required_properties;
        obj.additionalProperties = additionalProperties;
        obj.properties = new HashMap<>();
        for(Map.Entry<String, JSONSchemaObject> kv : properties.entrySet()) {
            obj.properties.put(kv.getKey(), (JSONSchemaObject) kv.getValue().clone());
        }
        return obj;
    }

    /**
     * Instantiates the additional property stored in this object schema
     * @param name
     * @return
     */
    public JSONSchemaObject addAdditionalProperty(String name) {
        JSONSchemaObject obj = (JSONSchemaObject) additionalProperties.clone();
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
            case "string":
            case "number":
            case "boolean":
                return getValue();
            case "array": {
                ArrayList<Object> result = new ArrayList<>();
                for (JSONSchemaObject obj : items) {
                    result.add(obj.toValue());
                }
                return result;
            }
            case "object": {
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

    public MISADataType getFilesystemDataType() {
        if(filesystemData != null)
            return filesystemData.getType();
        return MISADataType.None;
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
        return type.equals("object") || value != null;
    }

    private void triggerStructureChangedEvent() {
        propertyChangeSupport.firePropertyChange("structure", null, null);
        if(parent != null)
            parent.triggerStructureChangedEvent();
    }
}
