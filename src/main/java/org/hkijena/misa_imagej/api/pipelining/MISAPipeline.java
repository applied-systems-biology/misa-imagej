package org.hkijena.misa_imagej.api.pipelining;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISAParameterValidity;
import org.hkijena.misa_imagej.api.MISAValidatable;
import org.hkijena.misa_imagej.api.repository.MISAModule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

public class MISAPipeline implements MISAValidatable {

    @SerializedName("nodes")
    private Map<String, MISAPipelineNode> nodes = new HashMap<>();

    private transient Map<MISAPipelineNode, Set<MISAPipelineNode>> edges = new HashMap<>();

    private transient PropertyChangeSupport propertyChangeSupport;

    public MISAPipeline() {
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Adds a new node into the pipeline
     * @param module
     * @return
     */
    public MISAPipelineNode addNode(MISAModule module) {
        MISAPipelineNode node = new MISAPipelineNode(this);
        node.moduleInstance = module.instantiate();
        node.setModuleName(module.getModuleInfo().getName());
        node.setName(module.getModuleInfo().getDescription());
        nodes.put(generateNodeName(module), node);
        propertyChangeSupport.firePropertyChange("addNode", null, node);
        return node;
    }

    public boolean canAddEdge(MISAPipelineNode source, MISAPipelineNode target) {
        if(source == target)
            return false;
        if(edges.containsKey(target)) {
            return !edges.get(target).contains(source);
        }
        return true;
    }

    /**
     * Adds an edge between two nodes, allowing access to data sources
     *
     * @param source
     * @param target
     * @return If the edge was added
     */
    public boolean addEdge(MISAPipelineNode source, MISAPipelineNode target) {
        if(!canAddEdge(source, target))
            return false;

        if(edges.containsKey(source))
            edges.get(source).add(target);
        else
            edges.put(source, new HashSet<>(Arrays.asList(target)));
        // TODO: Update module instances accordingly
        propertyChangeSupport.firePropertyChange("addEdge", null, null);
        return true;
    }

    /**
     * Removes an edge between two nodes
     * @param source
     * @param target
     * @return
     */
    public boolean removeEdge(MISAPipelineNode source, MISAPipelineNode target) {
        if(!edges.containsKey(source))
            return false;
        // TODO: Update module instances accordingly
        boolean result = edges.get(source).remove(target);
        propertyChangeSupport.firePropertyChange("removeEdge", null, null);
        return result;
    }

    private String generateNodeName(MISAModule module) {
        String prefix = module.getModuleInfo().getName();
        int counter = 1;
        while(nodes.containsKey(prefix + "-" + counter)) {
            ++counter;
        }
        return prefix + "-" + counter;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public Collection<MISAPipelineNode> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    @Override
    public MISAParameterValidity isValidParameter() {
        return null;
    }

    /**
     * Returns the edges between nodes
     * The map key is the source and the list of nodes are the targets
     * @return
     */
    public Map<MISAPipelineNode, Set<MISAPipelineNode>> getEdges() {
        return Collections.unmodifiableMap(edges);
    }
}
