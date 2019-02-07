package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;

import javax.swing.*;

public class MISAPipelineNodeUI extends JPanel {

    private MISAPipelineNode node;

    public MISAPipelineNodeUI(MISAPipelineNode node) {
        this.node = node;
    }

    public MISAPipelineNode getNode() {
        return node;
    }
}
