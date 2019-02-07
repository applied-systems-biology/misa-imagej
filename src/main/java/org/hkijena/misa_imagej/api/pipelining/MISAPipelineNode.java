package org.hkijena.misa_imagej.api.pipelining;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.api.MISAModuleInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MISAPipelineNode {

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("module-name")
    public String moduleName;

    @SerializedName("x")
    public int x;

    @SerializedName("y")
    public int y;

    public transient MISAPipeline pipeline;

    public transient MISAModuleInstance moduleInstance;

    public MISAPipelineNode() {

    }

    public MISAPipelineNode(MISAPipeline pipeline) {
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

}
