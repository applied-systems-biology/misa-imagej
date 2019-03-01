package org.hkijena.misa_imagej.utils.ui;

import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DocumentTabPane extends JTabbedPane {

    public enum CloseMode {
        withSilentCloseButton,
        withAskOnCloseButton,
        withoutCloseButton,
        withDisabledCloseButton
    }

    /**
     * Contains tabs that can be closed, but opened again
     */
    private Map<String, DocumentTab> singletonTabs = new HashMap<>();

    public DocumentTabPane() {
        super(JTabbedPane.TOP);
    }

    /**
     * Adds a document tab
     * @param title
     * @param icon
     * @param component
     * @param closeMode
     * @return The tab component
     */
    public DocumentTab addTab(String title, Icon icon, Component component, CloseMode closeMode) {
        // Create tab panel
        JPanel tabPanel = new JPanel();
        tabPanel.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));
        tabPanel.setOpaque(false);
        tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.LINE_AXIS));
        tabPanel.add(new JLabel(title, icon, JLabel.LEFT));
        if(closeMode != CloseMode.withoutCloseButton) {
            JButton closeButton = new JButton(UIUtils.getIconFromResources("remove.png"));
            closeButton.setBorder(null);
            closeButton.setBackground(Color.WHITE);
            closeButton.setOpaque(false);
            closeButton.setEnabled(closeMode != CloseMode.withDisabledCloseButton);
            closeButton.addActionListener(e -> remove(component));
            tabPanel.add(Box.createHorizontalStrut(8));
            tabPanel.add(closeButton);
        }

        DocumentTab tab = new DocumentTab(title, icon, tabPanel, component);
        addTab(tab);
        return tab;
    }

    /**
     * Adds a tab that can be silently closed and brought up again
     * @param title
     * @param icon
     * @param component
     */
    public void addSingletonTab(String id, String title, Icon icon, Component component, boolean hidden) {
        DocumentTab tab = addTab(title, icon, component, CloseMode.withSilentCloseButton);
        singletonTabs.put(id, tab);
        if(hidden) {
            remove(tab.getContent());
        }
    }

    /**
     * Re-opens or selects a singleton tab
     */
    public void selectSingletonTab(String id) {
        DocumentTab tab = singletonTabs.get(id);
        for(int i = 0; i < getTabCount(); ++i) {
            if(getTabComponentAt(i) == tab.getTabComponent()) {
                setSelectedComponent(tab.getContent());
                return;
            }
        }

        // Was closed; reinstantiate the component
        addTab(tab);
        setSelectedIndex(getTabCount() - 1);
    }

    private void addTab(DocumentTab tab) {
        addTab(tab.getTitle(), tab.getIcon(), tab.getContent());
        setTabComponentAt(getTabCount() - 1, tab.getTabComponent());
    }

    public static class DocumentTab {
        private String title;
        private Icon icon;
        private Component tabComponent;
        private Component content;

        private DocumentTab(String title, Icon icon, Component tabComponent, Component content) {
            this.title = title;
            this.icon = icon;
            this.tabComponent = tabComponent;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public Icon getIcon() {
            return icon;
        }

        public Component getTabComponent() {
            return tabComponent;
        }

        public Component getContent() {
            return content;
        }
    }

}
