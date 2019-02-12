package org.hkijena.misa_imagej.api.pipelining;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import org.hkijena.misa_imagej.api.*;
import org.hkijena.misa_imagej.api.datasources.MISAPipelineNodeDataSource;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.utils.FilesystemUtils;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.OSUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MISAPipeline implements MISAValidatable {

    private transient Set<MISAPipelineNode> nodes = new HashSet<>();

    /**
     * Edges from source -> target
     */
    private transient Map<MISAPipelineNode, Set<MISAPipelineNode>> edges = new HashMap<>();

    /**
     * Edges from target -> source
     */
    private transient Map<MISAPipelineNode, Set<MISAPipelineNode>> edgesInverted = new HashMap<>();

    private transient PropertyChangeSupport propertyChangeSupport;

    private transient Set<String> samples = new HashSet<>();

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

        if(edgesInverted.containsKey(target))
            edgesInverted.get(target).add(source);
        else
            edgesInverted.put(target, new HashSet<>(Arrays.asList(source)));

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
        MISAValidityReport report = new MISAValidityReport();
        for(MISAPipelineNode node : nodes) {
            report.merge(node.getValidityReport(), "Node " + node.getName());
        }
        return report;
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
        for(MISAPipelineNode node : nodes) {
            for(MISASample sample : node.getModuleInstance().getSamples()) {
                samples.add(sample.name);
            }
        }
        for(MISAPipelineNode node : nodes) {
            for(String sample : samples) {
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

    /**
     * Ensures that pipeline nodes Ids are set and unique
     */
    public Map<String, MISAPipelineNode> getIdNodeMap() {
        Map<String, MISAPipelineNode> nodeMap = new HashMap<>();
        for(MISAPipelineNode node : nodes) {
            if(node.getId() == null || node.getId().isEmpty() || nodeMap.containsKey(node.getId())) {
                String id = node.getId();
                if(id == null || id.isEmpty()) {
                    if(node.getName() == null || node.getName().isEmpty()) {
                        id = node.getModuleInstance().getModuleInfo().getName().toLowerCase().replace(' ', '-');
                    }
                    else {
                        id = node.getName().toLowerCase().replace(' ', '-');
                    }
                }
                if(!nodeMap.containsKey(id)) {
                    node.setId(id);
                    nodeMap.put(id, node);
                }
                else {
                    int counter = 1;
                    while(nodeMap.containsKey(id + "-" + counter)) {
                        ++counter;
                    }
                    node.setId(id + "-" + counter);
                    nodeMap.put(id + "-" + counter, node);
                }
            }
            else {
                nodeMap.put(node.getId(), node);
            }
        }
        return nodeMap;
    }

    /**
     * Exports the pipeline into an executable state
     * @param exportDirectory
     */
    public void export(Path exportDirectory, boolean forceCopy, boolean relativeDirectories) throws IOException {
        Files.createDirectories(exportDirectory);
        save(exportDirectory.resolve("pipeline.json"));

        for(Map.Entry<String, MISAPipelineNode> kv : getIdNodeMap().entrySet()) {
            Path modulePath = exportDirectory.resolve(kv.getKey());
            Files.createDirectories(modulePath);
            Files.createDirectories(modulePath.resolve("imported"));
            Files.createDirectories(modulePath.resolve("exported"));
            kv.getValue().getModuleInstance().install(modulePath.resolve("parameters.json"),
                    modulePath.resolve("imported"),
                    modulePath.resolve("exported"),
                    forceCopy,
                    relativeDirectories);
        }

        // Create runners for the current OS
        switch(OSUtils.detectOperatingSystem()) {
            case Linux:
                exportToLinux(exportDirectory);
                break;
            default:
                throw new UnsupportedOperationException("Export not supported for this operating system!");
        }
    }


    private void exportToLinux(Path exportDirectory) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(exportDirectory.resolve("run.sh").toString()))) {
            writer.write("#!/bin/bash\n");
            writer.write("# --------------------------------------\n");
            writer.write("# --- Generated by MISA++ for ImageJ ---\n");
            writer.write("# --------------------------------------\n");
            writer.write("\n");
            Set<MISAModule> usedModules = new HashSet<>();
            Map<MISAModule, String> moduleVariableMapping = new HashMap<>();
            for(MISAPipelineNode node : nodes) {
                usedModules.add(node.getModuleInstance().getModule());
            }
            for(MISAModule used : usedModules) {
                String variable = used.getModuleInfo().getName().toUpperCase().replace('-','_');
                writer.write( variable + "=\"" + used.getExecutablePath() + "\"\n");
                moduleVariableMapping.put(used, variable);
            }
            writer.write("\n");
            for(MISAPipelineNode node : traverse()) {
                writer.write("\n");
                writer.write("# -- " + node.getName() + " (" + node.getId() + ")" + "\n");
                // Link results into the caches if needed
                for(MISASample sample : node.getModuleInstance().getSamples()) {
                    for(MISACache cache : sample.getImportedCaches()) {
                        if(cache.getDataSource() instanceof MISAPipelineNodeDataSource) {
                            MISAPipelineNodeDataSource pipelineNodeDataSource = (MISAPipelineNodeDataSource)cache.getDataSource();
                            Path sourceLink = Paths.get(pipelineNodeDataSource.getSourceNode().getId())
                                    .resolve("exported")
                                    .resolve(sample.name)
                                    .resolve(pipelineNodeDataSource.getSourceCache().getRelativePath());
                            Path targetLink = Paths.get(node.getId())
                                    .resolve("imported")
                                    .resolve(sample.name)
                                    .resolve(pipelineNodeDataSource.getCache().getRelativePath());
                            writer.write("rm -rv \"$PWD/" + targetLink.toString() + "\"\n");
                            writer.write("ln -s \"$PWD/" + sourceLink.toString() + "\" \"$PWD/" + targetLink.toString() + "\"\n");
                        }
                    }
                }

                // Run the application
                writer.write("pushd \"$PWD/" + node.getId() + "\"\n");
                writer.write("$" + moduleVariableMapping.get(node.getModuleInstance().getModule()) + " --parameters parameters.json\n");
                writer.write("popd\n");
            }
        }
        FilesystemUtils.addPosixExecutionPermission(exportDirectory.resolve("run.sh"));
    }

    /**
     * Traverses the node tree(s)
     * @return
     */
    private List<MISAPipelineNode> traverse() {
        List<MISAPipelineNode> result = new ArrayList<>();
        while(result.size() != nodes.size()) {
            result.add(nodes.stream().filter(misaPipelineNode -> {
                if(result.contains(misaPipelineNode))
                    return false;

                // Can add if has no sources or all sources are already in the list
                return !edgesInverted.containsKey(misaPipelineNode) || result.containsAll(edgesInverted.get(misaPipelineNode));
            }).findFirst().get());
        }
        return result;
    }

    /**
     * Saves the pipeline structure
     * @param filename
     * @throws IOException
     */
    public void save(Path filename) throws IOException {
        Gson gson = GsonUtils.getGson();
        String json = gson.toJson(this);
        Files.write(filename, json.getBytes(Charsets.UTF_8));
    }

    public static class JSONAdapter implements JsonDeserializer<MISAPipeline>, JsonSerializer<MISAPipeline> {

        @Override
        public MISAPipeline deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            MISAPipeline result = new MISAPipeline();
            Map<String, MISAPipelineNode> nodes = new HashMap<>();
            for(Map.Entry<String, JsonElement> kv : jsonElement.getAsJsonObject().getAsJsonObject("nodes").entrySet()) {
                MISAPipelineNode node = jsonDeserializationContext.deserialize(kv.getValue(), MISAPipelineNode.class);
                node.getModuleInstance().setName(node.getName());
                result.addNode(node);
                nodes.put(kv.getKey(), node);
            }
            for(JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray("edges")) {
                JsonObject asJsonObject = element.getAsJsonObject();
                MISAPipelineNode source = nodes.get(asJsonObject.getAsJsonPrimitive("source-node").getAsString());
                MISAPipelineNode target = nodes.get(asJsonObject.getAsJsonPrimitive("target-node").getAsString());
                result.addEdge(source, target);
            }
            for(JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray("samples")) {
                result.samples.add(element.getAsString());
            }
            result.synchronizeSamples();
            result.updateCacheDataSources();

            // Assign pipeline caches
            for(JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray("edges")) {
                JsonObject asJsonObject = element.getAsJsonObject();
                // If we assign a cache, apply it
                if(asJsonObject.has("source-cache") && asJsonObject.has("target-cache")) {
                    MISAPipelineNode source = nodes.get(asJsonObject.get("source-node").getAsString());
                    MISAPipelineNode target = nodes.get(asJsonObject.get("target-node").getAsString());
                    MISASample sourceSample = source.getModuleInstance().getSample(asJsonObject.get("sample").getAsString());
                    MISASample targetSample = target.getModuleInstance().getSample(asJsonObject.get("sample").getAsString());
                    MISACache sourceCache = sourceSample.getExportedCacheByRelativePath(asJsonObject.get("source-cache").getAsString());
                    MISACache targetCache = targetSample.getImportedCacheByRelativePath(asJsonObject.get("target-cache").getAsString());
                    MISADataSource dataSource = targetCache.getAvailableDataSources().stream().filter(misaDataSource -> {
                        if(misaDataSource instanceof  MISAPipelineNodeDataSource) {
                            return ((MISAPipelineNodeDataSource)misaDataSource).getSourceNode() == source;
                        }
                        return false;
                    }).findFirst().orElse(null);
                    // Assign the data source from the selected cache
                    ((MISAPipelineNodeDataSource)dataSource).setSourceCache(sourceCache);
                    targetCache.setDataSource(dataSource);
                }
            }

            return result;
        }

        @Override
        public JsonElement serialize(MISAPipeline pipeline, Type type, JsonSerializationContext jsonSerializationContext) {
            BiMap<String, MISAPipelineNode> nodes = HashBiMap.create(pipeline.getIdNodeMap());
            Map<String, JsonElement> nodeParameters = new HashMap<>();

            for(Map.Entry<String, MISAPipelineNode> kv : nodes.entrySet()) {
                nodeParameters.put(kv.getKey(), kv.getValue().getModuleInstance().getParametersAsJson(Paths.get(""), Paths.get("")));
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
                                    edge.addProperty("target-cache", dataSource.getCache().getRelativePath());
                                    edge.addProperty("sample", sample.name);
                                    edges.add(edge);
                                }
                            }
                        }
                    }

                }
            }

            JsonObject result = new JsonObject();
            result.add("samples", jsonSerializationContext.serialize(pipeline.samples));
            result.add("nodes", jsonSerializationContext.serialize(new HashMap<>(nodes)));
            result.add("edges", jsonSerializationContext.serialize(edges));
            result.add("parameters", jsonSerializationContext.serialize(nodeParameters));

            return  result;
        }
    }
}
