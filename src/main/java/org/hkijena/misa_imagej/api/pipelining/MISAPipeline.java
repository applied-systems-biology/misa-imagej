package org.hkijena.misa_imagej.api.pipelining;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.repository.MISAModule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MISAPipeline {

    @SerializedName("nodes")
    private List<MISAPipelineNode> nodes = new ArrayList<>();

    private transient PropertyChangeSupport propertyChangeSupport;

    public MISAPipeline() {
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public MISAPipelineNode addInstance(MISAModule module) {
        MISAPipelineNode node = new MISAPipelineNode();
        nodes.add(node);
        propertyChangeSupport.firePropertyChange("addInstance", null, node);
        return node;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public List<MISAPipelineNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }
}
