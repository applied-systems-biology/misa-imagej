package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.base.Joiner;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.SQLUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ObjectBrowserTreeNode extends DefaultMutableTreeNode {

    public enum Role {
        SerializationNamespace,
        SerializationId,
        Cache,
        Sample,
        Property
    }

    private MISAAttachmentDatabase database;
    private Role[] roleAssignment;
    private String[] knownValues;
    private boolean loaded = false;

    public ObjectBrowserTreeNode(MISAAttachmentDatabase database, Role[] roleAssignment, String[] knownValues) {
        super("Root");
        this.database = database;
        this.roleAssignment = roleAssignment;
        this.knownValues = knownValues;
        setAllowsChildren(true);
        add(new DefaultMutableTreeNode("Loading ..."));
        for(int i = knownValues.length - 1; i >= 0; --i) {
            if(knownValues[i] != null) {
                setUserObject(knownValues[i]);
                break;
            }
        }
    }

    public void loadDatabaseEntries(DefaultTreeModel model) {
        if(loaded)
            return;

        int childrenRoleIndex = getFirstUnknownValue();
        ResultSet resultSet = getChildDatabaseEntries();
        removeAllChildren();
        try {
            Set<String > knownChildren = new HashSet<>();
            while(resultSet.next()) {
                String childValue = resultSet.getString(1);

                if(roleAssignment[childrenRoleIndex] == Role.SerializationNamespace) {
                    // Modify
                    childValue = childValue.substring(0, childValue.indexOf(":"));
                }

                if(knownChildren.contains(childValue))
                    continue;
                knownChildren.add(childValue);
                String[] childKnownValues = knownValues.clone();
                childKnownValues[childrenRoleIndex] = childValue;

                ObjectBrowserTreeNode childNode = new ObjectBrowserTreeNode(database, roleAssignment, childKnownValues);
                add(childNode);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        loaded = true;
        model.nodeStructureChanged(this);
    }

    public ResultSet getChildDatabaseEntries() {
        StringBuilder sql = new StringBuilder();
        List<String> filters = new ArrayList<>();

        int childrenRoleIndex = getFirstUnknownValue();

        sql.append("distinct ");

        switch (roleAssignment[childrenRoleIndex]) {
            case SerializationNamespace:
                sql.append("\"serialization-id\"");
                break;
            case SerializationId:
                sql.append("\"serialization-id\"");
                break;
            case Cache:
                sql.append("cache");
                break;
            case Sample:
                sql.append("sample");
                break;
            case Property:
                sql.append("property");
                break;
        }

        for(int i = 0; i < roleAssignment.length; ++i) {
            if(knownValues[i] != null) {
                switch (roleAssignment[i]) {
                    case SerializationNamespace:
                        filters.add("\"serialization-id\" like '" + SQLUtils.escapeWildcardsForMySQL(knownValues[i]) + "%'" );
                        break;
                    case SerializationId:
                        filters.add("\"serialization-id\" is '" + SQLUtils.escapeStringForMySQL(knownValues[i]) + "'" );
                        break;
                    case Cache:
                        filters.add("cache is '" + SQLUtils.escapeStringForMySQL(knownValues[i]) + "'" );
                        break;
                    case Sample:
                        filters.add("sample is '" + SQLUtils.escapeStringForMySQL(knownValues[i]) + "'" );
                        break;
                    case Property:
                        filters.add("property is '" + SQLUtils.escapeStringForMySQL(knownValues[i]) + "'" );
                        break;
                }
            }
        }

        return database.query(sql.toString(), filters, "");
    }

    private boolean hasUnknownValue() {
        return getFirstUnknownValue() != knownValues.length;
    }

    private int getFirstUnknownValue() {
        for(int i = 0; i < knownValues.length; ++i) {
            if(knownValues[i] == null)
                return i;
        }
        return knownValues.length;
    }

    @Override
    public boolean isLeaf() {
       return !hasUnknownValue();
    }
}
