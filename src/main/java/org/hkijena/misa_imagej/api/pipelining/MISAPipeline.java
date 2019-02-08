package org.hkijena.misa_imagej.api.pipelining;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.*;
import org.hkijena.misa_imagej.api.datasources.MISAPipelineNodeDataSource;
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
        String id = generateNodeName(module);
        MISAPipelineNode node = new MISAPipelineNode(this);
        node.id = id;
        node.moduleInstance = module.instantiate();
        node.setModuleName(module.getModuleInfo().getName());
        node.setName(module.getModuleInfo().getDescription());
        nodes.put(id, node);
        propertyChangeSupport.firePropertyChange("addNode", null, node);
        node.moduleInstance.addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("samples")) {
                synchronizeSamples();
                updateCacheDataSources();
            }
        });
        synchronizeSamples();
        return node;
    }

    /**
     * Removes the node
     * @param node
     */
    public void removeNode(MISAPipelineNode node) {
        isolateNode(node);
        nodes.remove(node.id);
        propertyChangeSupport.firePropertyChange("removeNode", null, node);
    }

    /**
     * Removes all connections from and to this node
     * @param node
     */
    public void isolateNode(MISAPipelineNode node) {
        propertyChangeSupport.firePropertyChange("isolateNode", null, node);
        List<Map.Entry<MISAPipelineNode, MISAPipelineNode>> toRemove = new ArrayList<>();

        for(Map.Entry<MISAPipelineNode, Set<MISAPipelineNode>> kv : edges.entrySet()) {
            if(kv.getKey() == node) {
                for(MISAPipelineNode target : edges.get(node)) {
                    toRemove.add(Maps.immutableEntry(node, target));
                }
            }
            else {
                for(MISAPipelineNode target : kv.getValue()) {
                    if(target == node)
                        toRemove.add(Maps.immutableEntry(target, node));
                }
            }
        }

        for(Map.Entry<MISAPipelineNode, MISAPipelineNode> rem : toRemove) {
            removeEdge(rem.getKey(), rem.getValue());
        }
    }

    public boolean canAddEdge(MISAPipelineNode source, MISAPipelineNode target) {
        if(source == target)
            return false;
        if(edges.containsKey(source) && edges.get(source).contains(target))
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

        updateCacheDataSources();
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
        boolean result = edges.get(source).remove(target);
        updateCacheDataSources();
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
    public MISAValidityReport getValidityReport() {
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

    /**
     * Returns true if there is an edge
     * @param source
     * @param target
     * @return
     */
    public boolean hasEdge(MISAPipelineNode source, MISAPipelineNode target) {
        if(edges.containsKey(source)) {
            return edges.get(source).contains(target);
        }
        return false;
    }

    /**
     * Synchronizes samples across all modules
     */
    private void synchronizeSamples() {
        Set<String> sampleNames = new HashSet<>();
        for(MISAPipelineNode node : nodes.values()) {
            for(MISASample sample : node.moduleInstance.getSamples()) {
                sampleNames.add(sample.name);
            }
        }
        for(MISAPipelineNode node : nodes.values()) {
            for(String sample : sampleNames) {
                if(!node.moduleInstance.getSampleNames().contains(sample)) {
                    node.moduleInstance.addSample(sample);
                }
            }
        }
    }

    /**
     * Updates the data sources of the caches
     */
    private void updateCacheDataSources() {
        for(Map.Entry<MISAPipelineNode, Set<MISAPipelineNode>> kv : edges.entrySet()) {
            MISAPipelineNode source = kv.getKey();
            for(MISAPipelineNode target : kv.getValue()) {
                for(MISASample sample : target.moduleInstance.getSamples()) {
                    for(MISACache cache : sample.getImportedCaches()) {
                        // Add any missing data source to the cache
                        if(cache.getAvailableDataSources().stream().noneMatch(misaDataSource -> {
                            if(misaDataSource instanceof MISAPipelineNodeDataSource) {
                                return ((MISAPipelineNodeDataSource)misaDataSource).getSourceNode() == source;
                            }
                            return false;
                        })) {
                            cache.addAvailableDataSource(new MISAPipelineNodeDataSource(cache, source));
                        }
                    }
                }
            }
        }
        List<MISADataSource> toRemove = new ArrayList<>();
        for(MISAPipelineNode target : nodes.values()) {
            for(MISASample sample : target.moduleInstance.getSamples()) {
                for (MISACache cache : sample.getImportedCaches()) {
                    toRemove.clear();
                    for(MISADataSource dataSource : cache.getAvailableDataSources()) {
                        if(dataSource instanceof MISAPipelineNodeDataSource) {
                            MISAPipelineNodeDataSource d = (MISAPipelineNodeDataSource)dataSource;
                            if(!hasEdge(d.getSourceNode(), target))
                                toRemove.add(dataSource);
                        }
                    }
                    for(MISADataSource dataSource : toRemove) {
                        cache.removeAvailableDataSource(dataSource);
                    }
                }
            }
        }
    }
}
