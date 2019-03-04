package org.hkijena.misa_imagej.api.workbench;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MISAAttachmentDatabase {

    private MISAOutput misaOutput;
    private Connection databaseConnection;

    public MISAAttachmentDatabase(MISAOutput misaOutput) {
        this.misaOutput = misaOutput;
        try {
            initialize();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialize() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + misaOutput.getRootPath().resolve("attachment-index.sqlite"));
    }

    public MISAOutput getMisaOutput() {
        return misaOutput;
    }
}
