package org.hkijena.misa_imagej.api.workbench.table;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MISAAttachmentTable {
    private MISAAttachmentDatabase database;
    private int[] databaseIds;
    private List<MISAAttachmentTableColumn> columns = new ArrayList<>();
    private EventBus eventBus = new EventBus();

    private int currentId;
    private Object[] rowBuffer;
    private String sqlSelectionString;

    public MISAAttachmentTable(MISAAttachmentDatabase database, int[] databaseIds) {
        this.database = database;
        this.databaseIds = databaseIds;
    }

    public Object[] nextRow() {

    }

    private void updateSqlSelectionString() {
        StringBuilder sqlSelection = new StringBuilder();
        for(int i = 0; i < columns.size(); ++i) {
            
        }
    }
}
