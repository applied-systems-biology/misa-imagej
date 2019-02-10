package org.hkijena.misa_imagej.api.pipelining;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import org.hkijena.misa_imagej.api.*;
import org.hkijena.misa_imagej.api.datasources.MISAPipelineNodeDataSource;
import org.hkijena.misa_imagej.api.repository.MISAModule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;
import java.util.*;

public class MISAPipeline implements MISAValidatable {

    private transient Set<MISAPipelineNode> nodes = new HashSet<>();

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
        node.setModuleName(module.getModuleInfo().getName());
        node.setName(module.getModuleInfo().getDescription());
        node.setPipeline(this);
        addNode(node);
        return node;
    }

    private void addNode(MISAPipelineNode node) {
        node.setPipeline(this);
        nodes.add(node);
        propertyChangeSupport.firePropertyChange("addNode", null, node);
        node.getModuleInstance().addPropertyChangeListener(propertyChangeEvent -> {
            if(propertyChangeEvent.getPropertyName().equals("samples")) {
                synchronizeSamples();
                updateCacheDataSources();
            }
        });
        synchronizeSamples();
    }

    /**
     * Removes the node
     * @param node
     */
    public void removeNode(MISAPipelineNode node) {
        isolateNode(node);
        nodes.remove(node);
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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public Collection<MISAPipelineNode> getNodes() {
        return Collections.unmodifiableCollection(nodes);
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
        for(MISAPipelineNode node : nodes) {
            for(MISASample sample : node.getModuleInstance().getSamples()) {
                sampleNames.add(sample.name);
            }
        }
        for(MISAPipelineNode node : nodes) {
            for(String sample : sampleNames) {
                if(!node.getModuleInstance().getSampleNames().contains(sample)) {
                    node.getModuleInstance().addSample(sample);
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
                for(MISASample sample : target.getModuleInstance().getSamples()) {
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
        for(MISAPipelineNode target : nodes) {
            for(MISASample sample : target.getModuleInstance().getSamples()) {
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

    public static class JSONAdapter implements JsonDeserializer<MISAPipeline>, JsonSerializer<MISAPipeline> {

        @Override
        public MISAPipeline deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            MISAPipeline result = new MISAPipeline();
            Map<String, MISAPipelineNode> nodes = new HashMap<>();
            for(Map.Entry<String, JsonElement> kv : jsonElement.getAsJsonObject().getAsJsonObject("nodes").entrySet()) {
                MISAPipelineNode node = jsonDeserializationContext.deserialize(kv.getValue(), MISAPipelineNode.class);
                result.addNode(node);
                nodes.put(kv.getKey(), node);
            }
            for(JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray("edges")) {
                MISAPipelineNode source = nodes.get(element.getAsJsonObject().getAsJsonPrimitive("source-node").getAsString());
                MISAPipelineNode target = nodes.get(element.getAsJsonObject().getAsJsonPrimitive("target-node").getAsString());
                result.addEdge(source, target);
            }
            result.synchronizeSamples();
            result.updateCacheDataSources();
            return result;
        }

        @Override
        public JsonElement serialize(MISAPipeline pipeline, Type type, JsonSerializationContext jsonSerializationContext) {
            BiMap<String, MISAPipelineNode> nodes = HashBiMap.create();
            for(MISAPipelineNode node : pipeline.nodes) {
                nodes.put(generateNodeName(nodes, node.getModuleInstance().getModule()), node);
            }

            List<JsonObject> edges = new ArrayList<>();

            // The output edge list directly contains cache -> cache edges
            for(Map.Entry<MISAPipelineNode, Set<MISAPipelineNode>> kv : pipeline.edges.entrySet()) {
                MISAPipelineNode source = kv.getKey();
                for(MISAPipelineNode target : kv.getValue()) {

                    // Also track the module-edges
                    {
                        JsonObject edge = new JsonObject();
                        edge.addProperty("source-node", nodes.inverse().get(source));
                        edge.addProperty("target-node", nodes.inverse().get(target));
                        edges.add(edge);
                    }

                    for(MISASample sample : target.getModuleInstance().getSamples()) {
                        for(MISACache cache : sample.getImportedCaches()) {
                            if(cache.getDataSource() instanceof MISAPipelineNodeDataSource) {
                                MISAPipelineNodeDataSource dataSource = (MISAPipelineNodeDataSource)cache.getDataSource();
                                if(dataSource.getSourceNode() == source) {
                                    JsonObject edge = new JsonObject();
                                    edge.addProperty("source-node", nodes.inverse().get(source));
                                    edge.addProperty("target-node", nodes.inverse().get(target));
                                    edge.addProperty("source-cache", dataSource.getSourceCache().getRelativePath());
                                    edge.addProperty("sample", sample.name);
                                    edges.add(edge);
                                }
                            }
                        }
                    }

                }
            }

            JsonObject result = new JsonObject();
            result.add("nodes", jsonSerializationContext.serialize(new HashMap<>(nodes)));
            result.add("edges", jsonSerializationContext.serialize(edges));

            return  result;
        }

        private static String generateNodeName(Map<String, MISAPipelineNode> nodes, MISAModule module) {
            String prefix = module.getModuleInfo().getName();
            int counter = 1;
            while(nodes.containsKey(prefix + "-" + counter)) {
                ++counter;
            }
            return prefix + "-" + counter;
        }
    }
}
