package org.hkijena.misa_imagej.api.workbench.table;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.utils.GsonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MISAAttachmentTable {
    private MISAAttachmentDatabase database;
    private int[] databaseIds;
    private List<MISAAttachmentTableColumn> columns = new ArrayList<>();
    private EventBus eventBus = new EventBus();

    public MISAAttachmentTable(MISAAttachmentDatabase database, int[] databaseIds) {
        this.database = database;
        this.databaseIds = databaseIds;
    }

    public void addColumn(MISAAttachmentTableColumn column) {
        columns.add(column);
        eventBus.post(new ColumnsChangedEvent(this));
    }

    public void removeColumn(MISAAttachmentTableColumn column) {
        columns.remove(column);
        eventBus.post(new ColumnsChangedEvent(this));
    }

    public static class ColumnsChangedEvent {
        private MISAAttachmentTable table;

        public ColumnsChangedEvent(MISAAttachmentTable table) {
            this.table = table;
        }

        public MISAAttachmentTable getTable() {
            return table;
        }
    }

    public static class Iterator {
        private List<MISAAttachmentTableColumn> columns;
        private ResultSet resultSet;
        private Object[] rowBuffer;
        private Gson gson = GsonUtils.getGson();

        public Iterator(List<MISAAttachmentTableColumn> columns, ResultSet resultSet) {
            this.columns = columns;
            this.resultSet = resultSet;
            this.rowBuffer = new Object[columns.size()];
        }

        public Object[] nextRow() throws SQLException  {
            if(!resultSet.next())
                return null;
            JsonObject jsonObject = gson.fromJson(resultSet.getString("json-data"), JsonObject.class);
            for(int i = 0; i < columns.size(); ++i) {
                rowBuffer[i] = columns.get(i).getValue(resultSet.getInt(1),
                        resultSet.getString("sample"),
                        resultSet.getString("cache"),
                        resultSet.getString("property"),
                        resultSet.getString("serialization-id"),
                        jsonObject);
            }
            return rowBuffer;
        }
    }
}
