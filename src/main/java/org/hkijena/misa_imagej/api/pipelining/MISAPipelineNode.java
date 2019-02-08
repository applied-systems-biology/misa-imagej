package org.hkijena.misa_imagej.api.pipelining;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISAModuleInstance;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MISAPipelineNode {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("module-name")
    private String moduleName;

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    private transient PropertyChangeSupport propertyChangeSupport;

    public transient MISAPipeline pipeline;

    public transient MISAModuleInstance moduleInstance;

    public MISAPipelineNode() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public MISAPipelineNode(MISAPipeline pipeline) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.pipeline = pipeline;
    }

    /**
     * Finds all nodes that can be used as input for this node
     * @return
     */
    public Collection<MISAPipelineNode> getAvailableInNodes() {
        List<MISAPipelineNode> result = new ArrayList<>();
        for(MISAPipelineNode source : pipeline.getNodes()) {
            if(pipeline.canAddEdge(source, this)) {
                result.add(source);
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.moduleInstance.setName(name);
        propertyChangeSupport.firePropertyChange("name", null, null);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        propertyChangeSupport.firePropertyChange("description", null, null);
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
        propertyChangeSupport.firePropertyChange("moduleName", null, null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        propertyChangeSupport.firePropertyChange("x", null, null);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        propertyChangeSupport.firePropertyChange("y", null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
