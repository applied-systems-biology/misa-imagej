package org.hkijena.misa_imagej.ui.components;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.ui.components.renderers.MISASampleListCellRenderer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MISASampleComboBox extends JComboBox<MISASample> {

    private MISAModuleInstance moduleInstance;

    private EventBus eventBus = new EventBus();

    public MISASampleComboBox(MISAModuleInstance moduleInstance) {
        this.moduleInstance = moduleInstance;
        this.setRenderer(new MISASampleListCellRenderer());
        this.moduleInstance.getEventBus().register(this);
        this.addItemListener(e -> {
            eventBus.post(new SelectionChangedEvent(this.getCurrentSample()));
        });
        refreshItems();
    }

    @Subscribe
    public void handleSampleListChangedEvent(MISAModuleInstance.AddedSampleEvent event) {
        refreshItems();
    }

    @Subscribe
    public void handleSampleListChangedEvent(MISAModuleInstance.RemovedSampleEvent event) {
        refreshItems();
    }

    @Subscribe
    public void handleSampleListChangedEvent(MISAModuleInstance.RenamedSampleEvent event) {
        refreshItems();
    }

    private void refreshItems() {
        if(moduleInstance.getSamples().isEmpty()) {
            this.setModel(new DefaultComboBoxModel<>());
            this.setEnabled(false);
            this.setSelectedItem(null);
            eventBus.post(new SelectionChangedEvent(null));
        }
        else {
            MISASample current = getSelectedItem() instanceof MISASample ? (MISASample)getSelectedItem() : null;
            DefaultComboBoxModel<MISASample> model = new DefaultComboBoxModel<>();
            List<MISASample> sampleList = new ArrayList<>(moduleInstance.getSamples().values());
            sampleList.sort(Comparator.comparing(MISASample::getName));
            for(MISASample sample : sampleList) {
                model.addElement(sample);
            }
            this.setModel(model);
            this.setEnabled(true);
            this.setSelectedItem(null);
            if(current != null && sampleList.contains(current))
                this.setSelectedItem(current);
            else if (model.getSize() > 0)
                this.setSelectedItem(model.getElementAt(0));
            else
                this.setSelectedItem(null);
            eventBus.post(new SelectionChangedEvent(this.getCurrentSample()));
        }
    }

    public MISASample getCurrentSample() {
        return getSelectedItem() instanceof MISASample ? ((MISASample)getSelectedItem()) : null;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static class SelectionChangedEvent {
        private MISASample sample;

        public SelectionChangedEvent(MISASample sample) {
            this.sample = sample;
        }

        public MISASample getSample() {
            return sample;
        }
    }
}
